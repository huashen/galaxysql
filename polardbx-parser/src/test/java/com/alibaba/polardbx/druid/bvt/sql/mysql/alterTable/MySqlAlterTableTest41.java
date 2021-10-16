/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.polardbx.druid.bvt.sql.mysql.alterTable;

import com.alibaba.polardbx.druid.sql.SQLUtils;
import com.alibaba.polardbx.druid.sql.ast.SQLStatement;
import com.alibaba.polardbx.druid.util.JdbcConstants;
import junit.framework.TestCase;

import java.util.List;

public class MySqlAlterTableTest41 extends TestCase {

    public void test_alter_1() throws Exception {
        String sql = "alter table test COLLATE utf8mb4_unicode_ci";
        List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, JdbcConstants.MYSQL);
        assertEquals(1, stmtList.size());
        SQLStatement stmt = stmtList.get(0);
        String output = SQLUtils.toMySqlString(stmt);
        assertEquals("ALTER TABLE test\n" +
                "\tCOLLATE = utf8mb4_unicode_ci", output);
    }

    public void test_alter_2() throws Exception {
        String sql = "alter table test storage_type = 'oss'";
        List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, JdbcConstants.MYSQL);
        assertEquals(1, stmtList.size());
        SQLStatement stmt = stmtList.get(0);
        String output = SQLUtils.toMySqlString(stmt);
        assertEquals("ALTER TABLE test\n" +
                "\tSTORAGE_TYPE = 'oss'", output);
    }

    public void test_alter_3() throws Exception {
        String sql = "alter table test storage_policy = 'hot'";
        List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, JdbcConstants.MYSQL);
        assertEquals(1, stmtList.size());
        SQLStatement stmt = stmtList.get(0);
        String output = SQLUtils.toMySqlString(stmt);
        assertEquals("ALTER TABLE test\n" +
                "\tSTORAGE_POLICY = 'hot'", output);
    }
}
