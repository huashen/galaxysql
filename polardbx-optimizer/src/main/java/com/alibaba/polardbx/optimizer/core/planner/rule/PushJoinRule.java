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

import com.alibaba.polardbx.optimizer.utils.PlannerUtils;
import com.alibaba.polardbx.optimizer.utils.RelUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.alibaba.polardbx.common.model.sqljep.Comparative;
import com.alibaba.polardbx.common.properties.ConnectionParams;
import com.alibaba.polardbx.common.properties.ParamManager;
import com.alibaba.polardbx.common.utils.Pair;
import com.alibaba.polardbx.common.utils.TStringUtil;
import com.alibaba.polardbx.optimizer.PlannerContext;
import com.alibaba.polardbx.optimizer.core.rel.LogicalIndexScan;
import com.alibaba.polardbx.optimizer.core.rel.LogicalView;
import com.alibaba.polardbx.optimizer.partition.PartitionInfo;
import com.alibaba.polardbx.optimizer.rule.TddlRuleManager;
import com.alibaba.polardbx.optimizer.sharding.ConditionExtractor;
import com.alibaba.polardbx.optimizer.sharding.result.ExtractionResult;
import com.alibaba.polardbx.rule.TableRule;
import org.apache.calcite.plan.RelOptPredicateList;
import org.apache.calcite.plan.RelOptRule;
import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.plan.RelOptRuleOperand;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Join;
import org.apache.calcite.rel.core.JoinRelType;
import org.apache.calcite.rel.logical.LogicalJoin;
import org.apache.calcite.rel.metadata.RelMdPredicates;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.rex.RexUtil;
import org.apache.calcite.sql.SqlKind;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

/**
 * @author lingce.ldm
 */
public class PushJoinRule extends RelOptRule {

    public PushJoinRule(RelOptRuleOperand operand, String description) {
        super(operand, "PushJoinRule:" + description);
    }

    public static final PushJoinRule INSTANCE = new PushJoinRule(
        operand(LogicalJoin.class, some(operand(LogicalView.class, none()), operand(LogicalView.class, none()))),
        "INSTANCE");

    @Override
    public void onMatch(RelOptRuleCall call) {
        final LogicalJoin join = (LogicalJoin) call.rels[0];
        final LogicalView leftView = (LogicalView) call.rels[1];
        final LogicalView rightView = (LogicalView) call.rels[2];

        final ParamManager paramManager = PlannerContext.getPlannerContext(join).getParamManager();
        boolean enablePushJoin = paramManager.getBoolean(ConnectionParams.ENABLE_PUSH_JOIN);
        if (!enablePushJoin) {
            return;
        }

        RexNode joinCondition = join.getCondition();
        if (joinCondition.isAlwaysTrue() || joinCondition.isAlwaysFalse()) {
            return;
        }

        tryPushJoin(call, join, leftView, rightView, joinCondition);
    }

