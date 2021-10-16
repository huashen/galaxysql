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

package com.alibaba.polardbx.druid.bvt.sql.mysql;

import com.alibaba.polardbx.druid.sql.MysqlTest;
import com.alibaba.polardbx.druid.sql.SQLUtils;
import com.alibaba.polardbx.druid.sql.ast.SQLStatement;
import com.alibaba.polardbx.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.polardbx.druid.sql.parser.SQLParserFeature;
import com.alibaba.polardbx.druid.sql.parser.SQLStatementParser;
import org.junit.Assert;

import java.util.List;

/**
 * @version 1.0
 * @ClassName DrdsGsiTest
 * @description
 * @Author zzy
 * @Date 2019/10/8 20:26
 */
public class DrdsGsiTest extends MysqlTest {

    public void test_0() {
        String sql = "show global index";
        SQLStatementParser parser = new MySqlStatementParser(sql, SQLParserFeature.TDDLHint, SQLParserFeature.EnableCurrentUserExpr, SQLParserFeature.DRDSAsyncDDL, SQLParserFeature.DRDSBaseline, SQLParserFeature.DrdsGSI);
        List<SQLStatement> stmtList = parser.parseStatementList();

        SQLStatement result = stmtList.get(0);
        Assert.assertEquals("SHOW GLOBAL INDEX", SQLUtils.toMySqlString(result));
        Assert.assertEquals("show global index", SQLUtils.toMySqlString(result, SQLUtils.DEFAULT_LCASE_FORMAT_OPTION));
    }

    public void test_1() {
        String sql = "show global indexes;";
        SQLStatementParser parser = new MySqlStatementParser(sql, SQLParserFeature.TDDLHint, SQLParserFeature.EnableCurrentUserExpr, SQLParserFeature.DRDSAsyncDDL, SQLParserFeature.DRDSBaseline, SQLParserFeature.DrdsGSI);
        List<SQLStatement> stmtList = parser.parseStatementList();

        SQLStatement result = stmtList.get(0);
        Assert.assertEquals("SHOW GLOBAL INDEX;", SQLUtils.toMySqlString(result));
        Assert.assertEquals("show global index;", SQLUtils.toMySqlString(result, SQLUtils.DEFAULT_LCASE_FORMAT_OPTION));
    }

    public void test_2() {
        String sql = "show global indexes from tb;";
        SQLStatementParser parser = new MySqlStatementParser(sql, SQLParserFeature.TDDLHint, SQLParserFeature.EnableCurrentUserExpr, SQLParserFeature.DRDSAsyncDDL, SQLParserFeature.DRDSBaseline, SQLParserFeature.DrdsGSI);
        List<SQLStatement> stmtList = parser.parseStatementList();

        SQLStatement result = stmtList.get(0);
        Assert.assertEquals("SHOW GLOBAL INDEX FROM tb;", SQLUtils.toMySqlString(result));
        Assert.assertEquals("show global index from tb;", SQLUtils.toMySqlString(result, SQLUtils.DEFAULT_LCASE_FORMAT_OPTION));
    }

    public void test_3() {
        String sql = "show global index from `tb`";
        SQLStatementParser parser = new MySqlStatementParser(sql, SQLParserFeature.TDDLHint, SQLParserFeature.EnableCurrentUserExpr, SQLParserFeature.DRDSAsyncDDL, SQLParserFeature.DRDSBaseline, SQLParserFeature.DrdsGSI);
        List<SQLStatement> stmtList = parser.parseStatementList();

        SQLStatement result = stmtList.get(0);
        Assert.assertEquals("SHOW GLOBAL INDEX FROM `tb`", SQLUtils.toMySqlString(result));
        Assert.assertEquals("show global index from `tb`", SQLUtils.toMySqlString(result, SQLUtils.DEFAULT_LCASE_FORMAT_OPTION));
    }

