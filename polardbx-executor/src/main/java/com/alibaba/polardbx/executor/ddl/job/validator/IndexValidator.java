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

package com.alibaba.polardbx.executor.ddl.job.validator;

import com.alibaba.polardbx.common.exception.TddlRuntimeException;
import com.alibaba.polardbx.common.exception.code.ErrorCode;
import com.alibaba.polardbx.executor.ddl.job.meta.delegate.TableInfoManagerDelegate;
import com.alibaba.polardbx.gms.metadb.limit.LimitValidator;
import com.alibaba.polardbx.gms.metadb.table.TableInfoManager;
import org.apache.calcite.sql.SqlAddIndex;
import org.apache.calcite.sql.SqlAlterSpecification;
import org.apache.calcite.sql.SqlAlterTable;
import org.apache.calcite.sql.SqlCreateTable;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlIndexDefinition;
import org.apache.calcite.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class IndexValidator {

    public static void validateIndexNameLengths(SqlCreateTable createTable) {
        List<Pair<SqlIdentifier, SqlIndexDefinition>> keys = new ArrayList<>();
        if (null != createTable.getKeys()) {
            keys.addAll(createTable.getKeys());
        }
        if (null != createTable.getGlobalKeys()) {
            keys.addAll(createTable.getGlobalKeys());
        }
        if (null != createTable.getClusteredKeys()) {
            keys.addAll(createTable.getClusteredKeys());
        }
        if (!keys.isEmpty()) {
            for (org.apache.calcite.util.Pair<SqlIdentifier, SqlIndexDefinition> key : keys) {
                if (key != null) {
                    if (key.getKey() != null) {
                        LimitValidator.validateIndexNameLength(key.getKey().getLastName());
                    } else if (key.getValue().getIndexName() != null) {
                        LimitValidator.validateIndexNameLength(key.getValue().getIndexName().getLastName());
                    }
                }
            }
        }
    }

    public static void validateIndexNameLengths(SqlAlterTable sqlAlterTable) {
        List<SqlAlterSpecification> alterItems = sqlAlterTable.getAlters();
        if (alterItems != null && alterItems.size() > 0) {
            for (SqlAlterSpecification alterItem : alterItems) {
                if (alterItem instanceof SqlAddIndex) {
                    SqlAddIndex index = (SqlAddIndex) alterItem;
                    if (index.getIndexName() != null) {
                        LimitValidator.validateIndexNameLength(index.getIndexName().getLastName());
                    } else {
                        SqlIndexDefinition indexDefinition = index.getIndexDef();
                        if (indexDefinition.getIndexName() != null) {
                            LimitValidator.validateIndexNameLength(indexDefinition.getIndexName().getLastName());
                        }
                    }
                }
            }
        }
    }

    public static void validateIndexNameLength(String indexName) {
        LimitValidator.validateIndexNameLength(indexName);
    }

    public static void validateIndexExistence(String schemaName, String logicalTableName, String indexName) {
        if (!checkIfIndexExists(schemaName, logicalTableName, indexName)) {
            throw new TddlRuntimeException(ErrorCode.ERR_EXECUTOR,
                "Index '" + indexName + "' on table '" + logicalTableName + "' doesn't exist");
        }
    }

    public static void validateIndexNonExistence(String schemaName, String logicalTableName, String indexName) {
        if (checkIfIndexExists(schemaName, logicalTableName, indexName)) {
            throw new TddlRuntimeException(ErrorCode.ERR_EXECUTOR,
                "Index '" + indexName + "' on table '" + logicalTableName + "' already exists");
        }
    }

    public static boolean checkIfIndexExists(String schemaName, String logicalTableName, String indexName) {
        return new TableInfoManagerDelegate<Boolean>(new TableInfoManager()) {
            @Override
            protected Boolean invoke() {
                return tableInfoManager.checkIfIndexExists(schemaName, logicalTableName, indexName);
            }
        }.execute();
    }

}