    protected boolean pushable(RelOptRuleCall call, RelNode rel, LogicalView leftView, LogicalView rightView) {
        /**
         * Across different schemaNames, DO NOT push.
         */
        if (!StringUtils.equalsIgnoreCase(leftView.getSchemaName(), rightView.getSchemaName())) {
            return false;
        }

        String schemaName = leftView.getSchemaName();

        TddlRuleManager tddlRuleManager =
            PlannerContext.getPlannerContext(leftView).getExecutionContext().getSchemaManager(schemaName)
                .getTddlRuleManager();
        Join join = (Join) rel;
        /**
         * ????????????????????????
         */
        JoinRelType joinType = join.getJoinType();

        // ??????????????????????????????????????????????????????LeftView?????????
        String leftTable = leftView.getShardingTable();
        String rightTable = rightView.getShardingTable();
        final PlannerContext plannerContext = PlannerContext.getPlannerContext(join);
        final Map<String, Object> extraCmds = plannerContext.getExtraCmds();
        String pushPolicy = null;
        if (!extraCmds.containsKey(ConnectionParams.PUSH_POLICY.getName())) {
            pushPolicy = ConnectionParams.ConnectionParamValues.PUSH_POLICY_FULL;
        }
        Object policy = extraCmds
            .get(ConnectionParams.PUSH_POLICY.getName());

        if (policy != null) {
            pushPolicy = String.valueOf(policy);
        }

        if (StringUtils.isEmpty(pushPolicy) ||
            ConnectionParams.ConnectionParamValues.PUSH_POLICY_NO.equalsIgnoreCase(pushPolicy)) {
            return false;
        }

        // ????????????????????????????????????????????????????????????????????????????????????
        final List<Integer> lShardColumnRef = new ArrayList<>();
        final List<Integer> rShardColumnRef = new ArrayList<>();
        if (leftView.isNewPartDbTbl() && rightView.isNewPartDbTbl()) {
            PartitionInfo leftPartitionInfo = tddlRuleManager.getPartitionInfoManager().getPartitionInfo(leftTable);
            PartitionInfo rightPartitionInfo = tddlRuleManager.getPartitionInfoManager().getPartitionInfo(rightTable);

            switch (joinType) {
            case INNER:
                if (leftPartitionInfo.isBroadcastTable() || rightPartitionInfo.isBroadcastTable()) {
                    return true;
                }
                break;
            case LEFT:
            case ANTI:
            case SEMI:
            case LEFT_SEMI:
                if (rightPartitionInfo.isBroadcastTable()) {
                    return true;
                }
                break;
            case RIGHT:
                if (leftPartitionInfo.isBroadcastTable()) {
                    return true;
                }
                break;
            case FULL:
                return false;
            default:
                // pass
            }

            if (leftPartitionInfo.getTableGroupId() == null || rightPartitionInfo.getTableGroupId() == null) {
                return false;
            } else if (!leftPartitionInfo.getTableGroupId().equals(rightPartitionInfo.getTableGroupId())) {
                return false;
            }

            if (leftPartitionInfo.isSingleTable() && rightPartitionInfo.isSingleTable()) {
                return true;
            }

            getShardColumnRef(call, leftView, leftTable, leftPartitionInfo, rightView, rightTable, rightPartitionInfo,
                lShardColumnRef,
                rShardColumnRef);

        } else if (!leftView.isNewPartDbTbl() && !rightView.isNewPartDbTbl()) {

            if (ConnectionParams.ConnectionParamValues.PUSH_POLICY_BROADCAST.equals(pushPolicy)
                || ConnectionParams.ConnectionParamValues.PUSH_POLICY_FULL.equals(pushPolicy)) {
                /**
                 * ????????????????????????????????????
                 */
                if (pushOneBroadCast(joinType, leftTable, rightTable, tddlRuleManager)) {
                    return true;
                }

                /**
                 * ????????????/????????????????????????????????????broadcast?????????
                 */
                if (pushAllSingle(leftTable, rightTable, tddlRuleManager)) {
                    return true;
                }

                if (!ConnectionParams.ConnectionParamValues.PUSH_POLICY_FULL.equalsIgnoreCase(pushPolicy)) {
                    return false;
                }

            }

            /**
             * ??????????????????????????????????????????????????????
             */
            TableRule ltr = tddlRuleManager.getTableRule(leftTable);
            TableRule rtr = tddlRuleManager.getTableRule(rightTable);
            if (!PlannerUtils.tableRuleIsIdentical(ltr, rtr)) {
                return false;
            }

            getShardColumnRef(call, leftView, leftTable, ltr, rightView, rightTable, rtr, lShardColumnRef,
                rShardColumnRef);
        } else {
            return false;
        }

        /**
         * search filter and join node that already being push down. union their condition to filters.
         */
        return findShardColumnMatch(rel, rel, lShardColumnRef, rShardColumnRef);
    }

