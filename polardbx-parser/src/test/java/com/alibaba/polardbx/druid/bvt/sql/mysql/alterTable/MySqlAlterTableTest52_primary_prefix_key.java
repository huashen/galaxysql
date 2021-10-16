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
import org.junit.Assert;

/**
 * @version 1.0
 * @ClassName MySqlAlterTableTest52_primary_prefix_key
 * @description
 * @Author zzy
 * @Date 2019-05-15 14:37
 */
public class MySqlAlterTableTest52_primary_prefix_key extends TestCase {

    public void test_0() {
        String sql = "alter table test001 add primary key (b (4) asc, c (8) desc);";

        MySqlStatementParser parser = new MySqlStatementParser(sql);
        SQLStatement stmt = parser.parseStatementList().get(0);
        parser.match(Token.EOF);

        Assert.assertEquals("ALTER TABLE test001\n" +
                "\tADD PRIMARY KEY (b(4) ASC, c(8) DESC);", SQLUtils.toMySqlString(stmt));

    }

}
