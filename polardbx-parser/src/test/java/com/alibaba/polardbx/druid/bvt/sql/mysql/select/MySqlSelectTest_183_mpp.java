/*
 * Copyright 1999-2017 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.polardbx.druid.bvt.sql.mysql.select;

import com.alibaba.polardbx.druid.sql.MysqlTest;
import com.alibaba.polardbx.druid.sql.SQLUtils;
import com.alibaba.polardbx.druid.sql.ast.SQLStatement;
import com.alibaba.polardbx.druid.sql.ast.statement.SQLSelect;
import com.alibaba.polardbx.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.polardbx.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.polardbx.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.polardbx.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.polardbx.druid.sql.parser.SQLParserFeature;

import java.util.List;

public class MySqlSelectTest_183_mpp extends MysqlTest {

    public void test_0() throws Exception {
        String sql = "SELECT \"cid\", 3 + \"f2\" FROM \"wenyu_meta_test\".\"WENYU_META_TEST_02\" LIMIT 4";

        MySqlStatementParser parser = new MySqlStatementParser(sql, SQLParserFeature.KeepNameQuotes);
        List<SQLStatement> statementList = parser.parseStatementList();
        SQLStatement stmt = statementList.get(0);

        SQLSelectStatement selectStmt = (SQLSelectStatement) stmt;

        SQLSelect select = selectStmt.getSelect();
        assertNotNull(select.getQuery());
        MySqlSelectQueryBlock queryBlock = (MySqlSelectQueryBlock) select.getQuery();
        assertNull(queryBlock.getOrderBy());

//        print(statementList);

        System.out.println(stmt.toString());

        assertEquals(1, statementList.size());

        MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
        stmt.accept(visitor);

        System.out.println("Tables : " + visitor.getTables());
        System.out.println("fields : " + visitor.getColumns());
//        System.out.println("coditions : " + visitor.getConditions());
//        System.out.println("orderBy : " + visitor.getOrderByColumns());

        assertEquals(1, visitor.getTables().size());
        assertEquals(2, visitor.getColumns().size());
        assertEquals(0, visitor.getConditions().size());
        assertEquals(0, visitor.getOrderByColumns().size());

        assertTrue(visitor.containsTable("wenyu_meta_test.WENYU_META_TEST_02"));
        assertTrue(visitor.containsColumn("wenyu_meta_test.WENYU_META_TEST_02", "cid"));

        String output = SQLUtils.toMySqlString(stmt);
        assertEquals("SELECT \"cid\", 3 + \"f2\"\n" +
                        "FROM \"wenyu_meta_test\".\"WENYU_META_TEST_02\"\n" +
                        "LIMIT 4", //
                            output);

        assertEquals("wenyu_meta_test.WENYU_META_TEST_02", visitor.getTables().entrySet().iterator().next().getKey().getName());
        assertEquals("cid", visitor.getColumns().iterator().next().getName());
    }
}
