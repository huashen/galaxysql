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

import com.alibaba.polardbx.common.utils.GeneralUtil;
import com.alibaba.polardbx.optimizer.context.ExecutionContext;
import com.alibaba.polardbx.optimizer.core.datatype.DataType;
import com.alibaba.polardbx.optimizer.core.datatype.DataTypeUtil;
import com.alibaba.polardbx.optimizer.core.datatype.DataTypes;
import com.alibaba.polardbx.optimizer.core.function.calc.AbstractScalarFunction;
import com.alibaba.polardbx.optimizer.utils.FunctionUtils;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.List;

/**
 * Created by chuanqin on 18/3/14.
 */
public class FromBase64 extends AbstractScalarFunction {
    public FromBase64(List<DataType> operandTypes, DataType resultType) {
        super(operandTypes, resultType);
    }

    @Override
    public Object compute(Object[] args, ExecutionContext ec) {
        if (FunctionUtils.isNull(args[0])) {
            return null;
        }
        String decodedStr = null;
        String str = DataTypeUtil.convert(operandTypes.get(0), DataTypes.StringType, args[0]);
        try {
            byte[] base64decodedBytes = Base64.getDecoder().decode(str);
            decodedStr = new String(base64decodedBytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            GeneralUtil.nestedException("Convert from base64 failed, encoded str is :" + str);
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
            return null;
        }
        return decodedStr;
    }

    @Override
    public String[] getFunctionNames() {
        return new String[] {"FROM_BASE64"};
    }
}
