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
import com.alibaba.polardbx.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.polardbx.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.polardbx.druid.stat.TableStat;
import com.alibaba.polardbx.druid.util.JdbcConstants;

import java.util.List;


public class MySqlSelectTest_109_alias extends MysqlTest {
    public void test_0() throws Exception {
        String sql = "\n" +
                "select * from t where op_date = date_format(DATE_ADD('2017-12-27', Interval -1 year), \"%Y-%m-%d 00:00:00\")";

        MySqlStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> statementList = parser.parseStatementList();

        assertEquals(1, statementList.size());

        SQLStatement stmt = statementList.get(0);

        assertEquals("SELECT *\n" +
                "FROM t\n" +
                "WHERE op_date = date_format(DATE_ADD('2017-12-27', INTERVAL -1 YEAR), '%Y-%m-%d 00:00:00')", stmt.toString());

        SchemaStatVisitor visitor = SQLUtils.createSchemaStatVisitor(JdbcConstants.MYSQL);
        stmt.accept(visitor);

        TableStat.Name tableName = visitor.getTables().keySet().iterator().next();
        assertEquals("t", tableName.toString());
//        System.out.println("Tables : " + visitor.getTables());
//        System.out.println("fields : " + visitor.getColumns());
        System.out.println("coditions : " + visitor.getConditions());
        System.out.println("orderBy : " + visitor.getOrderByColumns());
    }

}