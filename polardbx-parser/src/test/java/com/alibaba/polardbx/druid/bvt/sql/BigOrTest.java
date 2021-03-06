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
package com.alibaba.polardbx.druid.bvt.sql;

import com.alibaba.polardbx.druid.sql.SQLUtils;
import com.alibaba.polardbx.druid.sql.ast.SQLStatement;
import junit.framework.TestCase;

import java.util.List;

public class BigOrTest extends TestCase {

    public void testBigOr() throws Exception {
        StringBuilder buf = new StringBuilder();
        buf.append("SELECT * FROM T WHERE FID = ?");
        for (int i = 0; i < 10000; ++i) {
            buf.append(" OR FID = " + i);
        }
        String sql = buf.toString();
        List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, null);
        String text = SQLUtils.toSQLString(stmtList.get(0));
        //System.out.println(text);
    }
}
