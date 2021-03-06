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

package com.alibaba.polardbx.server.response;

import com.alibaba.polardbx.CobarServer;
import com.alibaba.polardbx.config.SchemaConfig;
import com.alibaba.polardbx.executor.cursor.ResultCursor;
import com.alibaba.polardbx.executor.cursor.impl.ArrayResultCursor;
import com.alibaba.polardbx.executor.sync.ISyncAction;
import com.alibaba.polardbx.matrix.jdbc.TDataSource;
import com.alibaba.polardbx.optimizer.core.datatype.DataTypes;
import com.alibaba.polardbx.stats.MatrixStatistics;

public class ShowWorkLoadSyncAction implements ISyncAction {

    private String schema;

    public ShowWorkLoadSyncAction(String schema) {
        this.schema = schema;
    }

    public ShowWorkLoadSyncAction() {

    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public ResultCursor sync() {

        ArrayResultCursor result = new ArrayResultCursor("workload");
        result.addColumn("TP", DataTypes.LongType);
        result.addColumn("AP", DataTypes.LongType);
        result.addColumn("LOCAL", DataTypes.LongType);
        result.addColumn("CLUSTER", DataTypes.LongType);

        result.initMeta();

        SchemaConfig schemaConfig = CobarServer.getInstance().getConfig().getSchemas().get(schema);

        if (schemaConfig != null && schemaConfig.getDataSource().isInited()) {
            TDataSource ds = schemaConfig.getDataSource();
            MatrixStatistics stats = ds.getStatistics();
            result.addRow(new Object[] {
                stats.tpLoad,
                stats.apLoad,
                stats.local,
                stats.cluster});
        }
        return result;

    }

}
