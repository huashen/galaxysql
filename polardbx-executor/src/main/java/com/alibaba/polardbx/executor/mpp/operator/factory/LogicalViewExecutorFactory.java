/*
 * Copyright [2013-2021], Alibaba Group Holding Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.polardbx.executor.mpp.operator.factory;

import com.google.common.base.Preconditions;
import com.alibaba.polardbx.common.properties.ConnectionParams;
import com.alibaba.polardbx.executor.operator.DrivingStreamTableScanExec;
import com.alibaba.polardbx.executor.operator.DrivingStreamTableScanSortExec;
import com.alibaba.polardbx.executor.operator.Executor;
import com.alibaba.polardbx.executor.operator.LookupTableScanExec;
import com.alibaba.polardbx.executor.operator.MergeSortTableScanClient;
import com.alibaba.polardbx.executor.operator.MergeSortWithBufferTableScanClient;
import com.alibaba.polardbx.executor.operator.ResumeTableScanExec;
import com.alibaba.polardbx.executor.operator.ResumeTableScanSortExec;
import com.alibaba.polardbx.executor.operator.TableScanClient;
import com.alibaba.polardbx.executor.operator.TableScanExec;
import com.alibaba.polardbx.executor.operator.TableScanSortExec;
import com.alibaba.polardbx.executor.operator.lookup.LookupConditionBuilder;
import com.alibaba.polardbx.executor.operator.spill.SpillerFactory;
import com.alibaba.polardbx.executor.operator.util.bloomfilter.BloomFilterConsume;
import com.alibaba.polardbx.executor.operator.util.bloomfilter.BloomFilterExpression;
import com.alibaba.polardbx.executor.utils.ExecUtils;
import com.alibaba.polardbx.optimizer.context.ExecutionContext;
import com.alibaba.polardbx.optimizer.core.CursorMeta;
import com.alibaba.polardbx.optimizer.core.datatype.DataType;
import com.alibaba.polardbx.optimizer.core.join.EquiJoinKey;
import com.alibaba.polardbx.optimizer.core.join.EquiJoinUtils;
import com.alibaba.polardbx.optimizer.core.join.LookupPredicate;
import com.alibaba.polardbx.optimizer.core.join.LookupPredicateBuilder;
import com.alibaba.polardbx.optimizer.core.rel.LogicalView;
import com.alibaba.polardbx.optimizer.utils.CalciteUtils;
import com.alibaba.polardbx.statistics.RuntimeStatHelper;
import org.apache.calcite.rel.core.Join;
import org.apache.calcite.rel.metadata.JaninoRelMetadataProvider;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rex.RexCall;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class LogicalViewExecutorFactory extends ExecutorFactory {

    private final int totalPrefetch;
    private final CursorMeta meta;
    private final AtomicInteger counter = new AtomicInteger(0);
    private final int parallelism;
    private final long maxRowCount;
    private LogicalView logicalView;
    private TableScanClient scanClient;
    private SpillerFactory spillerFactory;

    private boolean bSort;
    private long fetch;
    private long skip;

    private BloomFilterExpression filterExpression;

    private boolean enablePassiveResume;

    private boolean enableDrivingResume;

    private List<EquiJoinKey> allJoinKeys; // including null-safe equal (`<=>`)
    private LookupPredicate predicates;
    private List<DataType> dataTypeList;

    public LogicalViewExecutorFactory(
        LogicalView logicalView, int totalPrefetch, int parallelism, long maxRowCount, boolean bSort,
        long fetch, long skip, SpillerFactory spillerFactory, Map<Integer, BloomFilterExpression> bloomFilters,
        boolean enableRuntimeFilter) {
        this.logicalView = logicalView;
        this.totalPrefetch = totalPrefetch;
        this.meta = CursorMeta.build(CalciteUtils.buildColumnMeta(logicalView, "TableScanColumns"));
        this.parallelism = parallelism;
        this.maxRowCount = maxRowCount;
        this.bSort = bSort;
        this.fetch = fetch;
        this.skip = skip;
        this.spillerFactory = spillerFactory;

        if (logicalView.getJoin() != null) {
            Join join = logicalView.getJoin();
            this.allJoinKeys = EquiJoinUtils.buildEquiJoinKeys(join, join.getOuter(), join.getInner(),
                (RexCall) join.getCondition(), join.getJoinType(), true);
            this.predicates = new LookupPredicateBuilder(join).build(allJoinKeys);
        }

        if (enableRuntimeFilter) {
            List<Integer> bloomFilterIds = logicalView.getBloomFilters();
            if (bloomFilterIds.size() > 0) {
                List<BloomFilterConsume> consumes = new ArrayList<>();
                for (Integer bloomId : bloomFilterIds) {
                    consumes.add(new BloomFilterConsume(null, bloomId));
                }
                this.filterExpression = new BloomFilterExpression(consumes, true);
                bloomFilters.put(logicalView.getRelatedId(), filterExpression);
            }
        }
        this.dataTypeList = CalciteUtils.getTypes(logicalView.getRowType());
    }

    @Override
    public Executor createExecutor(ExecutionContext context, int index) {
        RelMetadataQuery.THREAD_PROVIDERS
            .set(JaninoRelMetadataProvider.of(logicalView.getCluster().getMetadataProvider()));

        TableScanExec scanExec;
        Join join = logicalView.getJoin();
        if (join != null) {
            boolean canShard = false;
            if (context.getParamManager().getBoolean(ConnectionParams.ENABLE_BKA_PRUNING)) {
                LogicalView lv = this.getLogicalView();
                if (lv.getTableNames().size() == 1) {
                    canShard = new LookupConditionBuilder(allJoinKeys, predicates, lv, context).canShard();
                }
            }
            scanExec = createLookupScanExec(context, canShard, predicates, allJoinKeys);
        } else {
            boolean useTransactionConnection = ExecUtils.useExplicitTransaction(context);

            if (bSort) {
                long limit = context.getParamManager().getLong(ConnectionParams.MERGE_SORT_BUFFER_SIZE);
                if (limit > 0) {
                    this.scanClient = new MergeSortWithBufferTableScanClient(
                        context, meta, useTransactionConnection, totalPrefetch);
                } else {
                    this.scanClient = new MergeSortTableScanClient(
                        context, meta, useTransactionConnection, totalPrefetch);
                }
            } else if (useTransactionConnection || enablePassiveResume || enableDrivingResume) {
                int prefetch = calculatePrefetchNum(counter.incrementAndGet(), parallelism);
                this.scanClient = new TableScanClient(context, meta, useTransactionConnection, prefetch);
            } else {
                synchronized (this) {
                    if (scanClient == null) {
                        this.scanClient =
                            new TableScanClient(context, meta, false, Math.max(totalPrefetch, parallelism));
                    }
                }
            }

            if (filterExpression != null) {
                scanClient.initWaitFuture(filterExpression.getWaitBloomFuture());
            }

            scanExec = buildTableScanExec(scanClient, context);
        }
        scanExec.setId(logicalView.getRelatedId());
        if (context.getRuntimeStatistics() != null) {
            RuntimeStatHelper.registerStatForExec(logicalView, scanExec, context);
        }
        return scanExec;
    }

    private TableScanExec buildTableScanExec(TableScanClient scanClient, ExecutionContext context) {
        int stepSize = context.getParamManager().getInt(ConnectionParams.RESUME_SCAN_STEP_SIZE);
        if (enablePassiveResume && !context.isShareReadView()) {
            if (bSort) {
                return new ResumeTableScanSortExec(
                    logicalView, context, scanClient.incrementSourceExec(), maxRowCount, skip, fetch, spillerFactory,
                    stepSize, dataTypeList);
            } else {
                return new ResumeTableScanExec(logicalView, context, scanClient.incrementSourceExec(),
                    spillerFactory, stepSize, dataTypeList);

            }
        } else if (enableDrivingResume) {
            if (bSort) {
                return new DrivingStreamTableScanSortExec(
                    logicalView, context, scanClient.incrementSourceExec(), maxRowCount, skip, fetch, spillerFactory,
                    stepSize, dataTypeList);
            } else {
                return new DrivingStreamTableScanExec(logicalView, context, scanClient.incrementSourceExec(),
                    spillerFactory, stepSize, dataTypeList);

            }
        } else {
            if (bSort) {
                return new TableScanSortExec(
                    logicalView, context, scanClient.incrementSourceExec(), maxRowCount, skip, fetch,
                    spillerFactory, dataTypeList);
            } else {
                return new TableScanExec(logicalView, context, scanClient.incrementSourceExec(),
                    maxRowCount,
                    spillerFactory, dataTypeList);
            }
        }
    }

    public void enablePassiveResumeSource() {
        this.enablePassiveResume = true;
        Preconditions.checkArgument(
            !(enablePassiveResume && enableDrivingResume), "Don't support stream scan in different mode");
    }

    public void enableDrivingResumeSource() {
        this.enableDrivingResume = true;
        Preconditions.checkArgument(
            !(enablePassiveResume && enableDrivingResume), "Don't support stream scan in different mode");
    }

    public TableScanExec createLookupScanExec(ExecutionContext context, boolean canShard,
                                              LookupPredicate predicate, List<EquiJoinKey> allJoinKeys) {
        boolean allowMultipleReadConn = ExecUtils.allowMultipleReadConns(context, logicalView);
        boolean useTransaction = ExecUtils.useExplicitTransaction(context);

        int prefetch = 1;
        if (allowMultipleReadConn) {
            prefetch = calculatePrefetchNum(counter.incrementAndGet(), parallelism);
            if (parallelism > 1 && totalPrefetch > 1) {
                //由于bkaJoin有动态裁剪能力，会导致部分scan的分配split被裁剪为0，浪费prefetch的分配名额
                prefetch = prefetch == 1 ? 2 : prefetch;
            }
        }

        TableScanClient scanClient = new TableScanClient(context, meta, useTransaction, prefetch);
        TableScanExec scanExec =
            new LookupTableScanExec(logicalView, context, scanClient.incrementSourceExec(), canShard, spillerFactory,
                predicate, allJoinKeys, dataTypeList);
        scanExec.setId(logicalView.getRelatedId());
        if (context.getRuntimeStatistics() != null) {
            RuntimeStatHelper.registerStatForExec(logicalView, scanExec, context);
        }
        return scanExec;
    }

    private int calculatePrefetchNum(int index, int parallelism) {
        Preconditions.checkArgument(index <= parallelism, "index must less than " + parallelism);
        if (parallelism >= totalPrefetch) {
            return 1;
        } else {
            if (index <= totalPrefetch % parallelism) {
                return totalPrefetch / parallelism + 1;
            } else {
                return totalPrefetch / parallelism;
            }
        }
    }

    public boolean isPushDownSort() {
        return bSort;
    }

    public LogicalView getLogicalView() {
        return logicalView;
    }

    public int getParallelism() {
        return this.parallelism;
    }
}
