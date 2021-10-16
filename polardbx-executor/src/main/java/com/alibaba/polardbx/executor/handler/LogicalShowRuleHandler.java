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

package com.alibaba.polardbx.executor.handler;

import com.alibaba.polardbx.common.utils.TStringUtil;
import com.alibaba.polardbx.executor.cursor.Cursor;
import com.alibaba.polardbx.executor.cursor.impl.ArrayResultCursor;
import com.alibaba.polardbx.executor.spi.IRepository;
import com.alibaba.polardbx.gms.metadb.table.TableStatus;
import com.alibaba.polardbx.optimizer.OptimizerContext;
import com.alibaba.polardbx.optimizer.config.table.TableMeta;
import com.alibaba.polardbx.optimizer.context.ExecutionContext;
import com.alibaba.polardbx.optimizer.core.datatype.DataTypes;
import com.alibaba.polardbx.optimizer.core.function.calc.scalar.CanAccessTable;
import com.alibaba.polardbx.optimizer.core.rel.dal.LogicalShow;
import com.alibaba.polardbx.optimizer.index.HumanReadableRule;
import com.alibaba.polardbx.optimizer.rule.TddlRuleManager;
import com.alibaba.polardbx.optimizer.utils.RelUtils;
import com.alibaba.polardbx.rule.Rule;
import com.alibaba.polardbx.rule.TableRule;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.sql.SqlShowRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author chenmo.cm
 */
public class LogicalShowRuleHandler extends HandlerCommon {

    public LogicalShowRuleHandler(IRepository repo) {
        super(repo);
    }

