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

package com.alibaba.polardbx.druid.bvt.sql.mysql.createTable;

import com.alibaba.polardbx.druid.sql.MysqlTest;
import com.alibaba.polardbx.druid.sql.ast.SQLStatement;
import com.alibaba.polardbx.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;
import com.alibaba.polardbx.druid.sql.dialect.mysql.parser.MySqlStatementParser;

import java.util.List;

public class MySqlCreateTableTest103 extends MysqlTest {

    public void test_0() throws Exception {
        String sql = "CREATE TABLE `procs_priv` (\n" +
                "  `Host` char(60) COLLATE utf8_bin NOT NULL DEFAULT '',\n" +
                "  `Db` char(64) COLLATE utf8_bin NOT NULL DEFAULT '',\n" +
                "  `User` char(32) COLLATE utf8_bin NOT NULL DEFAULT '',\n" +
                "  `Routine_name` char(64) CHARACTER SET utf8 NOT NULL DEFAULT '',\n" +
                "  `Routine_type` enum('FUNCTION','PROCEDURE') COLLATE utf8_bin NOT NULL,\n" +
                "  `Grantor` char(93) COLLATE utf8_bin NOT NULL DEFAULT '',\n" +
                "  `Proc_priv` set('Execute','Alter Routine','Grant') CHARACTER SET utf8 NOT NULL DEFAULT '',\n" +
                "  `Timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n" +
                "  PRIMARY KEY (`Host`,`Db`,`User`,`Routine_name`,`Routine_type`),\n" +
                "  KEY `Grantor` (`Grantor`)\n" +
                ") ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='Procedure privileges'";

        MySqlStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> statementList = parser.parseStatementList();
        MySqlCreateTableStatement stmt = (MySqlCreateTableStatement)statementList.get(0);

        assertEquals(1, statementList.size());
        assertEquals(10, stmt.getTableElementList().size());

        assertEquals("CREATE TABLE `procs_priv` (\n" +
                "\t`Host` char(60) COLLATE utf8_bin NOT NULL DEFAULT '',\n" +
                "\t`Db` char(64) COLLATE utf8_bin NOT NULL DEFAULT '',\n" +
                "\t`User` char(32) COLLATE utf8_bin NOT NULL DEFAULT '',\n" +
                "\t`Routine_name` char(64) CHARACTER SET utf8 NOT NULL DEFAULT '',\n" +
                "\t`Routine_type` enum('FUNCTION', 'PROCEDURE') COLLATE utf8_bin NOT NULL,\n" +
                "\t`Grantor` char(93) COLLATE utf8_bin NOT NULL DEFAULT '',\n" +
                "\t`Proc_priv` set('Execute', 'Alter Routine', 'Grant') CHARACTER SET utf8 NOT NULL DEFAULT '',\n" +
                "\t`Timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n" +
                "\tPRIMARY KEY (`Host`, `Db`, `User`, `Routine_name`, `Routine_type`),\n" +
                "\tKEY `Grantor` (`Grantor`)\n" +
                ") ENGINE = MyISAM DEFAULT CHARSET = utf8 DEFAULT COLLATE = utf8_bin COMMENT 'Procedure privileges'", stmt.toString());

        assertEquals("CREATE TABLE `procs_priv` (\n" +
                "\t`Host` char(60) COLLATE utf8_bin NOT NULL DEFAULT '',\n" +
                "\t`Db` char(64) COLLATE utf8_bin NOT NULL DEFAULT '',\n" +
                "\t`User` char(32) COLLATE utf8_bin NOT NULL DEFAULT '',\n" +
                "\t`Routine_name` char(64) CHARACTER SET utf8 NOT NULL DEFAULT '',\n" +
                "\t`Routine_type` enum('FUNCTION', 'PROCEDURE') COLLATE utf8_bin NOT NULL,\n" +
                "\t`Grantor` char(93) COLLATE utf8_bin NOT NULL DEFAULT '',\n" +
                "\t`Proc_priv` set('Execute', 'Alter Routine', 'Grant') CHARACTER SET utf8 NOT NULL DEFAULT '',\n" +
                "\t`Timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n" +
                "\tPRIMARY KEY (`Host`, `Db`, `User`, `Routine_name`, `Routine_type`),\n" +
                "\tKEY `Grantor` (`Grantor`)\n" +
                ") ENGINE = MyISAM DEFAULT CHARSET = utf8 DEFAULT COLLATE = utf8_bin COMMENT 'Procedure privileges'", stmt.clone().toString());
    }
}