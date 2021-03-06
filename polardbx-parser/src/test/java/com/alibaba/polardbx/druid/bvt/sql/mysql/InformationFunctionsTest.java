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
package com.alibaba.polardbx.druid.bvt.sql.mysql;

import com.alibaba.polardbx.druid.sql.SQLUtils;
import com.alibaba.polardbx.druid.sql.ast.SQLStatement;
import com.alibaba.polardbx.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.polardbx.druid.sql.parser.SQLStatementParser;
import com.alibaba.polardbx.druid.util.JdbcConstants;
import junit.framework.TestCase;
import org.junit.Assert;

import java.util.List;

public class InformationFunctionsTest extends TestCase {

    public void test_0() throws Exception {
        String sql = "SELECT BENCHMARK(1000000,ENCODE('hello','goodbye'))";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT BENCHMARK(1000000, ENCODE('hello', 'goodbye'))", text);
    }

    public void test_1() throws Exception {
        String sql = "SELECT CHARSET('abc');";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT CHARSET('abc');", text);
    }

    public void test_2() throws Exception {
        String sql = "SELECT CHARSET(CONVERT('abc' USING utf8));";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT CHARSET(CONVERT('abc' USING utf8));", text);
    }

    public void test_3() throws Exception {
        String sql = "SELECT CHARSET(USER());";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT CHARSET(USER());", text);
    }

    public void test_4() throws Exception {
        String sql = "SELECT COERCIBILITY('abc' COLLATE latin1_swedish_ci);";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT COERCIBILITY('abc' COLLATE latin1_swedish_ci);", text);
    }

    public void test_5() throws Exception {
        String sql = "SELECT COLLATION('abc');";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT COLLATION('abc');", text);
    }

    public void test_6() throws Exception {
        String sql = "SELECT * FROM mysql.user;";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT *\nFROM mysql.user;", text);
    }

    public void test_7() throws Exception {
        String sql = "SELECT CURRENT_USER();";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT CURRENT_USER();", text);
    }

    public void test_8() throws Exception {
        String sql = "SELECT DATABASE();";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT DATABASE();", text);
    }

    public void test_9() throws Exception {
        String sql = "SELECT SQL_CALC_FOUND_ROWS * FROM tbl_name;";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT SQL_CALC_FOUND_ROWS *\nFROM tbl_name;", text);
    }

    public void test_10() throws Exception {
        String sql = "SELECT FOUND_ROWS();";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT FOUND_ROWS();", text);
    }

    public void test_11() throws Exception {
        String sql = "SELECT LAST_INSERT_ID();";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT LAST_INSERT_ID();", text);
    }

    public void test_12() throws Exception {
        String sql = "SELECT ROW_COUNT();";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT ROW_COUNT();", text);
    }

    public void test_13() throws Exception {
        String sql = "SELECT USER();";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT USER();", text);
    }

    public void test_14() throws Exception {
        String sql = "SELECT VERSION();";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SELECT VERSION();", text);
    }

    private String output(List<SQLStatement> stmtList) {
        return SQLUtils.toSQLString(stmtList, JdbcConstants.MYSQL);
    }
}
