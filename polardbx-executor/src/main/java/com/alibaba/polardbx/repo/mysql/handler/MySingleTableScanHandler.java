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

import com.alibaba.polardbx.common.model.SqlType;
import com.alibaba.polardbx.executor.cursor.AbstractCursor;
import com.alibaba.polardbx.executor.cursor.Cursor;
import com.alibaba.polardbx.executor.cursor.impl.LogicalViewResultCursor;
import com.alibaba.polardbx.executor.handler.HandlerCommon;
import com.alibaba.polardbx.executor.spi.IRepository;
import com.alibaba.polardbx.optimizer.context.ExecutionContext;
import com.alibaba.polardbx.optimizer.core.rel.DirectTableOperation;
import com.alibaba.polardbx.optimizer.core.rel.PhyTableOperation;
import com.alibaba.polardbx.optimizer.core.rel.SingleTableOperation;
import com.alibaba.polardbx.optimizer.parse.SqlTypeUtils;
import com.alibaba.polardbx.statistics.RuntimeStatistics;
import org.apache.calcite.rel.RelNode;

/**
 * Created by chuanqin on 17/7/5.
 */
public class MySingleTableScanHandler extends HandlerCommon {

    public MySingleTableScanHandler(IRepository repo) {
        super(repo);
    }

    @Override
    public Cursor handle(RelNode logicalPlan, ExecutionContext executionContext) {
        Cursor cursor = repo.getCursorFactory().repoCursor(executionContext, logicalPlan);
        if (logicalPlan instanceof SingleTableOperation) {
            cursor = new LogicalViewResultCursor((AbstractCursor) cursor, executionContext, true);
        } else if (logicalPlan instanceof DirectTableOperation) {
            SqlType sqlType = executionContext.getSqlType();
            if (sqlType != null && SqlTypeUtils.isSelectSqlType(sqlType)) {
                cursor = new LogicalViewResultCursor((AbstractCursor) cursor, executionContext, true);
            }
        } else if (logicalPlan instanceof PhyTableOperation) {
            RuntimeStatistics runtimeStat = (RuntimeStatistics) executionContext.getRuntimeStatistics();
            if (runtimeStat != null && runtimeStat.isFromAllAtOnePhyTable()) {
                SqlType sqlType = executionContext.getSqlType();
                if (sqlType != null && SqlTypeUtils.isSelectSqlType(sqlType)) {
                    cursor = new LogicalViewResultCursor((AbstractCursor) cursor, executionContext, true);
                }
            }
        }
        return cursor;
    }
}
