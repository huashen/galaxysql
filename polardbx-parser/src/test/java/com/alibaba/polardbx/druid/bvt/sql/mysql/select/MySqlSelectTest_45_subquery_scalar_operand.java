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
import com.alibaba.polardbx.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.polardbx.druid.util.JdbcConstants;
import org.junit.Assert;

import java.util.List;

public class MySqlSelectTest_45_subquery_scalar_operand extends MysqlTest {

    public void test_0() throws Exception {
        String sql = "SELECT (SELECT s1 FROM t2) FROM t1;";


        List<SQLStatement> statementList = SQLUtils.parseStatements(sql, JdbcConstants.MYSQL, true);
        SQLStatement stmt = statementList.get(0);

        Assert.assertEquals(1, statementList.size());

        SchemaStatVisitor visitor = SQLUtils.createSchemaStatVisitor(JdbcConstants.MYSQL);
        stmt.accept(visitor);

//        System.out.println("Tables : " + visitor.getTables());
//        System.out.println("fields : " + visitor.getColumns());
//        System.out.println("coditions : " + visitor.getConditions());
//        System.out.println("orderBy : " + visitor.getOrderByColumns());
        
//        Assert.assertEquals(1, visitor.getTables().size());
//        Assert.assertEquals(1, visitor.getColumns().size());
//        Assert.assertEquals(0, visitor.getConditions().size());
//        Assert.assertEquals(0, visitor.getOrderByColumns().size());
        
        {
            String output = SQLUtils.toMySqlString(stmt);
            Assert.assertEquals("SELECT (\n" +
                            "\t\tSELECT s1\n" +
                            "\t\tFROM t2\n" +
                            "\t)\n" +
                            "FROM t1;", //
                                output);
        }
        {
            String output = SQLUtils.toMySqlString(stmt, SQLUtils.DEFAULT_LCASE_FORMAT_OPTION);
            Assert.assertEquals("select (\n" +
                            "\t\tselect s1\n" +
                            "\t\tfrom t2\n" +
                            "\t)\n" +
                            "from t1;", //
                                output);
        }

        {
            String output = SQLUtils.toMySqlString(stmt, new SQLUtils.FormatOption(true, true, true));
            Assert.assertEquals("SELECT (\n" +
                            "\t\tSELECT s1\n" +
                            "\t\tFROM t2\n" +
                            "\t)\n" +
                            "FROM t1;", //
                    output);
        }
    }
}
