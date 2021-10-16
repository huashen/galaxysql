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

package com.alibaba.polardbx.optimizer.core.sequence.bean;

import com.alibaba.polardbx.common.constants.SequenceAttribute;
import com.alibaba.polardbx.common.constants.SequenceAttribute.Type;

/**
 * @author chensr 2016年12月1日 下午9:53:22
 * @since 5.0.0
 */
public class CreateTimeBasedSequence extends CreateSequenceOpt {

    public CreateTimeBasedSequence(String name) {
        super(name);
        this.type = Type.TIME;
    }

    @Override
    public String getSql() {
        return String.format(super.getSql(),
            name,
            String.valueOf(0),
            String.valueOf(0),
            String.valueOf(0),
            String.valueOf(0),
            String.valueOf(SequenceAttribute.TIME_BASED));
    }

}
