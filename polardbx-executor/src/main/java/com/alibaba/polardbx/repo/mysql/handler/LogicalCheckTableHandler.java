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

package com.alibaba.polardbx.repo.mysql.handler;

import com.alibaba.polardbx.atom.TAtomDataSource;
import com.alibaba.polardbx.common.model.Group;
import com.alibaba.polardbx.common.model.Group.GroupType;
import com.alibaba.polardbx.common.utils.GeneralUtil;
import com.alibaba.polardbx.common.utils.logger.Logger;
import com.alibaba.polardbx.common.utils.logger.LoggerFactory;
import com.alibaba.polardbx.config.ConfigDataMode;
import com.alibaba.polardbx.executor.cursor.Cursor;
import com.alibaba.polardbx.executor.cursor.impl.ArrayResultCursor;
import com.alibaba.polardbx.executor.handler.HandlerCommon;
import com.alibaba.polardbx.executor.spi.IRepository;
import com.alibaba.polardbx.group.jdbc.TGroupDataSource;
import com.alibaba.polardbx.optimizer.OptimizerContext;
import com.alibaba.polardbx.optimizer.PlannerContext;
import com.alibaba.polardbx.optimizer.context.ExecutionContext;
import com.alibaba.polardbx.optimizer.core.datatype.DataTypes;
import com.alibaba.polardbx.optimizer.core.function.calc.scalar.CanAccessTable;
import com.alibaba.polardbx.optimizer.core.rel.dal.LogicalDal;
import com.alibaba.polardbx.optimizer.rule.TddlRuleManager;
import com.alibaba.polardbx.optimizer.utils.RelUtils;
import com.alibaba.polardbx.repo.mysql.checktable.CheckTableUtil;
import com.alibaba.polardbx.repo.mysql.checktable.FieldDescription;
import com.alibaba.polardbx.repo.mysql.checktable.TableCheckResult;
import com.alibaba.polardbx.repo.mysql.checktable.TableDescription;
import com.alibaba.polardbx.repo.mysql.spi.MyRepository;
import com.alibaba.polardbx.rule.TableRule;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.sql.SqlCheckTable;
import org.apache.calcite.sql.SqlNode;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author chenmo.cm
 */
public class LogicalCheckTableHandler extends HandlerCommon {

    private static final Logger logger = LoggerFactory.getLogger(LogicalCheckTableHandler.class);

    public LogicalCheckTableHandler(IRepository repo) {
        super(repo);
    }

    @Override
    public Cursor handle(RelNode logicalPlan, ExecutionContext executionContext) {
        final LogicalDal dal = (LogicalDal) logicalPlan;
        final SqlCheckTable checkTable = (SqlCheckTable) dal.getNativeSqlNode();

        List<String> tableNameList = new LinkedList<>();
        for (SqlNode tableName : checkTable.getTableNames()) {
            tableNameList.add(RelUtils.lastStringValue(tableName));
        }

        String appName = PlannerContext.getPlannerContext(logicalPlan).getSchemaName();
        ArrayResultCursor result = new ArrayResultCursor("checkTable");
        result.addColumn("Table", DataTypes.StringType);
        result.addColumn("Op", DataTypes.StringType);
        result.addColumn("Msg_type", DataTypes.StringType);
        result.addColumn("Msg_text", DataTypes.StringType);

        boolean isTableWithPrivileges = false;

        for (int i = 0; i < tableNameList.size(); i++) {
            String table = tableNameList.get(i);
            isTableWithPrivileges = CanAccessTable.verifyPrivileges(
                executionContext.getSchemaName(),
                table,
                executionContext);
            if (isTableWithPrivileges) {
                doCheckForOneTable(executionContext.getSchemaName(), appName, table, executionContext, result);
            }
        }

        return result;

    }

