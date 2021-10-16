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

package com.alibaba.polardbx.druid.bvt.sql.mysql.select;

import com.alibaba.polardbx.druid.sql.MysqlTest;
import com.alibaba.polardbx.druid.sql.SQLUtils;
import com.alibaba.polardbx.druid.sql.ast.SQLStatement;
import com.alibaba.polardbx.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.polardbx.druid.util.JdbcConstants;

import java.util.List;

public class MySqlSelectTest_163 extends MysqlTest {

    public void test_0() throws Exception {
        String sql = "SELECT\n" +
                "  val,\n" +
                "  ROW_NUMBER() OVER (ORDER BY val) AS 'row_number',\n" +
                "  RANK()       OVER (ORDER BY val) AS 'rank',\n" +
                "  DENSE_RANK() OVER (ORDER BY val) AS 'dense_rank'\n" +
                "FROM numbers;";
//
        List<SQLStatement> statementList = SQLUtils.parseStatements(sql, JdbcConstants.MYSQL);
        SQLSelectStatement stmt = (SQLSelectStatement)statementList.get(0);

        assertEquals(1, statementList.size());

        assertEquals("SELECT val, ROW_NUMBER() OVER (ORDER BY val) AS \"row_number\", RANK() OVER (ORDER BY val) AS \"rank\"\n" +
                "\t, DENSE_RANK() OVER (ORDER BY val) AS \"dense_rank\"\n" +
                "FROM numbers;", stmt.toString());


    }

}