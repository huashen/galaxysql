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

package com.alibaba.polardbx.druid.bvt.bug;

import com.alibaba.polardbx.druid.sql.SQLUtils;
import com.alibaba.polardbx.druid.sql.ast.SQLStatement;
import junit.framework.TestCase;

public class Issue74009 extends TestCase {
    public void test_for_issue() throws Exception {
        String sql = "select * from (select * from mm union select * from mm) a,(select * from mm union select * from mm) b;";

        SQLStatement stmt = SQLUtils.parseSingleMysqlStatement(sql);

        assertEquals("SELECT *\n" +
                "FROM (\n" +
                "\tSELECT *\n" +
                "\tFROM mm\n" +
                "\tUNION\n" +
                "\tSELECT *\n" +
                "\tFROM mm\n" +
                ") a, (\n" +
                "\t\tSELECT *\n" +
                "\t\tFROM mm\n" +
                "\t\tUNION\n" +
                "\t\tSELECT *\n" +
                "\t\tFROM mm\n" +
                "\t) b;", stmt.toString());
    }
}
