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

import com.alibaba.polardbx.optimizer.core.planner.rule.util.CBOUtil;
import com.alibaba.polardbx.optimizer.memory.MemoryEstimator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.alibaba.polardbx.optimizer.config.meta.CostModelWeight;
import com.alibaba.polardbx.optimizer.core.DrdsConvention;
import com.alibaba.polardbx.optimizer.core.MppConvention;
import org.apache.calcite.plan.DeriveMode;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptCost;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.PhysicalNode;
import org.apache.calcite.rel.RelCollation;
import org.apache.calcite.rel.RelDistribution;
import org.apache.calcite.rel.RelDistributions;
import org.apache.calcite.rel.RelInput;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelWriter;
import org.apache.calcite.rel.core.CorrelationId;
import org.apache.calcite.rel.core.JoinInfo;
import org.apache.calcite.rel.core.JoinRelType;
import org.apache.calcite.rel.core.SemiJoin;
import org.apache.calcite.rel.externalize.RelDrdsWriter;
import org.apache.calcite.rel.externalize.RexExplainVisitor;
import org.apache.calcite.rel.logical.LogicalSemiJoin;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.util.ImmutableIntList;
import org.apache.calcite.util.Pair;

import java.util.List;
import java.util.Set;

public class SemiHashJoin extends SemiJoin implements PhysicalNode {

    private SqlOperator operator;
    private RelNode pushDownRelNode;
    private List<RexNode> operands;
    private String subqueryPosition;
    private RelOptCost fixedCost;
    private final RexNode equalCondition;
    private final RexNode otherCondition;
    private final boolean runtimeFilterPushedDown;

    // ~ Constructors -----------------------------------------------------------

    public SemiHashJoin(
        RelOptCluster cluster,
        RelTraitSet traitSet,
        RelNode left,
        RelNode right,
        RexNode condition,
        ImmutableIntList leftKeys,
        ImmutableIntList rightKeys,
        JoinRelType joinRelType,
        List<RexNode> operands,
        Set<CorrelationId> variablesSet,
        SqlNodeList hints,
        SqlOperator operator,
        RelNode pushDownRelNode,
        String subqueryPosition,
        RexNode equalCondition,
        RexNode otherCondition,
        boolean runtimeFilterPushedDown
    ) {
        super(
            cluster,
            traitSet,
            left,
            right,
            condition,
            leftKeys,
            rightKeys,
            variablesSet,
            joinRelType,
            hints);
        assert traitSet.containsIfApplicable(DrdsConvention.INSTANCE)
            || traitSet.containsIfApplicable(MppConvention.INSTANCE);
        this.operands = operands;
        this.operator = operator;
        this.pushDownRelNode = pushDownRelNode;
        this.subqueryPosition = subqueryPosition;
        this.equalCondition = equalCondition;
        this.otherCondition = otherCondition;
        this.runtimeFilterPushedDown = runtimeFilterPushedDown;
    }

    public SemiHashJoin(
        RelOptCluster cluster,
        RelTraitSet traitSet,
        RelNode left,
        RelNode right,
        RexNode condition,
        ImmutableIntList leftKeys,
        ImmutableIntList rightKeys,
        JoinRelType joinRelType,
        List<RexNode> operands,
        Set<CorrelationId> variablesSet,
        SqlNodeList hints,
        SqlOperator operator,
        RelNode pushDownRelNode,
        String subqueryPosition,
        RexNode equalCondition,
        RexNode otherCondition
    ) {
        super(
            cluster,
            traitSet,
            left,
            right,
            condition,
            leftKeys,
            rightKeys,
            variablesSet,
            joinRelType,
            hints);
        assert traitSet.containsIfApplicable(DrdsConvention.INSTANCE)
            || traitSet.containsIfApplicable(MppConvention.INSTANCE);
        this.operands = operands;
        this.operator = operator;
        this.pushDownRelNode = pushDownRelNode;
        this.subqueryPosition = subqueryPosition;
        this.equalCondition = equalCondition;
        this.otherCondition = otherCondition;
        this.runtimeFilterPushedDown = false;
    }

    public SemiHashJoin(RelInput relInput) {
        super(relInput.getCluster(),
            relInput.getTraitSet(),
            relInput.getInputs().get(0),
            relInput.getInputs().get(1),
            relInput.getExpression("condition"),
            JoinInfo.of(relInput.getInputs().get(0), relInput.getInputs().get(1),
                relInput.getExpression("condition")).leftKeys,
            JoinInfo.of(relInput.getInputs().get(0), relInput.getInputs().get(1),
                relInput.getExpression("condition")).rightKeys,
            ImmutableSet.<CorrelationId>of(),
            JoinRelType.valueOf(relInput.getString("joinType")),
            null);
        this.traitSet = this.traitSet.replace(DrdsConvention.INSTANCE);
        this.equalCondition = relInput.getExpression("equalCondition");
        this.otherCondition = relInput.getExpression("otherCondition");
        if (relInput.get("operands") == null) {
            this.operands = ImmutableList.of();
        } else {
            this.operands = relInput.getExpressionList("operands");
        }
        this.runtimeFilterPushedDown = relInput.getBoolean("insertRf", false);
    }

