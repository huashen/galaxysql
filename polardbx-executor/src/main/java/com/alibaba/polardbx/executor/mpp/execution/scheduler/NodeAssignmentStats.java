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

package com.alibaba.polardbx.executor.mpp.execution.scheduler;

import com.alibaba.polardbx.executor.mpp.execution.NodeTaskMap;
import com.alibaba.polardbx.executor.mpp.execution.RemoteTask;
import com.alibaba.polardbx.gms.node.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public final class NodeAssignmentStats {

    private final NodeTaskMap nodeTaskMap;
    private final Map<Node, Integer> assignmentCount = new HashMap<>();
    private final Map<Node, Integer> splitCountByNode = new HashMap<>();
    private final Map<String, Integer> queuedSplitCountByNode = new HashMap<>();

    public NodeAssignmentStats(NodeTaskMap nodeTaskMap, List<Node> nodeMap, List<RemoteTask> existingTasks) {
        this.nodeTaskMap = requireNonNull(nodeTaskMap, "nodeTaskMap is null");

        // pre-populate the assignment counts with zeros. This makes getOrDefault() faster
        for (Node node : nodeMap) {
            assignmentCount.put(node, 0);
        }

        for (RemoteTask task : existingTasks) {
            checkArgument(queuedSplitCountByNode.put(task.getNodeId(), task.getQueuedPartitionedSplitCount()) == null,
                "A single stage may not have multiple tasks running on the same node");
        }
    }

    public int getTotalSplitCount(Node node) {
        return assignmentCount.getOrDefault(node, 0) + splitCountByNode
            .computeIfAbsent(node, nodeTaskMap::getPartitionedSplitsOnNode);
    }

    public int getQueuedSplitCountForStage(Node node) {
        return queuedSplitCountByNode.getOrDefault(node.getNodeIdentifier(), 0) + assignmentCount.getOrDefault(node, 0);
    }

    public void addAssignedSplit(Node node) {
        assignmentCount.merge(node, 1, (x, y) -> x + y);
    }
}
