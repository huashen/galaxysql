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

package com.alibaba.polardbx.executor.ddl.job.task.cdc;

import com.alibaba.fastjson.annotation.JSONCreator;
import com.alibaba.polardbx.common.cdc.CdcManagerHelper;
import com.alibaba.polardbx.common.cdc.DdlVisibility;
import com.alibaba.polardbx.common.cdc.ICdcManager;
import com.alibaba.polardbx.executor.ddl.job.converter.PhysicalPlanData;
import com.alibaba.polardbx.executor.ddl.job.meta.TableMetaChanger;
import com.alibaba.polardbx.executor.ddl.job.task.BaseDdlTask;
import com.alibaba.polardbx.executor.ddl.job.task.util.TaskName;
import com.alibaba.polardbx.executor.utils.failpoint.FailPoint;
import com.alibaba.polardbx.gms.topology.DbInfoManager;
import com.alibaba.polardbx.optimizer.context.DdlContext;
import com.alibaba.polardbx.optimizer.context.ExecutionContext;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import org.apache.calcite.sql.SqlKind;

import java.sql.Connection;
import java.util.Map;

/**
 * Created by ziyang.lb
 **/
@TaskName(name = "CdcDdlMarkTask")
@Getter
@Setter
public class CdcDdlMarkTask extends BaseDdlTask {
    private final PhysicalPlanData physicalPlanData;

    @JSONCreator
    public CdcDdlMarkTask(String schemaName, PhysicalPlanData physicalPlanData) {
        super(schemaName);
        this.physicalPlanData = physicalPlanData;
    }

    @Override
    protected void duringTransaction(Connection metaDbConnection, ExecutionContext executionContext) {
        updateSupportedCommands(true, false, metaDbConnection);
        FailPoint.injectRandomExceptionFromHint(executionContext);
        FailPoint.injectRandomSuspendFromHint(executionContext);
        if (physicalPlanData.getKind() == SqlKind.CREATE_TABLE) {
            mark4CreateTable(executionContext);
        } else if (physicalPlanData.getKind() == SqlKind.DROP_TABLE) {
            mark4DropTable(executionContext);
        } else if (physicalPlanData.getKind() == SqlKind.RENAME_TABLE) {
            if (DbInfoManager.getInstance().isNewPartitionDb(schemaName)) {
                mark4RenamePartitionModeTable(executionContext);
            } else {
                mark4RenameTable(executionContext);
            }
        } else if (physicalPlanData.getKind() == SqlKind.ALTER_TABLE) {
            mark4AlterTable(executionContext);
        } else if (physicalPlanData.getKind() == SqlKind.CREATE_INDEX) {
            mark4CreateIndex(executionContext);
        } else if (physicalPlanData.getKind() == SqlKind.DROP_INDEX) {
            mark4DropIndex(executionContext);
        } else if (physicalPlanData.getKind() == SqlKind.TRUNCATE_TABLE) {
            if (physicalPlanData.isTruncatePartition()) {
                mark4TruncatePartition(executionContext);
            } else {
                mark4TruncateTable(executionContext);
            }
        } else {
            throw new RuntimeException("not supported sql kind : " + physicalPlanData.getKind());
        }
    }

    private void mark4CreateTable(ExecutionContext executionContext) {
        DdlContext ddlContext = executionContext.getDdlContext();
        CdcManagerHelper.getInstance()
            .notifyDdlNew(schemaName, physicalPlanData.getLogicalTableName(), physicalPlanData.getKind().name(),
                physicalPlanData.getCreateTablePhysicalSql(), ddlContext.getDdlType(), ddlContext.getJobId(),
                getTaskId(),
                DdlVisibility.Public, executionContext.getExtraCmds());
    }

    private void mark4DropTable(ExecutionContext executionContext) {
        // CdcDdlMarkTask?????????????????????????????????????????????????????????????????????????????????????????????
        DdlContext ddlContext = executionContext.getDdlContext();
        CdcManagerHelper.getInstance()
            .notifyDdlNew(schemaName, physicalPlanData.getLogicalTableName(), physicalPlanData.getKind().name(),
                ddlContext.getDdlStmt(), ddlContext.getDdlType(), ddlContext.getJobId(), getTaskId(),
                DdlVisibility.Public,
                executionContext.getExtraCmds());
    }

