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
package com.alibaba.polardbx.druid.bvt.sql.mysql.show;

import com.alibaba.polardbx.druid.sql.ast.SQLStatement;
import com.alibaba.polardbx.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.polardbx.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;
import com.alibaba.polardbx.druid.sql.parser.SQLStatementParser;
import junit.framework.TestCase;
import org.junit.Assert;

import java.util.List;

public class SHOW_STATUS_Syntax_Test extends TestCase {

    public void test_0() throws Exception {
        String sql = "SHOW STATUS LIKE 'Key%'";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SHOW STATUS LIKE 'Key%';", text);
    }
    
    public void test_where() throws Exception {
        String sql = "SHOW STATUS WHERE X LIKE 'Key%'";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SHOW STATUS WHERE X LIKE 'Key%';", text);
    }
    
    public void test_corba() throws Exception {
        String sql = "SHOW COBAR_STATUS";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SHOW COBAR_STATUS;", text);
    }

    public void test_1() throws Exception {
        String sql = "SHOW GLOBAL STATUS LIKE 'Key%'";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SHOW GLOBAL STATUS LIKE 'Key%';", text);
    }

    public void test_2() throws Exception {
        String sql = "SHOW SESSION STATUS LIKE 'Key%'";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SHOW SESSION STATUS LIKE 'Key%';", text);
    }

    public void test_3() throws Exception {
        String sql = "SHOW SESSION STATUS";

        SQLStatementParser parser = new MySqlStatementParser(sql);
        List<SQLStatement> stmtList = parser.parseStatementList();

        String text = output(stmtList);

        Assert.assertEquals("SHOW SESSION STATUS;", text);
    }

    private String output(List<SQLStatement> stmtList) {
        StringBuilder out = new StringBuilder();

        for (SQLStatement stmt : stmtList) {
            stmt.accept(new MySqlOutputVisitor(out));
            out.append(";");
        }

        return out.toString();
    }
}
