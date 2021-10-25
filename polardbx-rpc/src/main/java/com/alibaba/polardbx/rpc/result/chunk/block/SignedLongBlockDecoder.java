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

package com.alibaba.polardbx.rpc.result.chunk.block;

import com.google.protobuf.CodedInputStream;
import com.mysql.cj.polarx.protobuf.PolarxResultset;
import com.alibaba.polardbx.rpc.result.chunk.AbstractBlockDecoder;
import com.alibaba.polardbx.rpc.result.chunk.Decimal;
import com.alibaba.polardbx.rpc.result.chunk.Slice;

/**
 * @version 1.0
 */
public class SignedLongBlockDecoder extends AbstractBlockDecoder {

    private long currentValue;

    public SignedLongBlockDecoder(PolarxResultset.Chunk chunk, int columnIndex,
                                  PolarxResultset.ColumnMetaData meta) {
        super(chunk, columnIndex, meta);
        this.currentValue = 0;
    }

    @Override
    public boolean next() throws Exception {
        final boolean hasNext = super.next();
        if (hasNext) {
            if (currentNull) {
                currentValue = 0;
            } else {
                currentValue = CodedInputStream.decodeZigZag64(stream.readInt64());
            }
            return true;
        }
        return false;
    }

    @Override
    public Object getObject() throws Exception {
        return currentNull ? null : getLong();
    }

    @Override
    public long getLong() throws Exception {
        assert !currentNull; // Just assert for performance.
        return currentValue;
    }

    @Override
    public float getFloat() throws Exception {
        assert !currentNull;
        return currentValue;
    }

    @Override
    public double getDouble() throws Exception {
        assert !currentNull;
        return currentValue;
    }

    @Override
    public Decimal getDecimal() throws Exception {
        assert !currentNull;
        return new Decimal(currentValue, 0);
    }

    @Override
    public Slice getString() throws Exception {
        if (currentNull) {
            return null;
        }
        final String str = Long.toString(currentValue);
        final byte[] bytes = str.getBytes();
        return new Slice(bytes, 0, bytes.length);
    }
}
