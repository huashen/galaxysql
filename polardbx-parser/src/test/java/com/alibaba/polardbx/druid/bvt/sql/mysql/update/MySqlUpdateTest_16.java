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
package com.alibaba.polardbx.druid.bvt.sql.mysql.update;

import com.alibaba.polardbx.druid.sql.MysqlTest;
import com.alibaba.polardbx.druid.sql.SQLUtils;
import com.alibaba.polardbx.druid.sql.ast.SQLExpr;
import com.alibaba.polardbx.druid.sql.ast.SQLStatement;
import com.alibaba.polardbx.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.polardbx.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.polardbx.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.polardbx.druid.stat.TableStat.Column;

import java.util.List;

public class MySqlUpdateTest_16 extends MysqlTest {

    public void test_0() throws Exception {
        String sql = "update am_activity_prize set lock_left_count=lock_left_count-120.0, lock_count=lock_count+0.0 where id=?";

        MySqlStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> statementList = parser.parseStatementList();
        SQLStatement stmt = statementList.get(0);
        print(statementList);

        assertEquals(1, statementList.size());

        MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
        stmt.accept(visitor);

//        System.out.println("Tables : " + visitor.getTables());
        System.out.println("fields : " + visitor.getColumns());
//        System.out.println("coditions : " + visitor.getConditions());
//        System.out.println("orderBy : " + visitor.getOrderByColumns());

        assertEquals(1, visitor.getTables().size());
        assertEquals(3, visitor.getColumns().size());
        // assertEquals(2, visitor.getConditions().size());

        assertTrue(visitor.containsTable("am_activity_prize"));


        final Column column = visitor.getColumn("am_activity_prize", "lock_left_count");
        assertNotNull(column);
        assertTrue(column.isUpdate());

        {
            String output = SQLUtils.toMySqlString(stmt);
            assertEquals("UPDATE am_activity_prize\n" +
                            "SET lock_left_count = lock_left_count - 120.0, lock_count = lock_count + 0.0\n" +
                            "WHERE id = ?", //
                                output);
        }
        {
            String output = SQLUtils.toMySqlString(stmt, SQLUtils.DEFAULT_LCASE_FORMAT_OPTION);
            assertEquals("update am_activity_prize\n" +
                            "set lock_left_count = lock_left_count - 120.0, lock_count = lock_count + 0.0\n" +
                            "where id = ?", //
                                output);
        }


        {
            SQLUpdateStatement update = (SQLUpdateStatement) stmt;
            SQLExpr where = update.getWhere();
            assertEquals("id = ?", where.toString());
        }

    }
}
