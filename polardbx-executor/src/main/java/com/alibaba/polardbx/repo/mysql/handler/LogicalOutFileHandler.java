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

import com.alibaba.polardbx.common.exception.TddlRuntimeException;
import com.alibaba.polardbx.common.exception.code.ErrorCode;
import com.alibaba.polardbx.common.properties.ConnectionParams;
import com.alibaba.polardbx.executor.ExecutorHelper;
import com.alibaba.polardbx.executor.cursor.Cursor;
import com.alibaba.polardbx.executor.cursor.impl.OutFileCursor;
import com.alibaba.polardbx.executor.handler.HandlerCommon;
import com.alibaba.polardbx.executor.mpp.deploy.ServiceProvider;
import com.alibaba.polardbx.executor.spi.IRepository;
import com.alibaba.polardbx.optimizer.context.ExecutionContext;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.logical.LogicalOutFile;

public class LogicalOutFileHandler extends HandlerCommon {
    public LogicalOutFileHandler(IRepository repo) {
        super(repo);
    }

    @Override
    public Cursor handle(RelNode logicalPlan, ExecutionContext executionContext) {
        if (!executionContext.getParamManager().getBoolean(ConnectionParams.ENABLE_SELECT_INTO_OUTFILE)) {
            throw new TddlRuntimeException(ErrorCode.ERR_OPERATION_NOT_ALLOWED,
                "Selecting into outfile is not enabled");
        }
        if (logicalPlan.getInputs().size() != 1) {
            throw new TddlRuntimeException(ErrorCode.ERR_DATA_OUTPUT,
                "OutFileCursor cannot be implented when the inputs more than one");
        }
        RelNode inputRelNode = logicalPlan.getInput(0);
        Cursor cursor = ExecutorHelper.execute(inputRelNode, executionContext, false);
        return new OutFileCursor(executionContext, ServiceProvider.getInstance().getServer().getSpillerFactory(),
            cursor, ((LogicalOutFile) logicalPlan).getOutFileParams());
    }
}
