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

package com.alibaba.polardbx.gms.partition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A logical table, contains all partitions information
 *
 * @author chenghui.lch
 */
public class TablePartRecordInfoContext {

    private TablePartitionRecord logTbRec = null;
    private List<TablePartitionRecord> partitionRecList = null;
    private List<TablePartitionRecord> subPartitionRecList = null;
    /**
     * key : the name of top level partition
     * values: subpartitions
     */
    private Map<String, List<TablePartitionRecord>> subPartitionRecMap = null;

    public TablePartRecordInfoContext() {
        partitionRecList = new ArrayList<>();
        subPartitionRecList = new ArrayList<>();
        subPartitionRecMap = new HashMap<>();
    }

    public String getTableName() {
        return this.logTbRec.tableName;
    }

    public List<TablePartitionRecord> getPartitionRecList() {
        return partitionRecList;
    }

    public List<TablePartitionRecord> filterPartitions(Predicate<TablePartitionRecord> pred) {
        return this.partitionRecList.stream().filter(pred).collect(Collectors.toList());
    }

    public void setPartitionRecList(List<TablePartitionRecord> partitionRecList) {
        this.partitionRecList = partitionRecList;
    }

    public TablePartitionRecord getLogTbRec() {
        return logTbRec;
    }

    public void setLogTbRec(TablePartitionRecord logTbRec) {
        this.logTbRec = logTbRec;
    }

    public void setSubPartitionRecList(List<TablePartitionRecord> subPartitionRecList) {
        this.subPartitionRecList = subPartitionRecList;
    }

    public List<TablePartitionRecord> getSubPartitionRecList() {
        return subPartitionRecList;
    }

    public Map<String, List<TablePartitionRecord>> getSubPartitionRecMap() {
        return subPartitionRecMap;
    }

    public void setSubPartitionRecMap(
        Map<String, List<TablePartitionRecord>> subPartitionRecMap) {
        this.subPartitionRecMap = subPartitionRecMap;
    }

    private static boolean filterPartitionByGroupId(TablePartitionRecord p, long pgId) {
        return p.groupId == pgId &&
            p.partLevel != TablePartitionRecord.PARTITION_LEVEL_LOGICAL_TABLE;
    }

    /**
     * Get partition at a specific position
     */
    public TablePartitionRecord getPartitionByPosition(int pos) {
        // TODO(moyi): build an index in memory to avoid sequential find
        for (TablePartitionRecord p : this.partitionRecList) {
            if (p.partPosition == pos) {
                return p;
            }
        }
        return null;
    }

    public List<TablePartitionRecord> getPartitionRecListByGroupId(long pgId) {
        return getPartitionRecList().stream()
            .filter(p -> filterPartitionByGroupId(p, pgId))
            .collect(Collectors.toList());
    }
}
