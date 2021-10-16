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

package com.alibaba.polardbx.druid.bvt.sql.mysql.alterTable;

import com.alibaba.polardbx.druid.sql.SQLUtils;
import com.alibaba.polardbx.druid.sql.ast.SQLStatement;
import com.alibaba.polardbx.druid.util.JdbcConstants;
import junit.framework.TestCase;

import java.util.List;

/**
 * @author shicai.xsc 2018/9/13 下午3:35
 * @desc
 * @since 5.0.0.0
 */
public class MySqlAlterTableTest45 extends TestCase {
    public void test_0() throws Exception {
        String sql = "ALTER TABLE xx\n" +
            "ADD \n" +
            "EXTPARTITION (\n" +
            "DBPARTITION xxx BY KEY('abc') TBPARTITION yyy BY KEY('abc'), \n" +
            "DBPARTITION yyy BY KEY('def') TBPARTITION yyy BY KEY('def'), \n" +
            "DBPARTITION yyy BY KEY('gpk')\n" +
            ")";

        List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, JdbcConstants.MYSQL);
        assertEquals(1, stmtList.size());
        SQLStatement stmt = stmtList.get(0);
        String output = stmt.toString();
        assertEquals("ALTER TABLE xx\n\t" +
            "ADD \n\t" +
            "EXTPARTITION (\n\t" +
            "\tDBPARTITION xxx BY KEY('abc') TBPARTITION yyy BY KEY('abc'), \n\t" +
            "\tDBPARTITION yyy BY KEY('def') TBPARTITION yyy BY KEY('def'), \n\t" +
            "\tDBPARTITION yyy BY KEY('gpk')\n\t" +
            ")", output);
    }

    public void test_1() throws Exception {
        String sql = "ALTER TABLE xx\n" +
            "DROP \n" +
            "EXTPARTITION (\n" +
            "DBPARTITION xxx BY KEY('abc') TBPARTITION yyy BY KEY('abc'), \n" +
            "DBPARTITION yyy BY KEY('def') TBPARTITION yyy BY KEY('def'), \n" +
            "DBPARTITION yyy BY KEY('gpk')\n" +
            ")";

        List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, JdbcConstants.MYSQL);
        assertEquals(1, stmtList.size());
        SQLStatement stmt = stmtList.get(0);
        String output = stmt.toString();
        assertEquals("ALTER TABLE xx\n\t" +
            "DROP \n\t" +
            "EXTPARTITION (\n\t" +
            "\tDBPARTITION xxx BY KEY('abc') TBPARTITION yyy BY KEY('abc'), \n\t" +
            "\tDBPARTITION yyy BY KEY('def') TBPARTITION yyy BY KEY('def'), \n\t" +
            "\tDBPARTITION yyy BY KEY('gpk')\n\t" +
            ")", output);
    }
}