    @Override
    public Cursor handle(RelNode logicalPlan, ExecutionContext executionContext) {
        final LogicalShow show = (LogicalShow) logicalPlan;
        final SqlShowRule showRule = (SqlShowRule) show.getNativeSqlNode();
        String tableName = null;
        String schemaName = ((LogicalShow) logicalPlan).getSchemaName();
        if (TStringUtil.isEmpty(schemaName)) {
            schemaName = executionContext.getSchemaName();
        }

        final OptimizerContext context = OptimizerContext.getContext(schemaName);
        final TddlRuleManager rule = context.getRuleManager();
        if (null != showRule.getTableName()) {
            tableName = RelUtils.lastStringValue(showRule.getTableName());
            context.getLatestSchemaManager().getTable(tableName);
        }

        boolean isTableHidden = false;
        boolean isTableWithoutPrivileges = false;

        if (tableName != null) {
            isTableWithoutPrivileges = !CanAccessTable.verifyPrivileges(
                schemaName,
                tableName,
                executionContext);
        }

        if (!showRule.isFull()) {
            ArrayResultCursor result = new ArrayResultCursor("RULE");
            result.addColumn("Id", DataTypes.IntegerType);
            result.addColumn("TABLE_NAME", DataTypes.StringType);
            result.addColumn("BROADCAST", DataTypes.BooleanType);

            result.addColumn("DB_PARTITION_KEY", DataTypes.StringType);
            result.addColumn("DB_PARTITION_POLICY", DataTypes.StringType);
            result.addColumn("DB_PARTITION_COUNT", DataTypes.StringType);

            result.addColumn("TB_PARTITION_KEY", DataTypes.StringType);
            result.addColumn("TB_PARTITION_POLICY", DataTypes.StringType);
            result.addColumn("TB_PARTITION_COUNT", DataTypes.StringType);

            result.initMeta();

            int index = 0;
            Collection<TableRule> tables;
            if (null != tableName && (isTableHidden || isTableWithoutPrivileges)) {
                tables = Collections.emptyList();
            } else if (null != tableName) {
                tables = Arrays.asList(rule.getTableRule(tableName));
            } else {
                tables = rule.getTableRules();
            }

            for (TableRule table : tables) {

                String dbPartitionPolicy = null;
                String tbPartitionPolicy = null;

                int dbCount = 1;
                int tbCount = 1;

                if (table == null) {
                    if (!isTableHidden && !isTableWithoutPrivileges) {
                        table = new TableRule();
                        table.setVirtualTbName(tableName);
                        // continue;
                    }
                } else {
                    TableMeta tableMeta =
                        executionContext.getSchemaManager(schemaName).getTable(table.getVirtualTbName());
                    isTableHidden |= tableMeta.getStatus() != TableStatus.PUBLIC;

                    isTableWithoutPrivileges = !CanAccessTable.verifyPrivileges(
                        schemaName,
                        table.getVirtualTbName(),
                        executionContext);

                    if (isTableHidden || isTableWithoutPrivileges) {
                        continue;
                    }

                    HumanReadableRule humanReadableTableRule = HumanReadableRule.getHumanReadableRule(table);
                    dbPartitionPolicy = humanReadableTableRule.dbPartitionPolicy;
                    dbCount = humanReadableTableRule.dbCount;
                    tbPartitionPolicy = humanReadableTableRule.tbPartitionPolicy;
                    tbCount = humanReadableTableRule.tbCount;
                }

                result.addRow(new Object[] {
                    index++,// Id
                    table.getVirtualTbName(), // TABLE_NAME
                    table.isBroadcast(), // BROADCAST

                    table.getDbPartitionKeys() == null ? null : TStringUtil.join(table.getDbPartitionKeys(), ","),
// DB_PARTITION_KEY
                    dbPartitionPolicy, // DB_PARTITION_POLICY
                    dbCount,

                    table.getTbPartitionKeys() == null ? null : TStringUtil.join(table.getTbPartitionKeys(), ","),
// TB_PARTITION_KEY
                    tbPartitionPolicy, // TB_PARTITION_POLICY
                    tbCount});

            }
            return result;
        } else {

            ArrayResultCursor result = new ArrayResultCursor("RULE");
            result.addColumn("Id", DataTypes.IntegerType);
            result.addColumn("TABLE_NAME", DataTypes.StringType);
            result.addColumn("BROADCAST", DataTypes.BooleanType);
            result.addColumn("JOIN_GROUP", DataTypes.StringType);

            result.addColumn("ALLOW_FULL_TABLE_SCAN", DataTypes.BooleanType);
            result.addColumn("DB_NAME_PATTERN", DataTypes.StringType);
            result.addColumn("DB_RULES_STR", DataTypes.StringType);
            result.addColumn("TB_NAME_PATTERN", DataTypes.StringType);
            result.addColumn("TB_RULES_STR", DataTypes.StringType);
            result.addColumn("PARTITION_KEYS", DataTypes.StringType);
            result.addColumn("DEFAULT_DB_INDEX", DataTypes.StringType);
            result.initMeta();
            int index = 0;
            Collection<TableRule> tables = null;

            if (null != tableName && isTableWithoutPrivileges) {
                tables = Collections.emptyList();
            } else if (null != tableName) {
                tables = Arrays.asList(rule.getTableRule(tableName));
            } else {
                tables = rule.getTableRules();
            }

            for (TableRule table : tables) {
                if (table == null) {
                    table = new TableRule();
                    table.setVirtualTbName(tableName);
                    table.setBroadcast(false);
                    table.setAllowFullTableScan(true);
                    table.setDbNamePattern(rule.getDefaultDbIndex(tableName));
                    table.setTbNamePattern(tableName);
                }

                isTableWithoutPrivileges = !CanAccessTable.verifyPrivileges(
                    schemaName,
                    table.getVirtualTbName(),
                    executionContext);

                if (isTableWithoutPrivileges) {
                    continue;
                }

                result.addRow(new Object[] {
                    index++, table.getVirtualTbName(), table.isBroadcast(),
                    table.getJoinGroup(), table.isAllowFullTableScan(), table.getDbNamePattern(),
                    buildStr(table.getDbShardRules()), table.getTbNamePattern(), buildStr(table.getTbShardRules()),
                    buildStr(table.getShardColumns()), rule.getDefaultDbIndex(table.getVirtualTbName())});

            }
            return result;
        }
    }

    public String buildStr(List list) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (Object o : list) {
            if (!first) {
                sb.append("\n");
            }
            if (o instanceof String) {
                sb.append(o);
            } else if (o instanceof Rule) {
                Rule r = (Rule) o;
                sb.append(r.getExpression());
            }
            first = false;
        }

        return sb.toString();
    }
}