    public static SemiHashJoin create(
        RelTraitSet traitSet,
        RelNode left,
        RelNode right,
        RexNode condition,
        LogicalSemiJoin semiJoin,
        RexNode equalCondition,
        RexNode otherCondition) {
        final RelOptCluster cluster = left.getCluster();
        final JoinInfo joinInfo = JoinInfo.of(left, right, condition);
        return new SemiHashJoin(
            cluster,
            traitSet,
            left,
            right,
            condition,
            joinInfo.leftKeys,
            joinInfo.rightKeys,
            semiJoin.getJoinType(),
            semiJoin.getOperands(),
            semiJoin.getVariablesSet(),
            semiJoin.getHints(),
            semiJoin.getOperator(),
            semiJoin.getPushDownRelNode(),
            semiJoin.getSubqueryPosition(),
            equalCondition,
            otherCondition);
    }

    @Override
    public SemiHashJoin copy(
        RelTraitSet traitSet,
        RexNode condition,
        RelNode left,
        RelNode right,
        JoinRelType joinType,
        boolean semiJoinDone) {
        final JoinInfo joinInfo = JoinInfo.of(left, right, condition);
        SemiHashJoin semiHashJoin =
            new SemiHashJoin(
                getCluster(),
                traitSet,
                left,
                right,
                condition,
                joinInfo.leftKeys,
                joinInfo.rightKeys,
                joinType,
                operands,
                variablesSet,
                hints,
                operator,
                pushDownRelNode,
                subqueryPosition,
                equalCondition,
                otherCondition,
                runtimeFilterPushedDown
            );
        semiHashJoin.setFixedCost(fixedCost);
        return semiHashJoin;
    }

    public SemiHashJoin copy(
        RelTraitSet traitSet,
        RexNode condition,
        RelNode left,
        RelNode right,
        JoinRelType joinType,
        boolean semiJoinDone,
        boolean runtimeFilterPushedDown) {
        final JoinInfo joinInfo = JoinInfo.of(left, right, condition);
        SemiHashJoin semiHashJoin =
            new SemiHashJoin(
                getCluster(),
                traitSet,
                left,
                right,
                condition,
                joinInfo.leftKeys,
                joinInfo.rightKeys,
                joinType,
                operands,
                variablesSet,
                hints,
                operator,
                pushDownRelNode,
                subqueryPosition,
                equalCondition,
                otherCondition,
                runtimeFilterPushedDown
            );
        semiHashJoin.setFixedCost(fixedCost);
        return semiHashJoin;
    }

    public boolean isRuntimeFilterPushedDown() {
        return runtimeFilterPushedDown;
    }

    public void setFixedCost(RelOptCost fixedCost) {
        this.fixedCost = fixedCost;
    }

    public RelOptCost getFixedCost() {
        return fixedCost;
    }

    @Override
    public RelOptCost computeSelfCost(RelOptPlanner planner, RelMetadataQuery mq) {
        if (fixedCost != null) {
            return fixedCost;
        }
        final double leftRowCount = mq.getRowCount(left);
        final double rightRowCount = mq.getRowCount(right);

        double rowCount = leftRowCount + rightRowCount;
        double buildWeight = CostModelWeight.INSTANCE.getBuildWeight();
        double probeWeight = CostModelWeight.INSTANCE.getProbeWeight();
        double driveSideRowCount = leftRowCount;
        double anotherSideRowCount = rightRowCount;

        double cpu = buildWeight * anotherSideRowCount + probeWeight * driveSideRowCount;
        double memory = MemoryEstimator.estimateRowSizeInHashTable(right.getRowType()) * anotherSideRowCount;

        return planner.getCostFactory().makeCost(rowCount, cpu, memory, 0, 0);
    }

    public RexNode getEqualCondition() {
        return equalCondition;
    }

    public RexNode getOtherCondition() {
        return otherCondition;
    }

    @Override
    public RelWriter explainTerms(RelWriter pw) {
        return super.explainTerms(pw)
            .itemIf("equalCondition", equalCondition, equalCondition != null)
            .itemIf("otherCondition", otherCondition, otherCondition != null)
            .itemIf("operands", operands, operands != null && !operands.isEmpty())
            .itemIf("insertRf", runtimeFilterPushedDown, runtimeFilterPushedDown);
    }

    @Override
    public RelWriter explainTermsForDisplay(RelWriter pw) {
        String name = "SemiHashJoin";
        pw.item(RelDrdsWriter.REL_NAME, name);

        RexExplainVisitor visitor = new RexExplainVisitor(this);
        condition.accept(visitor);
        return pw.item("condition", visitor.toSqlString())
            .item("type", joinType.name().toLowerCase())
            .itemIf("systemFields", getSystemFieldList(), !getSystemFieldList().isEmpty());
    }

    public List<RexNode> getOperands() {
        return operands;
    }

    @Override
    public Pair<RelTraitSet, List<RelTraitSet>> passThroughTraits(
        final RelTraitSet required) {
        return CBOUtil.passThroughTraitsForJoin(
            required, this, joinType, left.getRowType().getFieldCount(), getTraitSet());
    }

    @Override
    public Pair<RelTraitSet, List<RelTraitSet>> deriveTraits(
        final RelTraitSet childTraits, final int childId) {
        return HashJoin.deriveTraitsForJoin(childTraits, childId, getTraitSet(), left, right);
    }

    @Override
    public DeriveMode getDeriveMode() {
        return DeriveMode.LEFT_FIRST;
    }
}
