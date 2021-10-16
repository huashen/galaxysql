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

package com.alibaba.polardbx.optimizer.core.planner.rule;

import com.alibaba.polardbx.optimizer.core.DrdsConvention;
import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.convert.ConverterRule;
import org.apache.calcite.rel.logical.LogicalUnion;

public class DrdsUnionConvertRule extends ConverterRule {
    public static final DrdsUnionConvertRule INSTANCE = new DrdsUnionConvertRule();

    DrdsUnionConvertRule() {
        super(LogicalUnion.class, Convention.NONE, DrdsConvention.INSTANCE, "DrdsUnionConvertRule");
    }

    @Override
    public Convention getOutConvention() {
        return DrdsConvention.INSTANCE;
    }

    @Override
    public RelNode convert(RelNode rel) {
        final LogicalUnion union = (LogicalUnion) rel;
        final RelTraitSet traitSet = union.getTraitSet().simplify().replace(DrdsConvention.INSTANCE);
        if (!union.all) {
            LogicalUnion newUnion = union.copy(traitSet, convertList(union.getInputs(), DrdsConvention.INSTANCE), true);
            return RelOptUtil.createDistinctRel(newUnion);
        }
        return union.copy(traitSet, convertList(union.getInputs(), DrdsConvention.INSTANCE), union.all);
    }
}