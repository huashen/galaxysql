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

package com.alibaba.polardbx.optimizer.core.rel;

import com.alibaba.polardbx.common.jdbc.ParameterContext;
import com.alibaba.polardbx.common.utils.Pair;
import com.alibaba.polardbx.optimizer.context.ExecutionContext;
import com.alibaba.polardbx.optimizer.core.dialect.DbType;
import com.alibaba.polardbx.optimizer.utils.PlannerUtils;
import com.alibaba.polardbx.optimizer.utils.RelUtils;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelWriter;
import org.apache.calcite.rel.externalize.RelDrdsWriter;
import org.apache.calcite.sql.SqlNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenmo.cm
 */
public class PhyQueryOperation extends BaseQueryOperation {
    private Map<Integer, ParameterContext> param;

    public PhyQueryOperation(RelOptCluster cluster, RelTraitSet traitSet, SqlNode nativeSqlNode, String dbIndex,
                             Map<Integer, ParameterContext> param) {
        this(cluster, traitSet, nativeSqlNode, dbIndex, param, PlannerUtils.getDynamicParamIndex(nativeSqlNode));
    }

    public PhyQueryOperation(RelOptCluster cluster, RelTraitSet traitSet, SqlNode nativeSqlNode, String dbIndex,
                             Map<Integer, ParameterContext> param, List<Integer> dynamicParamIndex) {
        super(cluster, traitSet, RelUtils.toNativeSqlLine(nativeSqlNode), nativeSqlNode, DbType.MYSQL);
        this.dbIndex = dbIndex;
        this.param = new HashMap<>();

        // fix unmatched param index (e.g. limit ?, ?)
        int index = 1;
        for (Integer i : dynamicParamIndex) {
            final ParameterContext parameterContext = param.get(i + 1);
            this.param.put(index, PlannerUtils.changeParameterContextIndex(parameterContext, index));
            index++;
        }
    }

    @Override
    public Pair<String, Map<Integer, ParameterContext>> getDbIndexAndParam(Map<Integer, ParameterContext> param,
                                                                           ExecutionContext executionContext) {

        return Pair.of(this.dbIndex, this.param);
    }

    @Override
    public RelWriter explainTermsForDisplay(RelWriter pw) {
        pw.item(RelDrdsWriter.REL_NAME, getExplainName());
        pw.item("node", dbIndex);
        pw.item("sql", this.sqlTemplate);
        return pw;
    }

    public Map<Integer, ParameterContext> getParam() {
        return param;
    }

    public void setParam(Map<Integer, ParameterContext> param) {
        this.param = param;
    }
}
