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

package com.alibaba.polardbx.executor.sync;

import com.alibaba.polardbx.executor.cursor.ResultCursor;
import com.alibaba.polardbx.executor.gms.GmsTableMetaManager;
import com.alibaba.polardbx.optimizer.OptimizerContext;
import com.alibaba.polardbx.optimizer.config.table.SchemaManager;

/**
 * drop table失效对应的表结构cache
 *
 * @author agapple 2015年3月26日 下午8:29:04
 * @since 5.1.19
 */
public class DropTableSyncAction implements ISyncAction {

    private String schemaName;
    private String tableName;

    public DropTableSyncAction() {

    }

    public DropTableSyncAction(String schemaName, String tableName) {
        this.schemaName = schemaName;
        this.tableName = tableName;
    }

    @Override
    public ResultCursor sync() {
        syncForPolarDbX();
        return null;
    }

    private void syncForPolarDbX() {
        SchemaManager oldSchemaManager = OptimizerContext.getContext(schemaName).getLatestSchemaManager();
        ((GmsTableMetaManager) oldSchemaManager).tonewversion(tableName);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

}
