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

public class MySqlCreateTableTest134 extends MysqlTest {

    public void test_0() throws Exception {
        String sql = "create table xuhan3(\n" +
                "id int not null AUTO_INCREMENT primary key, \n" +
                "name char(40), SimpleDate date, \n" +
                "SimpleDate_dayofweek tinyint(4) GENERATED ALWAYS AS (dayofweek(SimpleDate)) VIRTUAL, \n" +
                "KEY SimpleDate_dayofweek (SimpleDate_dayofweek));";

        MySqlStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> statementList = parser.parseStatementList();
        MySqlCreateTableStatement stmt = (MySqlCreateTableStatement)statementList.get(0);

        assertEquals(1, statementList.size());


        assertEquals("CREATE TABLE xuhan3 (\n" +
                "\tid int NOT NULL PRIMARY KEY AUTO_INCREMENT,\n" +
                "\tname char(40),\n" +
                "\tSimpleDate date,\n" +
                "\tSimpleDate_dayofweek tinyint(4) GENERATED ALWAYS AS (dayofweek(SimpleDate)) VIRTUAL,\n" +
                "\tKEY SimpleDate_dayofweek (SimpleDate_dayofweek)\n" +
                ");", stmt.toString());

        assertEquals("create table xuhan3 (\n" +
                "\tid int not null primary key auto_increment,\n" +
                "\tname char(40),\n" +
                "\tSimpleDate date,\n" +
                "\tSimpleDate_dayofweek tinyint(4) generated always as (dayofweek(SimpleDate)) virtual,\n" +
                "\tkey SimpleDate_dayofweek (SimpleDate_dayofweek)\n" +
                ");", stmt.toLowerCaseString());

    }




}