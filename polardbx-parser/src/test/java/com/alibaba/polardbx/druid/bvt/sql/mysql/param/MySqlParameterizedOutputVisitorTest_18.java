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

package com.alibaba.polardbx.druid.bvt.sql.mysql.param;

import com.alibaba.polardbx.druid.DbType;
import com.alibaba.polardbx.druid.sql.SQLUtils;
import com.alibaba.polardbx.druid.sql.ast.SQLStatement;
import com.alibaba.polardbx.druid.sql.parser.SQLParserUtils;
import com.alibaba.polardbx.druid.sql.parser.SQLStatementParser;
import com.alibaba.polardbx.druid.sql.visitor.ParameterizedOutputVisitorUtils;
import com.alibaba.polardbx.druid.sql.visitor.SQLASTOutputVisitor;
import com.alibaba.polardbx.druid.util.JdbcConstants;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wenshao on 16/8/23.
 */
public class MySqlParameterizedOutputVisitorTest_18 extends TestCase {
    public void test_for_parameterize() throws Exception {
        final DbType dbType = JdbcConstants.MYSQL;

        String sql = "insert into `t_n_0021` ( " +
                "`f0`, `f1`, `f2`, `f3`, `f4`" +
                ", `f5`, `f6`, `f7`, `f8`, `f9`" +
                ", `f10`, `f11`, `f12`, `f13`, `f14`" +
                ", `f15`) " +
                "values ( NOW(), NOW(), 123, 'abc', 'abd'" +
                ", 'tair:ldbcount:808', 0.0, 2.0, 0, 251, 0, '172.29.60.62', 2, 1483686655818, 12, 0);";
        String psql = ParameterizedOutputVisitorUtils.parameterize(sql, dbType);
        assertEquals("INSERT INTO t_n (`f0`, `f1`, `f2`, `f3`, `f4`\n" +
                "\t, `f5`, `f6`, `f7`, `f8`, `f9`\n" +
                "\t, `f10`, `f11`, `f12`, `f13`, `f14`\n" +
                "\t, `f15`)\n" +
                "VALUES (NOW(), NOW(), ?, ?, ?\n" +
                "\t, ?, ?, ?, ?, ?\n" +
                "\t, ?, ?, ?, ?, ?\n" +
                "\t, ?);", psql);

        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
        List<SQLStatement> stmtList = parser.parseStatementList();

        StringBuilder out = new StringBuilder();
        SQLASTOutputVisitor visitor = SQLUtils.createOutputVisitor(out, JdbcConstants.MYSQL);
        List<Object> parameters = new ArrayList<Object>();
        visitor.setParameterized(true);
        visitor.setParameterizedMergeInList(true);
        visitor.setParameters(parameters);
        visitor.setExportTables(true);
        /*visitor.setPrettyFormat(false);*/

        SQLStatement stmt = stmtList.get(0);
        stmt.accept(visitor);

        // System.out.println(parameters);
        assertEquals(14, parameters.size());

        StringBuilder buf = new StringBuilder();
        SQLASTOutputVisitor visitor1 = SQLUtils.createOutputVisitor(buf, dbType);
        visitor1.setParameters(visitor.getParameters());
        stmt.accept(visitor1);

        assertEquals("INSERT INTO `t_n_0021` (`f0`, `f1`, `f2`, `f3`, `f4`\n" +
                "\t, `f5`, `f6`, `f7`, `f8`, `f9`\n" +
                "\t, `f10`, `f11`, `f12`, `f13`, `f14`\n" +
                "\t, `f15`)\n" +
                "VALUES (NOW(), NOW(), 123, 'abc', 'abd'\n" +
                "\t, 'tair:ldbcount:808', 0.0, 2.0, 0, 251\n" +
                "\t, 0, '172.29.60.62', 2, 1483686655818, 12\n" +
                "\t, 0);", buf.toString());
    }
}
