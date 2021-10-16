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
package com.alibaba.polardbx.druid.bvt.sql.mysql.show;

import com.alibaba.polardbx.druid.sql.MysqlTest;
import com.alibaba.polardbx.druid.sql.SQLUtils;
import com.alibaba.polardbx.druid.sql.ast.SQLStatement;
import com.alibaba.polardbx.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.polardbx.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import org.junit.Assert;

import java.util.List;

public class MySqlShowTestMaterializedView extends MysqlTest {

    public void test_0() throws Exception {
        String sql = "SHOW MATERIALIZED VIEWS ";

        MySqlStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> statementList = parser.parseStatementList();
        SQLStatement stmt = statementList.get(0);

        Assert.assertEquals(1, statementList.size());

        assertEquals("SHOW MATERIALIZED VIEWS", stmt.toString());

        MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
        stmt.accept(visitor);

        Assert.assertEquals(0, visitor.getTables().size());
        Assert.assertEquals(0, visitor.getColumns().size());
        Assert.assertEquals(0, visitor.getConditions().size());
        Assert.assertEquals(0, visitor.getOrderByColumns().size());
    }

    public void test_1() throws Exception {
        String sql = "SHOW MATERIALIZED VIEWS like 'namexxx'";

        MySqlStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> statementList = parser.parseStatementList();
        SQLStatement stmt = statementList.get(0);

        Assert.assertEquals(1, statementList.size());

        assertEquals("SHOW MATERIALIZED VIEWS LIKE 'namexxx'", stmt.toString());

        MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
        stmt.accept(visitor);

        Assert.assertEquals(0, visitor.getTables().size());
        Assert.assertEquals(0, visitor.getColumns().size());
        Assert.assertEquals(0, visitor.getConditions().size());
        Assert.assertEquals(0, visitor.getOrderByColumns().size());
    }

    public void test_2() throws Exception {
        SQLStatement view = SQLUtils.parseSingleMysqlStatement("SHOW CREATE MATERIALIZED VIEW myview");
        assertEquals("SHOW CREATE MATERIALIZED VIEW myview", view.toString());

        SQLStatement view1 = SQLUtils.parseSingleMysqlStatement("SHOW MATERIALIZED VIEWS");
        assertEquals("SHOW MATERIALIZED VIEWS", view1.toString());

        SQLStatement view2 = SQLUtils.parseSingleMysqlStatement("SHOW MATERIALIZED VIEWS LIKE '%_%'");
        assertEquals("SHOW MATERIALIZED VIEWS LIKE '%_%'", view2.toString());
    }
}
