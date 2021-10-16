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
import com.alibaba.polardbx.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.polardbx.druid.sql.parser.Token;
import junit.framework.TestCase;

public class AlterTableDbPartitionTest  extends TestCase {

    public void test1() throws Exception {
        String sql = "alter table t1 dbpartition by hash(id) tbpartition by hash(name) tbpartitions 1024";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        SQLStatement stmt = parser.parseStatementList().get(0);
        parser.match(Token.EOF);

        assertEquals("ALTER TABLE t1\n"
                     + "\tDBPARTITION BY hash(id) TBPARTITION BY hash(name) TBPARTITIONS 1024", SQLUtils.toMySqlString(stmt));
    }

    public void test2() throws Exception {
        String sql = "alter table t1 dbpartition by hash(id)";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        SQLStatement stmt = parser.parseStatementList().get(0);
        parser.match(Token.EOF);

        assertEquals("ALTER TABLE t1\n"
                     + "\tDBPARTITION BY hash(id)", SQLUtils.toMySqlString(stmt));
    }

    public void test3() throws Exception {
        String sql = "alter table t1 broadcast";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        SQLStatement stmt = parser.parseStatementList().get(0);
        parser.match(Token.EOF);

        assertEquals("ALTER TABLE t1\n"
                     + "\tBROADCAST", SQLUtils.toMySqlString(stmt));
    }


    public void test4() throws Exception {
        String sql = "alter table t1 single";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        SQLStatement stmt = parser.parseStatementList().get(0);
        parser.match(Token.EOF);

        assertEquals("ALTER TABLE t1\n"
            + "\tSINGLE", SQLUtils.toMySqlString(stmt));
    }
}