    protected void tryPushJoin(RelOptRuleCall call, RelNode rel, LogicalView leftView, LogicalView rightView,
                               RexNode joinCondition) {

        /**
         * ????????????????????????
         */
        if (!pushable(call, rel, leftView, rightView)) {
            return;
        }

        Join join = (Join) rel;
        JoinRelType joinType = join.getJoinType();
        List<RexNode> filters = RelOptUtil.conjunctions(joinCondition);

        // ???????????????????????????ON????????????????????????????????????????????????????????????????????????
        List<RexNode> leftFilters = new ArrayList<>();
        List<RexNode> rightFilters = new ArrayList<>();
        RelOptUtil.classifyFilters(join,
            filters,
            joinType,
            false,
            !joinType.generatesNullsOnRight(),
            !joinType.generatesNullsOnLeft(),
            null,
            leftFilters,
            rightFilters);

        /**
         * ??????????????????
         */
        RexBuilder rB = join.getCluster().getRexBuilder();
        RelMdPredicates.JoinConditionBasedPredicateInference jI =
            new RelMdPredicates.JoinConditionBasedPredicateInference(join,
                RexUtil.composeConjunction(rB, leftFilters, false),
                RexUtil.composeConjunction(rB, rightFilters, false));

        RelOptPredicateList preds = jI.inferPredicates(false);

        if (preds.leftInferredPredicates.size() > 0) {
            leftFilters.addAll(preds.leftInferredPredicates);
        }
        if (preds.rightInferredPredicates.size() > 0) {
            rightFilters.addAll(preds.rightInferredPredicates);
        }

        perform(call, leftFilters, rightFilters, preds);
    }