    public void test_4() {
        String sql = "show global index from `app`.`tb`";
        SQLStatementParser parser = new MySqlStatementParser(sql, SQLParserFeature.TDDLHint, SQLParserFeature.EnableCurrentUserExpr, SQLParserFeature.DRDSAsyncDDL, SQLParserFeature.DRDSBaseline, SQLParserFeature.DrdsGSI);
        List<SQLStatement> stmtList = parser.parseStatementList();

        SQLStatement result = stmtList.get(0);
        Assert.assertEquals("SHOW GLOBAL INDEX FROM `app`.`tb`", SQLUtils.toMySqlString(result));
        Assert.assertEquals("show global index from `app`.`tb`", SQLUtils.toMySqlString(result, SQLUtils.DEFAULT_LCASE_FORMAT_OPTION));
    }

    public void test_5() {
        String sql = "SHOW METADATA LOCK;";
        SQLStatementParser parser = new MySqlStatementParser(sql, SQLParserFeature.TDDLHint, SQLParserFeature.EnableCurrentUserExpr, SQLParserFeature.DRDSAsyncDDL, SQLParserFeature.DRDSBaseline, SQLParserFeature.DrdsGSI);
        List<SQLStatement> stmtList = parser.parseStatementList();

        SQLStatement result = stmtList.get(0);
        Assert.assertEquals("SHOW METADATA LOCK;", SQLUtils.toMySqlString(result));
        Assert.assertEquals("show metadata lock;", SQLUtils.toMySqlString(result, SQLUtils.DEFAULT_LCASE_FORMAT_OPTION));
    }

    public void test_6() {
        String sql = "SHOW METADATA LOCKS";
        SQLStatementParser parser = new MySqlStatementParser(sql, SQLParserFeature.TDDLHint, SQLParserFeature.EnableCurrentUserExpr, SQLParserFeature.DRDSAsyncDDL, SQLParserFeature.DRDSBaseline, SQLParserFeature.DrdsGSI);
        List<SQLStatement> stmtList = parser.parseStatementList();

        SQLStatement result = stmtList.get(0);
        Assert.assertEquals("SHOW METADATA LOCK", SQLUtils.toMySqlString(result));
        Assert.assertEquals("show metadata lock", SQLUtils.toMySqlString(result, SQLUtils.DEFAULT_LCASE_FORMAT_OPTION));
    }

    public void test_7() {
        String sql = "show metadata lock app";
        SQLStatementParser parser = new MySqlStatementParser(sql, SQLParserFeature.TDDLHint, SQLParserFeature.EnableCurrentUserExpr, SQLParserFeature.DRDSAsyncDDL, SQLParserFeature.DRDSBaseline, SQLParserFeature.DrdsGSI);
        List<SQLStatement> stmtList = parser.parseStatementList();

        SQLStatement result = stmtList.get(0);
        Assert.assertEquals("SHOW METADATA LOCK app", SQLUtils.toMySqlString(result));
        Assert.assertEquals("show metadata lock app", SQLUtils.toMySqlString(result, SQLUtils.DEFAULT_LCASE_FORMAT_OPTION));
    }

    public void test_8() {
        String sql = "show metadata locks `app`";
        SQLStatementParser parser = new MySqlStatementParser(sql, SQLParserFeature.TDDLHint, SQLParserFeature.EnableCurrentUserExpr, SQLParserFeature.DRDSAsyncDDL, SQLParserFeature.DRDSBaseline, SQLParserFeature.DrdsGSI);
        List<SQLStatement> stmtList = parser.parseStatementList();

        SQLStatement result = stmtList.get(0);
        Assert.assertEquals("SHOW METADATA LOCK `app`", SQLUtils.toMySqlString(result));
        Assert.assertEquals("show metadata lock `app`", SQLUtils.toMySqlString(result, SQLUtils.DEFAULT_LCASE_FORMAT_OPTION));
    }

