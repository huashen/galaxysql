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

package com.alibaba.polardbx.optimizer.core.function.calc.scalar.string;

import com.alibaba.polardbx.optimizer.context.ExecutionContext;
import com.alibaba.polardbx.optimizer.core.datatype.DataType;
import com.alibaba.polardbx.optimizer.core.datatype.DataTypes;
import com.alibaba.polardbx.optimizer.core.function.calc.AbstractScalarFunction;
import com.alibaba.polardbx.optimizer.utils.FunctionUtils;
import io.airlift.slice.Slice;

import java.math.BigInteger;
import java.util.List;

/**
 * <pre>
 * BIN(N)
 *
 * Returns a string representation of the binary value of N, where N is a longlong (BIGINT) number. This is equivalent to CONV(N,10,2). Returns NULL if N is NULL.
 *
 * mysql> SELECT BIN(12);
 *         -> '1100'
 * </pre>
 *
 * @author mengshi.sunmengshi 2014年4月11日 下午1:29:30
 * @since 5.1.0
 */
public class Bin extends AbstractScalarFunction {
    public Bin(List<DataType> operandTypes, DataType resultType) {
        super(operandTypes, resultType);
    }

    @Override
    public String[] getFunctionNames() {
        return new String[] {"BIN"};
    }

    @Override
    public Object compute(Object[] args, ExecutionContext ec) {
        Object arg = args[0];

        if (FunctionUtils.isNull(arg)) {
            return null;
        }

        if (arg instanceof String || arg instanceof Slice) {
            String s = DataTypes.StringType.convertFrom(arg);
            if (s.length() == 0) {
                return null;
            }
        }

        BigInteger longlong = (BigInteger)DataTypes.ULongType.convertJavaFrom(arg);
        if (longlong == null) {
            return "0";
        }

        return longlong.toString(2);
    }
}