    private boolean findShardColumnMatch(RelNode root, RelNode join,
                                         List<Integer> lShardColumnRef,
                                         List<Integer> rShardColumnRefForJoin) {
        if (lShardColumnRef.size() == 0 || lShardColumnRef.size() != rShardColumnRefForJoin.size()) {
            return false;
        }

        final ExtractionResult er = ConditionExtractor.columnEquivalenceFrom(root).extract();

        Map<Integer, BitSet> connectionMap = er.conditionOf(join).toColumnEquality();

        for (int i = 0; i < lShardColumnRef.size(); i++) {
            Integer lShard = lShardColumnRef.get(i);
            Integer targetShard = rShardColumnRefForJoin.get(i);
            BitSet b = connectionMap.get(lShard);
            if (b.get(targetShard)) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Get shard column index on join rowType for left and right child of join
     */
    protected void getShardColumnRef(RelOptRuleCall call, LogicalView leftView,
                                     String leftTable, TableRule ltr, LogicalView rightView,
                                     String rightTable, TableRule rtr, List<Integer> lShardColumnRef,
                                     List<Integer> rShardColumnRefForJoin) {
        if (null != lShardColumnRef) {
            lShardColumnRef.addAll(getRefByColumnName(leftView, leftTable, ltr.getShardColumns(), true));
        }

        if (null != rShardColumnRefForJoin) {
            List<Integer> rShardColumnRef = getRefByColumnName(rightView, rightTable, rtr.getShardColumns(), false);
            for (int rColRef : rShardColumnRef) {
                rShardColumnRefForJoin.add(leftView.getRowType().getFieldCount() + rColRef);
            }
        }
    }

    protected void getShardColumnRef(RelOptRuleCall call, LogicalView leftView,
                                     String leftTable, PartitionInfo lPartitionInfo, LogicalView rightView,
                                     String rightTable, PartitionInfo rPartitionInfo, List<Integer> lShardColumnRef,
                                     List<Integer> rShardColumnRefForJoin) {
        if (null != lShardColumnRef) {
            lShardColumnRef.addAll(getRefByColumnName(leftView, leftTable, lPartitionInfo.getPartitionColumns(), true));
        }

        if (null != rShardColumnRefForJoin) {
            List<Integer> rShardColumnRef =
                getRefByColumnName(rightView, rightTable, rPartitionInfo.getPartitionColumns(), false);
            for (int rColRef : rShardColumnRef) {
                rShardColumnRefForJoin.add(leftView.getRowType().getFieldCount() + rColRef);
            }
        }
    }

    /**
     * ????????????????????????????????????
     */
    protected List<Pair<Integer, Integer>> filterMeetEqualCondition(RexBuilder rexBuilder, List<RexNode> joinFilters) {
        List<Pair<Integer, Integer>> inputRef = new ArrayList<>();

        // to CNF
        RexNode newFilter = RexUtil.composeConjunction(rexBuilder, joinFilters, true);
        if (newFilter == null) {
            return null;
        }

        RexNode cnf = RexUtil.toCnf(rexBuilder, newFilter);

        // ??????????????????????????????
        if (joinFilters.isEmpty()) {
            return inputRef;
        }

        for (RexNode rex : RelOptUtil.conjunctions(cnf)) {
            if (!(rex instanceof RexCall)) {
                continue;
            }

            RexCall joinCall = (RexCall) rex;
            // ???????????????????????????
            if (joinCall.getKind() != SqlKind.EQUALS) {
                continue;
            }

            RexNode leftOp = joinCall.getOperands().get(0);
            RexNode rightOp = joinCall.getOperands().get(1);
            if (leftOp instanceof RexInputRef && rightOp instanceof RexInputRef) {
                int leftRef = ((RexInputRef) leftOp).getIndex();
                int rightRef = ((RexInputRef) rightOp).getIndex();
                /**
                 * ????????????key???????????????????????????value?????????????????????
                 */
                if (leftRef > rightRef) {
                    inputRef.add(new Pair<>(rightRef, leftRef));
                } else {
                    inputRef.add(new Pair<>(leftRef, rightRef));
                }
            }
        }
        return inputRef;
    }

    protected boolean shardColumnMatched(List<Pair<Integer, Integer>> joinCol, List<Integer> lShardCol,
                                         List<Integer> rShardCol) {
        if (lShardCol.size() != rShardCol.size()) {
            return false;
        }
        if (joinCol == null) {
            return false;
        }

        for (int i = 0; i < lShardCol.size(); i++) {
            int lCol = lShardCol.get(i);
            int rCol = rShardCol.get(i);

            List<Integer> compareList = Lists.newArrayList();
            compareList.add(lCol);
            findPairList(lCol, joinCol, compareList);
            if (compareList.contains(rCol)) {
                continue;
            }
            return false;
        }

        return true;
    }

    private void findPairList(Integer i, List<Pair<Integer, Integer>> joinCol, List<Integer> compareList) {
        for (Pair<Integer, Integer> p : joinCol) {
            if (p.getKey() == i && !compareList.contains(p.getValue())) {
                compareList.add(p.getValue());
                findPairList(p.getValue(), joinCol, compareList);
            }
            if (p.getValue() == i && !compareList.contains(p.getKey())) {
                compareList.add(p.getKey());
                findPairList(p.getKey(), joinCol, compareList);
            }

        }
    }

    /**
     * ???????????????????????????????????????????????????
     */
    protected List<Integer> getRefByColumnName(LogicalView logicalView, String tableName, List<String> columns,
                                               boolean last) {
        List<Integer> refs = new ArrayList<>();
        for (String col : columns) {
            int ref = logicalView.getRefByColumnName(tableName, col, last);

            /**
             * ???LogicalView ????????????????????????????????????,????????????
             */
            if (ref == -1) {
                return ListUtils.EMPTY_LIST;
            }
            refs.add(ref);
        }
        return refs;
    }

    /**
     * Just push it.
     */
    protected void perform(RelOptRuleCall call, List<RexNode> leftFilters,
                           List<RexNode> rightFilters, RelOptPredicateList relOptPredicateList) {
//        final LogicalJoin join = (LogicalJoin) call.rels[0];
//        LogicalView leftView = (LogicalView) call.rels[1];
//        final LogicalView rightView = (LogicalView) call.rels[2];
//
//        if (rightView instanceof LogicalIndexScan && !(leftView instanceof LogicalIndexScan)) {
//            leftView = new LogicalIndexScan(leftView);
//        }
//
//        leftView.pushJoin(join, rightView, leftFilters, rightFilters);
//        RelUtils.changeRowType(leftView, join.getRowType());
//        call.transformTo(leftView);

        final LogicalJoin join = (LogicalJoin) call.rels[0];
        LogicalView leftView = (LogicalView) call.rels[1];
        final LogicalView rightView = (LogicalView) call.rels[2];

        if (rightView instanceof LogicalIndexScan && !(leftView instanceof LogicalIndexScan)) {
            leftView = new LogicalIndexScan(leftView);
        }

        LogicalView newLeftView = leftView.copy(leftView.getTraitSet());
        LogicalView newRightView = rightView.copy(rightView.getTraitSet());

        LogicalJoin newLogicalJoin = join.copy(
            join.getTraitSet(),
            join.getCondition(),
            newLeftView,
            newRightView,
            join.getJoinType(),
            join.isSemiJoinDone());

        newLeftView.pushJoin(newLogicalJoin, newRightView, leftFilters, rightFilters);
        RelUtils.changeRowType(newLeftView, join.getRowType());
        call.transformTo(newLeftView);
    }

    protected boolean pushAllSingle(String leftTable, String rightTable, TddlRuleManager or) {
        final String shardingTable = allSingleTableCanPush(leftTable, rightTable, or);

        if (TStringUtil.isNotBlank(shardingTable)) {
            return true;
        }

        return false;
    }

    /**
     * ?????????????????????????????????
     */
    protected String allSingleTableCanPush(String leftTable, String rightTable, TddlRuleManager or) {
        // ???????????????????????????
        if (or.isTableInSingleDb(leftTable) && or.isTableInSingleDb(rightTable)) {
            return rightTable;
        }

        // ???????????????
        if (or.isBroadCast(leftTable) && or.isBroadCast(rightTable)) {
            return rightTable;
        }

        // ??????????????????????????????
        if (or.isTableInSingleDb(leftTable) && or.isBroadCast(rightTable)) {
            return leftTable;
        }

        if (or.isBroadCast(leftTable) && or.isTableInSingleDb(rightTable)) {
            return rightTable;
        }

        return null;
    }

    protected boolean pushOneBroadCast(JoinRelType joinType, String leftTable, String rightTable, TddlRuleManager or) {
        final String shardingTable = oneBroadCastTableCanPush(joinType, leftTable, rightTable, or);
        if (TStringUtil.isNotBlank(shardingTable)) {
            return true;
        }

        return false;
    }

    /**
     * ???????????????????????????????????????????????????
     */
    protected String oneBroadCastTableCanPush(JoinRelType joinType, String leftTable, String rightTable,
                                              TddlRuleManager or) {
        /**
         * inner join, ???????????????????????????????????????
         */
        if (joinType == JoinRelType.INNER) {
            if (or.isBroadCast(leftTable)) {
                return rightTable;
            } else if (or.isBroadCast(rightTable)) {
                return leftTable;
            }
        }

        /**
         * left join?????????????????????
         */
        if (joinType == JoinRelType.LEFT && (or.isBroadCast(rightTable))) {
            return leftTable;
        }

        /**
         * right join, ??????????????????
         */
        if (joinType == JoinRelType.RIGHT && (or.isBroadCast(leftTable))) {
            return rightTable;
        }

        if ((joinType == JoinRelType.SEMI || joinType == JoinRelType.ANTI || joinType == JoinRelType.LEFT_SEMI)
            && or.isBroadCast(rightTable)) {
            return rightTable;
        }

        return null;
    }

    /**
     * ????????????????????????, ?????????????????????????????????????????????
     */
    protected boolean inferEqualJoin(RexBuilder builder, RelDataType lDataType, RelDataType rDataType,
                                     List<RexNode> leftFilters, List<RexNode> rightFilters, List<String> lShardCol,
                                     List<String> rShardCol) {
        Preconditions.checkArgument(lShardCol.size() == rShardCol.size());

        List<Comparative> lComps = buildComparative(builder, lDataType, leftFilters, lShardCol);
        List<Comparative> rComps = buildComparative(builder, rDataType, rightFilters, rShardCol);

        if (lComps == null || rComps == null) {
            return false;
        }

        if (lComps.size() != rComps.size()) {
            return false;
        }

        for (int i = 0; i < lComps.size(); i++) {
            if (!lComps.get(i).equals(rComps.get(i))) {
                return false;
            }
        }

        return true;
    }

    private List<Comparative> buildComparative(RexBuilder builder, RelDataType rowType, List<RexNode> filters,
                                               List<String> cols) {
        List<Comparative> comps = new ArrayList<>();
        RexNode newLeftFilter = RexUtil.composeConjunction(builder, filters, true);
        for (String col : cols) {
            Comparative comp = TddlRuleManager.getComparative(newLeftFilter, rowType, col, null);
            if (comp == null) {
                return null;
            }

            comps.add(comp);
        }
        return comps;
    }
}
