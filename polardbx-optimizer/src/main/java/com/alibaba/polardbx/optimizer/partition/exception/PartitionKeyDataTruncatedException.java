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

package com.alibaba.polardbx.optimizer.partition.exception;

import com.alibaba.polardbx.common.exception.TddlRuntimeException;
import com.alibaba.polardbx.common.exception.code.ErrorCode;
import com.alibaba.polardbx.optimizer.core.field.TypeConversionStatus;
import com.alibaba.polardbx.optimizer.partition.pruning.PartFieldAccessType;

/**
 * @author chenghui.lch
 */
public class PartitionKeyDataTruncatedException extends TddlRuntimeException {

    protected PartFieldAccessType accessType;
    protected TypeConversionStatus status;

    public PartitionKeyDataTruncatedException(PartFieldAccessType accessType, TypeConversionStatus status) {
        super(ErrorCode.ERR_PARTITION_KEY_DATA_TRUNCATED,
            String.format("invalid type conversion[%s] in [%s] ", accessType.getSqlTypeDesc(), status.toString()));
        this.accessType = accessType;
        this.status = status;
    }

    public PartFieldAccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(PartFieldAccessType accessType) {
        this.accessType = accessType;
    }

    public TypeConversionStatus getStatus() {
        return status;
    }

    public void setStatus(TypeConversionStatus status) {
        this.status = status;
    }
}
