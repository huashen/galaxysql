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

package com.alibaba.polardbx.executor.balancer.action;

import com.alibaba.fastjson.annotation.JSONCreator;
import com.alibaba.polardbx.executor.ddl.job.task.BaseDdlTask;
import com.alibaba.polardbx.executor.ddl.job.task.util.TaskName;
import com.alibaba.polardbx.executor.utils.failpoint.FailPoint;
import com.alibaba.polardbx.gms.topology.DbTopologyManager;
import com.alibaba.polardbx.optimizer.context.ExecutionContext;

import java.sql.Connection;

/**
 * Drop a storage group
 *
 * @author moyi
 * @since 2021/08
 */
@TaskName(name = "DropStorageGroupTask")
public class DropStorageGroupTask extends BaseDdlTask implements BalanceAction {

    private final String groupName;

    @JSONCreator
    public DropStorageGroupTask(String schemaName, String groupName) {
        super(schemaName);
        this.groupName = groupName;
    }

    @Override
    protected void duringTransaction(Connection metaDbConnection, ExecutionContext executionContext) {
        DbTopologyManager.removeGroupByName(schemaName, groupName, metaDbConnection);
        FailPoint.injectRandomExceptionFromHint(executionContext);
        FailPoint.injectRandomSuspendFromHint(executionContext);
    }

    @Override
    public String getSchema() {
        return schemaName;
    }

    @Override
    public String getStep() {
        return String.format("drop group %s on %s", groupName, schemaName);
    }

}
