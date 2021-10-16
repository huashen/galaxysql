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

package org.apache.calcite.sql;

import java.util.Arrays;
import java.util.List;

import org.apache.calcite.sql.parser.SqlParserPos;

/**
 * @author chenmo.cm
 * @date 2019/1/23 12:07 AM
 */
public class SqlDropPrimaryKey extends SqlAlterSpecification {
    private static final SqlOperator OPERATOR =
        new SqlSpecialOperator("DROP PRIMARY KEY", SqlKind.DROP_PRIMARY_KEY);

    /**
     * Creates a SqlAlterTableDropIndex.
     *
     * @param tableName
     * @param pos
     */
    public SqlDropPrimaryKey(SqlIdentifier tableName, String sql, SqlParserPos pos) {
        super(pos);
        this.tableName = tableName;
        this.originTableName = tableName;
        this.sourceSql = sql;
    }

    private SqlNode tableName;
    final private SqlIdentifier originTableName;
    final private String sourceSql;

    @Override
    public SqlOperator getOperator() {
        return OPERATOR;
    }

    @Override
    public List<SqlNode> getOperandList() {
        return Arrays.asList(tableName);
    }

    public void setTargetTable(SqlNode tableName) {
        this.tableName = tableName;
    }

    @Override
    public void unparse(SqlWriter writer, int leftPrec, int rightPrec) {
        final SqlWriter.Frame frame = writer.startList(SqlWriter.FrameTypeEnum.SELECT, "DROP PRIMARY KEY", "");

        writer.endList(frame);
    }

    public SqlNode getTableName() {
        return tableName;
    }

    public String getSourceSql() {
        return sourceSql;
    }

    public SqlIdentifier getOriginTableName() {
        return originTableName;
    }
}
