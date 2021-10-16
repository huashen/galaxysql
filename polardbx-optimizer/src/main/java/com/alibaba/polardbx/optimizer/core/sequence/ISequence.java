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

package com.alibaba.polardbx.optimizer.core.sequence;

import com.alibaba.polardbx.optimizer.utils.ExplainResult;

/**
 * sequence执行计划
 *
 * @author agapple 2014年12月18日 下午6:45:23
 * @since 5.1.17
 */
public interface ISequence<RT extends ISequence> {

    public enum SEQUENCE_DDL_TYPE {
        GET_SEQUENCE, CREATE_SEQUENCE, DROP_SEQUENCE, ALTER_SEQUENCE, RENAME_SEQUENCE;
    }

    SEQUENCE_DDL_TYPE getSequenceDdlType();

    /**
     * 用于输出带缩进的字符串
     */
    public String toStringWithInden(int inden, ExplainResult.ExplainMode mode);

    public String getSql();

    String getName();

    void setName(String name);

    void setSchemaName(String schemaName);

    String getSchemaName();
}
