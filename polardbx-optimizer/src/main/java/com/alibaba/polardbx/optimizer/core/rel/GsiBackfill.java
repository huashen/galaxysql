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

import com.alibaba.polardbx.optimizer.context.ExecutionContext;
import com.alibaba.polardbx.optimizer.core.planner.SqlConverter;
import com.google.common.collect.ImmutableList;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.AbstractRelNode;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * Used to backfill data from base table to index table when a secondary global
 * index is created.
 */
public class GsiBackfill extends AbstractRelNode {

    private String schemaName;
    private String baseTableName;
    private List<String> indexNames; // Add one column and backfill one multi clustered index.
    private List<String> columns;

    public static GsiBackfill createGsiBackfill(String schemaName, String baseTableName, String indexName,
                                                ExecutionContext ec) {
        final RelOptCluster cluster = SqlConverter.getInstance(schemaName, ec).createRelOptCluster(null);
        RelTraitSet traitSet = RelTraitSet.createEmpty();
        return new GsiBackfill(cluster, traitSet, schemaName, baseTableName, indexName);
    }

    public static GsiBackfill createGsiAddColumnsBackfill(String schemaName, String baseTableName,
                                                          List<String> indexNames, List<String> columns,
                                                          ExecutionContext ec) {
        final RelOptCluster cluster = SqlConverter.getInstance(ec).createRelOptCluster(null);
        RelTraitSet traitSet = RelTraitSet.createEmpty();
        return new GsiBackfill(cluster, traitSet, schemaName, baseTableName, indexNames, columns);
    }

    public GsiBackfill(RelOptCluster cluster, RelTraitSet traitSet, String schemaName, String baseTableName,
                       String indexName) {
        super(cluster, traitSet);
        this.schemaName = schemaName;
        this.baseTableName = baseTableName;
        this.indexNames = ImmutableList.of(indexName);
        this.columns = null;
    }

    public GsiBackfill(RelOptCluster cluster, RelTraitSet traitSet, String schemaName, String baseTableName,
                       List<String> indexNames, List<String> columns) {
        super(cluster, traitSet);
        this.schemaName = schemaName;
        this.baseTableName = baseTableName;
        this.indexNames = indexNames;
        this.columns = columns;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getBaseTableName() {
        return baseTableName;
    }

    public void setBaseTableName(String baseTableName) {
        this.baseTableName = baseTableName;
    }

    public List<String> getIndexNames() {
        return indexNames;
    }

    public void setIndexNames(List<String> indexNames) {
        this.indexNames = indexNames;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public boolean isAddColumnsBackfill() {
        return CollectionUtils.isNotEmpty(columns);
    }

}