    private void mark4RenameTable(ExecutionContext executionContext) {
        // ??????????????????????????????????????????????????????tablePattern????????????????????????cdcManager
        // ??????????????????????????????????????????????????????????????????????????????rename(?????????????????????????????????????????????dml??????????????????)???cdc?????????????????????????????????
        // ??????????????????????????????????????????tablePattern?????????????????????Rename?????????????????????????????????????????????????????????????????????????????????????????????
        String newTbNamePattern = TableMetaChanger.buildNewTbNamePattern(executionContext, schemaName,
            physicalPlanData.getLogicalTableName(), physicalPlanData.getNewLogicalTableName());
        Map<String, Object> params = Maps.newHashMap();
        params.put(ICdcManager.TABLE_NEW_NAME, physicalPlanData.getNewLogicalTableName());
        params.put(ICdcManager.TABLE_NEW_PATTERN, newTbNamePattern);
        params.putAll(executionContext.getExtraCmds());

        DdlContext ddlContext = executionContext.getDdlContext();
        CdcManagerHelper.getInstance()
            .notifyDdlNew(schemaName, physicalPlanData.getLogicalTableName(), physicalPlanData.getKind().name(),
                ddlContext.getDdlStmt(), ddlContext.getDdlType(), ddlContext.getJobId(), getTaskId(),
                DdlVisibility.Public, params);
    }

    private void mark4RenamePartitionModeTable(ExecutionContext executionContext) {
        //???????????????tablePattern?????????????????????????????????????????????????????????????????????????????????
        Map<String, Object> params = Maps.newHashMap();
        params.put(ICdcManager.TABLE_NEW_NAME, physicalPlanData.getNewLogicalTableName());
        params.putAll(executionContext.getExtraCmds());

        DdlContext ddlContext = executionContext.getDdlContext();
        CdcManagerHelper.getInstance()
            .notifyDdlNew(schemaName, physicalPlanData.getLogicalTableName(), physicalPlanData.getKind().name(),
                ddlContext.getDdlStmt(), ddlContext.getDdlType(), ddlContext.getJobId(), getTaskId(),
                DdlVisibility.Public, params);
    }

    private void mark4AlterTable(ExecutionContext executionContext) {
        DdlContext ddlContext = executionContext.getDdlContext();
        CdcManagerHelper.getInstance()
            .notifyDdlNew(schemaName, physicalPlanData.getLogicalTableName(), physicalPlanData.getKind().name(),
                ddlContext.getDdlStmt(), ddlContext.getDdlType(), ddlContext.getJobId(), getTaskId(),
                DdlVisibility.Public,
                executionContext.getExtraCmds());
    }

    private void mark4CreateIndex(ExecutionContext executionContext) {
        DdlContext ddlContext = executionContext.getDdlContext();
        CdcManagerHelper.getInstance()
            .notifyDdlNew(schemaName, physicalPlanData.getLogicalTableName(), physicalPlanData.getKind().name(),
                ddlContext.getDdlStmt(), ddlContext.getDdlType(), ddlContext.getJobId(), getTaskId(),
                DdlVisibility.Public,
                executionContext.getExtraCmds());
    }

    private void mark4DropIndex(ExecutionContext executionContext) {
        DdlContext ddlContext = executionContext.getDdlContext();
        CdcManagerHelper.getInstance()
            .notifyDdlNew(schemaName, physicalPlanData.getLogicalTableName(), physicalPlanData.getKind().name(),
                ddlContext.getDdlStmt(), ddlContext.getDdlType(), ddlContext.getJobId(), getTaskId(),
                DdlVisibility.Public,
                executionContext.getExtraCmds());
    }

    private void mark4TruncateTable(ExecutionContext executionContext) {
        DdlContext ddlContext = executionContext.getDdlContext();
        CdcManagerHelper.getInstance()
            .notifyDdlNew(schemaName, physicalPlanData.getLogicalTableName(), physicalPlanData.getKind().name(),
                ddlContext.getDdlStmt(), ddlContext.getDdlType(), ddlContext.getJobId(), getTaskId(),
                DdlVisibility.Public,
                executionContext.getExtraCmds());
    }

    private void mark4TruncatePartition(ExecutionContext executionContext) {
        DdlContext ddlContext = executionContext.getDdlContext();
        CdcManagerHelper.getInstance()
            .notifyDdlNew(schemaName, physicalPlanData.getLogicalTableName(), physicalPlanData.getKind().name(),
                ddlContext.getDdlStmt(), ddlContext.getDdlType(), ddlContext.getJobId(), getTaskId(),
                DdlVisibility.Private,
                executionContext.getExtraCmds());
    }
}
