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
import com.alibaba.polardbx.druid.sql.visitor.ParameterizedOutputVisitorUtils;
import com.alibaba.polardbx.druid.util.JdbcConstants;

import java.util.List;

/**
 * @version 1.0
 * @ClassName MySqlSelectTest_eq_null_parameterized
 * @description
 * @Author zzy
 * @Date 2019-07-16 17:28
 */
public class MySqlSelectTest_eq_null_parameterized extends MysqlTest {

    public void test_0() throws Exception {
        String sql = "select * from test_null_shard where id = null;";

        List<SQLStatement> statementList = SQLUtils.parseStatements(sql, JdbcConstants.MYSQL);
        SQLSelectStatement stmt = (SQLSelectStatement) statementList.get(0);

        assertEquals(1, statementList.size());

        assertEquals("SELECT *\n" +
                "FROM test_null_shard\n" +
                "WHERE id = NULL;", stmt.toString());

        assertEquals("SELECT *\n" +
                "FROM test_null_shard\n" +
                "WHERE id = ?;", ParameterizedOutputVisitorUtils.parameterize(sql, JdbcConstants.MYSQL));
    }

}
