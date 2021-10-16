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

package com.alibaba.polardbx.executor.ddl.job.builder;

import com.alibaba.polardbx.optimizer.OptimizerContext;
import com.alibaba.polardbx.optimizer.config.table.TableMeta;
import com.alibaba.polardbx.optimizer.context.ExecutionContext;
import com.alibaba.polardbx.optimizer.core.rel.ddl.data.RenameTablePreparedData;
import com.alibaba.polardbx.optimizer.partition.PartitionInfoUtil;
import com.alibaba.polardbx.optimizer.utils.TableTopologyUtil;
import org.apache.calcite.rel.core.DDL;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class RenamePartitionTableBuilder extends RenameTableBuilder {

    public RenamePartitionTableBuilder(DDL ddl, RenameTablePreparedData preparedData,
                                       ExecutionContext executionContext) {
        super(ddl, preparedData, executionContext);
    }

    @Override
    public void buildTableRuleAndTopology() {
        partitionInfo = OptimizerContext.getContext(preparedData.getSchemaName()).getPartitionInfoManager()
            .getPartitionInfo(preparedData.getTableName());
        tableTopology = PartitionInfoUtil.buildTargetTablesFromPartitionInfo(partitionInfo);
        fillRenamePhyTable(tableTopology, preparedData.getNewTableName(), preparedData.getTableName());
    }
}
