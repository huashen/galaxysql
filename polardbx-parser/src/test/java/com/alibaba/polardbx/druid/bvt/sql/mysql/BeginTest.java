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
import junit.framework.TestCase;
import org.junit.Assert;

public class BeginTest extends TestCase {


    public void test_0() throws Exception {
        String sql = "Begin";

        SQLStatement sqlStatement = SQLUtils.parseSingleMysqlStatement(sql);

        Assert.assertEquals("BEGIN", sqlStatement.toString());
    }


    public void test_00() throws Exception {
        String sql = "Begin a";

        try {
            SQLStatement sqlStatement = SQLUtils.parseSingleMysqlStatement(sql);
            fail();
        } catch (Exception e) {
        }
    }

    public void test_1() throws Exception {
        String sql = "commit";

        SQLStatement sqlStatement = SQLUtils.parseSingleMysqlStatement(sql);

        Assert.assertEquals("COMMIT", sqlStatement.toString());
    }

    public void test_2() throws Exception {
        String sql = "ROLLBACK";

        SQLStatement sqlStatement = SQLUtils.parseSingleMysqlStatement(sql);

        Assert.assertEquals("ROLLBACK", sqlStatement.toString());
    }

}
