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

package com.alibaba.polardbx.optimizer.core.function.calc.scalar.math;

import com.alibaba.polardbx.optimizer.context.ExecutionContext;
import com.alibaba.polardbx.optimizer.core.datatype.DataType;
import com.alibaba.polardbx.optimizer.core.datatype.DataTypes;
import com.alibaba.polardbx.optimizer.core.function.calc.AbstractScalarFunction;
import com.alibaba.polardbx.optimizer.utils.FunctionUtils;

import java.util.List;

/**
 * Returns the natural logarithm of X; that is, the base-e logarithm of X. If X
 * is less than or equal to 0, then NULL is returned.
 *
 * <pre>
 * mysql> SELECT LN(2);
 *         -> 0.69314718055995
 * mysql> SELECT LN(-2);
 *         -> NULL
 * </pre>
 * <p>
 * This function is synonymous with LOG(X). The inverse of this function is the
 * EXP() function.
 *
 * @author jianghang 2014-4-14 下午10:20:59
 * @since 5.0.7
 */
public class Ln extends AbstractScalarFunction {
    public Ln(List<DataType> operandTypes, DataType resultType) {
        super(operandTypes, resultType);
    }

    @Override
    public Object compute(Object[] args, ExecutionContext ec) {
        if (FunctionUtils.isNull(args[0])) {
            return null;
        }

        Double d = DataTypes.DoubleType.convertFrom(args[0]);
        if (d <= 0) {
            return null;
        } else {
            return Math.log(d);
        }
    }

    @Override
    public String[] getFunctionNames() {
        return new String[] {"LN"};
    }
}
