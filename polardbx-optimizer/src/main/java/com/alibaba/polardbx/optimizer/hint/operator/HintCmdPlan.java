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

package com.alibaba.polardbx.optimizer.hint.operator;

import com.alibaba.polardbx.common.properties.ConnectionProperties;
import com.alibaba.polardbx.optimizer.context.ExecutionContext;
import com.alibaba.polardbx.optimizer.utils.RelUtils;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlNode;

import java.util.List;

public class HintCmdPlan extends BaseHintOperator implements HintCmdOperator {

    private String externalizePlan;

    public HintCmdPlan(SqlBasicCall hint, ExecutionContext ec) {
        super(hint, ec);
        if (this.argMap.size() == 1) {
            final SqlNode value = this.argMap.entrySet().iterator().next().getValue();
            if (null != value) {
                externalizePlan = RelUtils.stringValue(value);
            }
        }
    }

    @Override
    protected List<HintArgKey> getArgKeys() {
        return HintArgKey.PLAN_HINT;
    }

    @Override
    public CmdBean handle(CmdBean current) {
        current.getExtraCmd().put(ConnectionProperties.PLAN, externalizePlan);
        return current;
    }

}
