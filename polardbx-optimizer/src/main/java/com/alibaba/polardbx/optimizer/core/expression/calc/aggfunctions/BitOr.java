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

import com.alibaba.polardbx.optimizer.chunk.Block;
import com.alibaba.polardbx.optimizer.chunk.BlockBuilder;
import com.alibaba.polardbx.optimizer.chunk.Chunk;
import com.alibaba.polardbx.optimizer.core.datatype.DataType;
import com.alibaba.polardbx.optimizer.core.expression.calc.AbstractAggregator;
import com.alibaba.polardbx.optimizer.state.NullableObjectGroupState;

/**
 * Created by chuanqin on 17/12/7.
 */
public class BitOr extends AbstractAggregator {

    private NullableObjectGroupState groupState;

    public BitOr(int index, DataType inputTypes, DataType outType, int filterArg) {
        super(new int[] {index}, false, new DataType[] {inputTypes}, outType, filterArg);
    }

    @Override
    public void open(int capacity) {
        groupState = new NullableObjectGroupState(capacity);
    }

    @Override
    public void appendInitValue() {
        groupState.append(returnType.convertFrom(0));
    }

    @Override
    public void resetToInitValue(int groupId) {
        groupState.set(groupId, returnType.convertFrom(0));
    }

    @Override
    public void accumulate(int groupId, Chunk chunk, int position) {
        Block block = chunk.getBlock(aggIndexInChunk[0]);
        if (block.isNull(position)) {
            return;
        }

        Object value = block.getObject(position);
        Object beforeValue = groupState.get(groupId);
        groupState.set(groupId, returnType.getCalculator().bitOr(value, beforeValue));
    }

    @Override
    public void writeResultTo(int groupId, BlockBuilder bb) {
        bb.writeObject(groupState.get(groupId));
    }

    @Override
    public long estimateSize() {
        return groupState.estimateSize();
    }
}