    protected void doCheckForOneTable(String schemaName, String appName, String logicalTableName,
                                      ExecutionContext executionContext, ArrayResultCursor result) {

        // ?????????????????????????????????
        boolean hasTableRule = false;
        boolean isBroadcastTable = false;
        TddlRuleManager tddlRuleManager = OptimizerContext.getContext(schemaName).getRuleManager();
        TableRule tableRule = tddlRuleManager.getTableRule(logicalTableName);
        if (tableRule != null) {
            hasTableRule = true;
        }

        // =============????????????????????????=============
        String defaultDbIndex = tddlRuleManager.getDefaultDbIndex(logicalTableName);
        // ?????????????????????
        if (!hasTableRule) {
            // ???????????????????????????????????????????????????????????????
            // ????????????????????????defaultDbIndex, ??????????????????schema
            doCheckForSingleTable(appName, defaultDbIndex, logicalTableName, logicalTableName, result);

        } else {

            final String physicalTableName = tableRule.getTbNamePattern();
            String tableText = String.format("%s.%s", appName, logicalTableName);
            String opText = "check";
            String statusText = "Error";

            // ?????????????????????
            List<Group> groupList = OptimizerContext.getContext(schemaName).getMatrix().getGroups();
            List<Group> jdbcGroupList = new ArrayList<Group>();
            for (Group group : groupList) {
                if (group.getType() == GroupType.MYSQL_JDBC) {
                    jdbcGroupList.add(group);
                }
            }

            // < groupName, < tableName, TableDescription > >
            Map<String, Map<String, TableDescription>> groupTableDescMaps =
                new HashMap<String, Map<String, TableDescription>>();

            // referenceGroup????????????????????????????????????????????????
            String referenceGroupName = null;

            // referenceTable: ?????????????????????????????????????????????
            String referenceTableName = null;

            // ??????????????????????????????????????????????????????
            // ??????, ???????????????dbRuleArr???tbRuleArr???,
            // ?????????dbNamePattern???tbNamePattern????????????
            boolean isSingleTable = false;
            String groupNameForSingleTable = null;
            String tableNameForSingleTable = null;
            Map<String, Set<String>> dbTbActualTopology = tableRule.getActualTopology();
            if (dbTbActualTopology.size() == 1) {
                Set<String> groupKeySet = dbTbActualTopology.keySet();
                Iterator<String> groupKeySetItor = groupKeySet.iterator();
                String groupKey = groupKeySetItor.next();
                Set<String> tableSet = dbTbActualTopology.get(groupKey);
                if (tableSet.size() == 1) {
                    isSingleTable = true;
                    Iterator<String> tableSetItor = tableSet.iterator();
                    String table = tableSetItor.next();
                    groupNameForSingleTable = groupKey;
                    tableNameForSingleTable = table;
                }
            }

            // ???????????????????????????
            isBroadcastTable = tableRule.isBroadcast();

            // We should check each group for broadcast table.
            if (isBroadcastTable) {
                referenceGroupName = defaultDbIndex;
                referenceTableName = physicalTableName;
                StringBuilder targetSql = new StringBuilder("describe ");
                targetSql.append(physicalTableName);

                // ??????????????????group???????????????
                for (Group group : jdbcGroupList) {
                    TableDescription tableDescription = CheckTableUtil.getTableDescription((MyRepository) this.repo,
                        group.getName(),
                        physicalTableName,
                        false,
                        schemaName);
                    Map<String, TableDescription> tableNameDescMaps = new HashMap<String, TableDescription>();
                    tableNameDescMaps.put(physicalTableName, tableDescription);
                    groupTableDescMaps.put(group.getName(), tableNameDescMaps);
                }

            } else {
                if (isSingleTable) {
                    // A single table only exists in the single group in PolarDB-X mode.
                    doCheckForSingleTable(appName,
                        groupNameForSingleTable,
                        logicalTableName,
                        tableNameForSingleTable,
                        result);
                    return;
                }

                // ??????????????????????????????????????????
                for (Map.Entry<String, Set<String>> tbTopologyInOneDb : dbTbActualTopology.entrySet()) {
                    String targetGroup = tbTopologyInOneDb.getKey();
                    Set<String> tableSet = tbTopologyInOneDb.getValue();
                    Iterator<String> tableSetItor = tableSet.iterator();
                    Map<String, TableDescription> tableNameDescMaps = new HashMap<String, TableDescription>();

                    if (StringUtils.isEmpty(referenceGroupName)) {
                        referenceGroupName = targetGroup;
                    }

                    while (tableSetItor.hasNext()) {

                        // ??????????????????group????????????description
                        String targetTable = tableSetItor.next();
                        TableDescription tableDescription = CheckTableUtil.getTableDescription((MyRepository) this.repo,
                            targetGroup,
                            targetTable,
                            false,
                            schemaName);

                        tableNameDescMaps.put(targetTable, tableDescription);

                        // ??? referenceGroupName ????????????????????????????????????
                        if (targetGroup.equals(referenceGroupName)) {
                            // ????????????
                            if (StringUtils.isEmpty(referenceTableName)) {
                                referenceTableName = targetTable;
                            }
                        }
                    }
                    groupTableDescMaps.put(targetGroup, tableNameDescMaps);
                }

            }

            // =============?????????????????????=============

            // 1. 1??????????????????????????????????????????
            boolean isStatusOK = true;
            List<TableCheckResult> abnormalTableCheckResultList = new ArrayList<TableCheckResult>();
            for (Map.Entry<String, Map<String, TableDescription>> groupTableItems : groupTableDescMaps.entrySet()) {
                Map<String, TableDescription> tableNameAndDescMap = groupTableItems.getValue();
                for (Map.Entry<String, TableDescription> tableDescItem : tableNameAndDescMap.entrySet()) {
                    TableDescription tableDesc = tableDescItem.getValue();
                    if (tableDesc.getFields() == null) {
                        TableCheckResult abnormalTable = new TableCheckResult();
                        abnormalTable.setTableDesc(tableDesc);
                        abnormalTable.setExist(false);
                        abnormalTable.setFieldCountTheSame(false);
                        abnormalTable.setFieldDescTheSame(false);
                        abnormalTableCheckResultList.add(abnormalTable);
                    }
                }
            }

            // 1.2 ???????????????????????????
            if (abnormalTableCheckResultList.size() > 0) {
                TableCheckResult checkResult = abnormalTableCheckResultList.get(0);
                boolean isBroadcast = isBroadcastTable;
                for (int i = 0; i < abnormalTableCheckResultList.size(); i++) {
                    checkResult = abnormalTableCheckResultList.get(i);
                    outputExistCheckResults(result, tableText, opText, statusText, checkResult, isBroadcast);
                }
                isStatusOK = false;
                return;
            }

            // 2.1 ????????????????????????????????????????????????????????????????????????????????????
            Map<String, TableDescription> tableDescsOfReferGroup = groupTableDescMaps.get(referenceGroupName);
            TableDescription referTableDesc = tableDescsOfReferGroup.get(referenceTableName);
            for (Map.Entry<String, Map<String, TableDescription>> groupTableItems : groupTableDescMaps.entrySet()) {
                Map<String, TableDescription> tableNameAndDescMap = groupTableItems.getValue();
                for (Map.Entry<String, TableDescription> tableDescItem : tableNameAndDescMap.entrySet()) {
                    TableDescription tableDesc = tableDescItem.getValue();
                    TableCheckResult checkResult = CheckTableUtil.verifyTableMeta(referTableDesc, tableDesc);
                    if (!isCheckResultNormal(checkResult)) {
                        abnormalTableCheckResultList.add(checkResult);
                    }
                }
            }

            // 2.2 ????????????????????????schema?????????; ????????????????????????????????????????????????????????????????????????
            if (abnormalTableCheckResultList.size() > 0) {
                boolean isBroadcast = isBroadcastTable;
                for (int i = 0; i < abnormalTableCheckResultList.size(); i++) {
                    TableCheckResult checkResult = abnormalTableCheckResultList.get(i);
                    outputFieldCheckResults(result, tableText, opText, statusText, checkResult, isBroadcast);
                }
                isStatusOK = false;
            }
        }
    }

