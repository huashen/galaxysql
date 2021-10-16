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

package com.alibaba.polardbx.druid.bvt.sql;

import com.alibaba.polardbx.druid.sql.SQLUtils;
import com.alibaba.polardbx.druid.sql.ast.statement.SQLSelect;
import com.alibaba.polardbx.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.polardbx.druid.util.JdbcConstants;
import junit.framework.TestCase;

import java.util.List;

public class EqualTest_select extends TestCase {

    public void test_eq_select() throws Exception {
        List stmtsA = SQLUtils.parseStatements("select * from a", JdbcConstants.MYSQL);
        List stmtsB = SQLUtils.parseStatements("select * from b", JdbcConstants.MYSQL);
        SQLSelect selectA = ((SQLSelectStatement) stmtsA.get(0)).getSelect();
        SQLSelect selectB = ((SQLSelectStatement) stmtsB.get(0)).getSelect();
        boolean eq = selectA.equals(selectB);
        assertFalse(eq);
    }
}
