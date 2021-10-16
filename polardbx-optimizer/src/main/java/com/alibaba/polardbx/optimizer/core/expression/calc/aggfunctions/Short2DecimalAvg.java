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

package com.alibaba.polardbx.optimizer.core.expression.calc.aggfunctions;

import com.alibaba.polardbx.common.datatype.Decimal;
import com.alibaba.polardbx.optimizer.chunk.Block;
import com.alibaba.polardbx.optimizer.core.datatype.DataType;

public class Short2DecimalAvg extends SpecificType2DecimalAvg {
    public Short2DecimalAvg(int index, boolean isDistict, DataType inputType, DataType outputType, int filterArg) {
        super(index, isDistict, inputType, outputType, filterArg);
    }

    @Override
    protected Decimal getDecimal(Block block, int position) {
        return Decimal.fromLong(block.getShort(position));
    }
}