    private void outputExistCheckResults(ArrayResultCursor result, String tableText, String opText, String statusText,
                                         TableCheckResult checkResult, boolean isBroadcast) {
        TableDescription tableDesc = checkResult.getTableDesc();
        String tblName = tableDesc.getTableName();
        String grpName = tableDesc.getGroupName();
        String msgContent = String.format("Table '%s.%s' doesn't exist", grpName, tblName);
        if (isBroadcast) {
            msgContent = "[broadcast] " + msgContent;
        }
        result.addRow(new Object[] {tableText, opText, statusText, msgContent});
    }

    private void outputFieldCheckResults(ArrayResultCursor result, String tableText, String opText, String statusText,
                                         TableCheckResult checkResult, boolean isBroadcast) {
        String grpName = checkResult.getTableDesc().getGroupName();
        String tlbName = checkResult.getTableDesc().getTableName();
        Map<String, FieldDescription> incorrectFields = checkResult.getAbnormalFieldDescMaps();
        StringBuilder incorrectFieldsMsgBuilder = new StringBuilder("");
        for (Map.Entry<String, FieldDescription> incorrectFieldItem : incorrectFields.entrySet()) {
            if (StringUtils.isNotEmpty(incorrectFieldsMsgBuilder.toString())) {
                incorrectFieldsMsgBuilder.append(", ");
            }
            incorrectFieldsMsgBuilder.append(incorrectFieldItem.getKey());
        }

        StringBuilder missingFieldsMsgBuilder = new StringBuilder("");
        Map<String, FieldDescription> missingFields = checkResult.getMissingFieldDescMaps();
        for (Map.Entry<String, FieldDescription> missingFieldItem : missingFields.entrySet()) {
            if (StringUtils.isNotEmpty(missingFieldsMsgBuilder.toString())) {
                missingFieldsMsgBuilder.append(", ");
            }
            missingFieldsMsgBuilder.append(missingFieldItem.getKey());
        }

        String incorrectFieldsMsg = incorrectFieldsMsgBuilder.toString();
        String missingFieldsMsg = missingFieldsMsgBuilder.toString();
        String msgContent = null;
        if (StringUtils.isNotEmpty(incorrectFieldsMsg) && StringUtils.isNotEmpty(missingFieldsMsg)) {
            msgContent = String.format("Table '%s.%s' find incorrect columns '%s', and find missing columns '%s'",
                grpName,
                tlbName,
                incorrectFieldsMsg,
                missingFieldsMsg);
        } else if (StringUtils.isNotEmpty(incorrectFieldsMsg)) {
            msgContent = String.format("Table '%s.%s' find incorrect columns '%s'",
                grpName,
                tlbName,
                incorrectFieldsMsg);
        } else if (StringUtils.isNotEmpty(missingFieldsMsg)) {
            msgContent = String.format("Table '%s.%s' find missing columns '%s'", grpName, tlbName, missingFieldsMsg);
        } else {
            msgContent = String.format("Table '%s.%s' is invaild", grpName, tlbName);
        }
        if (isBroadcast) {
            msgContent = "[broadcast] " + msgContent;
        }
        if (checkResult.isShadowTable()) {
            msgContent += ", please recreate the shadow table";
        } else {
            msgContent += ", please recreate table";
        }
        result.addRow(new Object[] {tableText, opText, statusText, msgContent});
    }

