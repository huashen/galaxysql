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
package com.alibaba.polardbx.druid.bvt.sql.mysql;

import com.alibaba.polardbx.druid.sql.MysqlTest;
import com.alibaba.polardbx.druid.sql.ast.SQLCommentHint;
import com.alibaba.polardbx.druid.sql.ast.SQLStatement;
import com.alibaba.polardbx.druid.sql.ast.TDDLHint;
import com.alibaba.polardbx.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.polardbx.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.polardbx.druid.sql.parser.SQLParserFeature;

import java.util.List;

public class Coronadb_hints_test_2_sql_delay_cutoff extends MysqlTest {

    public void test_0() throws Exception {
        String sql = "/!TDDL:SQL_DELAY_CUTOFF=5*/ select * from t";

        MySqlStatementParser parser = new MySqlStatementParser(sql, SQLParserFeature.TDDLHint);
        List<SQLStatement> statementList = parser.parseStatementList();
        SQLStatement stmt = statementList.get(0);

        assertEquals(1, statementList.size());

        assertEquals("/*TDDL:SQL_DELAY_CUTOFF=5*/\n" +
                "SELECT *\n" +
                "FROM t", stmt.toString());

        MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
        stmt.accept(visitor);

        assertEquals(1, visitor.getTables().size());
        assertEquals(1, visitor.getColumns().size());
        assertEquals(0, visitor.getConditions().size());
        assertEquals(0, visitor.getOrderByColumns().size());

        List<SQLCommentHint> hints = stmt.getHeadHintsDirect();

        TDDLHint hint = (TDDLHint) hints.get(0);
        assertEquals("SQL_DELAY_CUTOFF", hint.getFunctions().get(0).getName());
    }

    public void test_1() throws Exception {
        String sql = "/!TDDL:SLAVE AND SQL_DELAY_CUTOFF=5*/ select * from t";

        MySqlStatementParser parser = new MySqlStatementParser(sql, SQLParserFeature.TDDLHint);
        List<SQLStatement> statementList = parser.parseStatementList();
        SQLStatement stmt = statementList.get(0);

        assertEquals(1, statementList.size());

        assertEquals("/*TDDL:SLAVE AND SQL_DELAY_CUTOFF=5*/\n" +
                "SELECT *\n" +
                "FROM t", stmt.toString());

        MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
        stmt.accept(visitor);

        assertEquals(1, visitor.getTables().size());
        assertEquals(1, visitor.getColumns().size());
        assertEquals(0, visitor.getConditions().size());
        assertEquals(0, visitor.getOrderByColumns().size());

        List<SQLCommentHint> hints = stmt.getHeadHintsDirect();

        TDDLHint hint = (TDDLHint) hints.get(0);
        assertEquals("SLAVE", hint.getFunctions().get(0).getName());
        assertEquals("SQL_DELAY_CUTOFF", hint.getFunctions().get(1).getName());
    }
}
