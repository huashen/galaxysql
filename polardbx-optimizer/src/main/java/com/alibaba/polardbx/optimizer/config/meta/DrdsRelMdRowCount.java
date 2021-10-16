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

package com.alibaba.polardbx.optimizer.config.meta;

import com.alibaba.polardbx.common.jdbc.ParameterContext;
import com.alibaba.polardbx.common.utils.logger.Logger;
import com.alibaba.polardbx.common.utils.logger.LoggerFactory;
import com.alibaba.polardbx.optimizer.PlannerContext;
import com.alibaba.polardbx.optimizer.core.planner.rule.util.CBOUtil;
import com.alibaba.polardbx.optimizer.core.rel.LogicalView;
import com.alibaba.polardbx.optimizer.core.rel.MysqlTableScan;
import com.alibaba.polardbx.optimizer.core.rel.Xplan.XPlanTableScan;
import com.alibaba.polardbx.optimizer.view.ViewPlan;
import org.apache.calcite.rel.core.Aggregate;
import org.apache.calcite.rel.core.GroupJoin;
import org.apache.calcite.rel.core.Join;
import org.apache.calcite.rel.core.Sort;
import org.apache.calcite.rel.core.TableLookup;
import org.apache.calcite.rel.logical.LogicalExpand;
import org.apache.calcite.rel.logical.RuntimeFilterBuilder;
import org.apache.calcite.rel.metadata.ReflectiveRelMetadataProvider;
import org.apache.calcite.rel.metadata.RelMdRowCount;
import org.apache.calcite.rel.metadata.RelMetadataProvider;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.util.BuiltInMethod;
import org.apache.calcite.util.ImmutableBitSet;

import java.util.Map;

public class DrdsRelMdRowCount extends RelMdRowCount {

    public static final RelMetadataProvider SOURCE =
        ReflectiveRelMetadataProvider.reflectiveSource(
            BuiltInMethod.ROW_COUNT.method, new DrdsRelMdRowCount());

    private final static Logger logger = LoggerFactory.getLogger(DrdsRelMdRowCount.class);

    public Double getRowCount(LogicalView rel, RelMetadataQuery mq) {
        return rel.getRowCount(mq);
    }

    public Double getRowCount(ViewPlan rel, RelMetadataQuery mq) {
        return mq.getRowCount(rel.getPlan());
    }

    public Double getRowCount(MysqlTableScan rel, RelMetadataQuery mq) {
        return mq.getRowCount(rel.getNodeForMetaQuery());
    }

    public Double getRowCount(XPlanTableScan rel, RelMetadataQuery mq) {
        return mq.getRowCount(rel.getNodeForMetaQuery());
    }

    @Override
    public Double getRowCount(Sort rel, RelMetadataQuery mq) {
        PlannerContext plannerContext = PlannerContext.getPlannerContext(rel);
        Double rowCount = mq.getRowCount(rel.getInput());
        if (rowCount == null) {
            return null;
        }

        Map<Integer, ParameterContext> params = plannerContext.getParams().getCurrentParameter();

        long offset = 0;
        if (rel.offset != null) {
            offset = CBOUtil.getRexParam(rel.offset, params);
        }

        rowCount = Math.max(rowCount - offset, 0D);

        if (rel.fetch != null) {
            long limit = CBOUtil.getRexParam(rel.fetch, params);
            if (limit < rowCount) {
                return (double) limit;
            }
        }
        return rowCount;
    }

    @Override
    public Double getRowCount(Aggregate rel, RelMetadataQuery mq) {
        ImmutableBitSet groupKey = rel.getGroupSet(); // .range(rel.getGroupCount());

        // rowCount is the cardinality of the group by columns
        Double distinctRowCount =
            mq.getDistinctRowCount(rel.getInput(), groupKey, null);
        double rowCount = mq.getRowCount(rel.getInput());
        if (distinctRowCount == null || distinctRowCount > rowCount) {
            distinctRowCount = mq.getRowCount(rel.getInput()) / 10;
        }

        // Grouping sets multiply
        distinctRowCount *= rel.getGroupSets().size();
        return distinctRowCount;
    }

    public Double getRowCount(GroupJoin rel, RelMetadataQuery mq) {
        ImmutableBitSet groupKey = rel.getGroupSet(); // .range(rel.getGroupCount());

        final int[] ints = groupKey.toArray();
        int min = ints[0];
        int max = ints[ints.length - 1];
        final long leftLength = rel.getLeft().getRowType().getFieldCount();
        // rowCount is the cardinality of the group by columns
        Double distinctRowCount = null;
        distinctRowCount =
            mq.getDistinctRowCount(rel.copyAsJoin(rel.getTraitSet(), rel.getCondition()), groupKey, null);
        final Join input = rel.copyAsJoin(rel.getTraitSet(), rel.getCondition());
        double rowCount = mq.getRowCount(input);
        if (distinctRowCount == null || distinctRowCount > rowCount) {
            distinctRowCount = rowCount / 10;
        }

        // Grouping sets multiply
        distinctRowCount *= rel.getGroupSets().size();
        return distinctRowCount;
    }

    public Double getRowCount(TableLookup rel, RelMetadataQuery mq) {
        if (rel.isRelPushedToPrimary()) {
            return mq.getRowCount(rel.getProject());
        } else {
            return mq.getRowCount(rel.getJoin().getLeft());
        }
    }

    public Double getRowCount(LogicalExpand rel, RelMetadataQuery mq) {
        return rel.estimateRowCount(mq);
    }

    public Double getRowCount(RuntimeFilterBuilder rel, RelMetadataQuery mq) {
        return mq.getRowCount(rel.getInput());
    }
}