    protected boolean isCheckResultNormal(TableCheckResult checkResult) {

        if (checkResult.isExist() && checkResult.getUnexpectedFieldDescMaps().size() == 0
            && checkResult.getMissingFieldDescMaps().size() == 0
            && checkResult.getIncorrectFieldDescMaps().size() == 0) {
            return true;
        }

        return false;

    }

    protected void doCheckForSingleTable(String appName, String groupName,
                                         String logicalTableName,
                                         String physicalTableName,
                                         ArrayResultCursor result) {

        MyRepository myRepository = (MyRepository) this.repo;
        TGroupDataSource groupDataSource = (TGroupDataSource) myRepository.getDataSource(groupName);
        TAtomDataSource atomDataSource = CheckTableUtil.findMasterAtomForGroup(groupDataSource);
        StringBuilder targetSql = new StringBuilder("check table ");
        targetSql.append("`" + physicalTableName + "`");
        Connection conn = null;
        ResultSet rs = null;
        Throwable ex = null;
        try {
            conn = (Connection) atomDataSource.getConnection();
            rs = conn.createStatement().executeQuery(targetSql.toString());
            String tableText = String.format("%s.%s", appName, logicalTableName);
            if (rs.next()) {
                String opText = rs.getString(2);
                String statusText = rs.getString(3);
                String msgText = rs.getString(4);
                result.addRow(new Object[] {tableText, opText, statusText, msgText});
            }
        } catch (Throwable e) {
            // ?????????????????????
            logger.error(e);
            ex = e;

        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (conn != null) {
                    conn.close();
                }

            } catch (SQLException e) {
                logger.error(e);
            }

            if (ex != null) {
                GeneralUtil.nestedException(ex);
            }
        }
    }
}
