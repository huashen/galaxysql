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

public class MySqlSelectTest_135 extends MysqlTest {

    public void test_0() throws Exception {
        String sql = "select (~(oct(mediumint_test  ))     ),(    ((  'b')AND (date_test  ))  in(smallint_test, bigint_test,tinyint_1bit_test,( WEIGHT_STRING( 0x007fff LEVEL 1 DESC  ))) )from select_base_two_multi_db_multi_tb";

        List<SQLStatement> statementList = SQLUtils.parseStatements(sql, JdbcConstants.MYSQL);
        SQLSelectStatement stmt = (SQLSelectStatement)statementList.get(0);

        assertEquals(1, statementList.size());

        assertEquals("SELECT ~oct(mediumint_test), ('b'\n" +
                "\tAND date_test) IN (smallint_test, bigint_test, tinyint_1bit_test, WEIGHT_STRING(0x007fff LEVEL 1 DESC))\n" +
                "FROM select_base_two_multi_db_multi_tb", stmt.toString());

        assertEquals("SELECT ~oct(mediumint_test), (?\n" +
                "\tAND date_test) IN (smallint_test, bigint_test, tinyint_1bit_test, WEIGHT_STRING(? LEVEL 1 DESC))\n" +
                "FROM select_base_two_multi_db_multi_tb", ParameterizedOutputVisitorUtils.parameterize(sql, JdbcConstants.MYSQL));
    }


}