    public void test_9() {
        String sql = "check global index g_i_buyer on t_order;";
        SQLStatementParser parser = new MySqlStatementParser(sql, SQLParserFeature.TDDLHint, SQLParserFeature.EnableCurrentUserExpr, SQLParserFeature.DRDSAsyncDDL, SQLParserFeature.DRDSBaseline, SQLParserFeature.DrdsGSI);
        List<SQLStatement> stmtList = parser.parseStatementList();

        SQLStatement result = stmtList.get(0);
        Assert.assertEquals("CHECK GLOBAL INDEX g_i_buyer ON t_order;", SQLUtils.toMySqlString(result));
        Assert.assertEquals("check global index g_i_buyer on t_order;", SQLUtils.toMySqlString(result, SQLUtils.DEFAULT_LCASE_FORMAT_OPTION));
    }

    public void test_10() {
        String sql = "check global index `g_i_buyer` on `t_order`";
        SQLStatementParser parser = new MySqlStatementParser(sql, SQLParserFeature.TDDLHint, SQLParserFeature.EnableCurrentUserExpr, SQLParserFeature.DRDSAsyncDDL, SQLParserFeature.DRDSBaseline, SQLParserFeature.DrdsGSI);
        List<SQLStatement> stmtList = parser.parseStatementList();

        SQLStatement result = stmtList.get(0);
        Assert.assertEquals("CHECK GLOBAL INDEX `g_i_buyer` ON `t_order`", SQLUtils.toMySqlString(result));
        Assert.assertEquals("check global index `g_i_buyer` on `t_order`", SQLUtils.toMySqlString(result, SQLUtils.DEFAULT_LCASE_FORMAT_OPTION));
    }

    public void test_11() {
        String sql = "check global index `g_i_buyer` on `app`.`t_order`;";
        SQLStatementParser parser = new MySqlStatementParser(sql, SQLParserFeature.TDDLHint, SQLParserFeature.EnableCurrentUserExpr, SQLParserFeature.DRDSAsyncDDL, SQLParserFeature.DRDSBaseline, SQLParserFeature.DrdsGSI);
        List<SQLStatement> stmtList = parser.parseStatementList();

        SQLStatement result = stmtList.get(0);
        Assert.assertEquals("CHECK GLOBAL INDEX `g_i_buyer` ON `app`.`t_order`;", SQLUtils.toMySqlString(result));
        Assert.assertEquals("check global index `g_i_buyer` on `app`.`t_order`;", SQLUtils.toMySqlString(result, SQLUtils.DEFAULT_LCASE_FORMAT_OPTION));
    }

    public void test_12() {
        String sql = "check global index `app`.`g_i_buyer` extcmd;";
        SQLStatementParser parser = new MySqlStatementParser(sql, SQLParserFeature.TDDLHint, SQLParserFeature.EnableCurrentUserExpr, SQLParserFeature.DRDSAsyncDDL, SQLParserFeature.DRDSBaseline, SQLParserFeature.DrdsGSI);
        List<SQLStatement> stmtList = parser.parseStatementList();

        SQLStatement result = stmtList.get(0);
        Assert.assertEquals("CHECK GLOBAL INDEX `app`.`g_i_buyer` extcmd;", SQLUtils.toMySqlString(result));
        Assert.assertEquals("check global index `app`.`g_i_buyer` extcmd;", SQLUtils.toMySqlString(result, SQLUtils.DEFAULT_LCASE_FORMAT_OPTION));
    }

    public void test_13() {
        String sql = "check global index `g_i_buyer` on `app`.`t_order` extcmd";
        SQLStatementParser parser = new MySqlStatementParser(sql, SQLParserFeature.TDDLHint, SQLParserFeature.EnableCurrentUserExpr, SQLParserFeature.DRDSAsyncDDL, SQLParserFeature.DRDSBaseline, SQLParserFeature.DrdsGSI);
        List<SQLStatement> stmtList = parser.parseStatementList();

        SQLStatement result = stmtList.get(0);
        Assert.assertEquals("CHECK GLOBAL INDEX `g_i_buyer` ON `app`.`t_order` extcmd", SQLUtils.toMySqlString(result));
        Assert.assertEquals("check global index `g_i_buyer` on `app`.`t_order` extcmd", SQLUtils.toMySqlString(result, SQLUtils.DEFAULT_LCASE_FORMAT_OPTION));
    }

}
