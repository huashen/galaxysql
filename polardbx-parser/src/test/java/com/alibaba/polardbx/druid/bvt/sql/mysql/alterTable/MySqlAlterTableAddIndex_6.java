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
package com.alibaba.polardbx.druid.bvt.sql.mysql.alterTable;

import com.alibaba.polardbx.druid.sql.SQLUtils;
import com.alibaba.polardbx.druid.sql.ast.SQLStatement;
import com.alibaba.polardbx.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.polardbx.druid.sql.parser.Token;
import com.alibaba.polardbx.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.polardbx.druid.stat.TableStat;
import com.alibaba.polardbx.druid.util.JdbcConstants;
import junit.framework.TestCase;
import org.junit.Assert;

public class MySqlAlterTableAddIndex_6 extends TestCase {

    public void test_alter_first() throws Exception {
        String sql =
            "ALTER TABLE t_order ADD UNIQUE GLOBAL INDEX `g_i_buyer` (`buyer_id`) COVERING (order_snapshot) dbpartition by hash(`buyer_id`) tbpartition by hash(`buyer_id`) COMMENT \"CREATE GSI TEST\";";
        MySqlStatementParser parser = new MySqlStatementParser(sql);
        SQLStatement stmt = parser.parseStatementList().get(0);
        parser.match(Token.EOF);
        
        Assert.assertEquals("ALTER TABLE t_order\n" +
                "\tADD UNIQUE GLOBAL INDEX `g_i_buyer` (`buyer_id`) COVERING (order_snapshot) DBPARTITION BY hash(`buyer_id`) TBPARTITION BY hash(`buyer_id`) COMMENT 'CREATE GSI TEST';", SQLUtils.toMySqlString(stmt));
        
        Assert.assertEquals("alter table t_order\n" +
                "\tadd unique global index `g_i_buyer` (`buyer_id`) covering (order_snapshot) dbpartition by hash(`buyer_id`) tbpartition by hash(`buyer_id`) comment 'CREATE GSI TEST';", SQLUtils.toMySqlString(stmt, SQLUtils.DEFAULT_LCASE_FORMAT_OPTION));

        SchemaStatVisitor visitor = new SQLUtils().createSchemaStatVisitor(JdbcConstants.MYSQL);
        stmt.accept(visitor);
        TableStat tableStat = visitor.getTableStat("t_order");
        assertNotNull(tableStat);
        assertEquals(1, tableStat.getAlterCount());
        assertEquals(1, tableStat.getCreateIndexCount());
    }

}
