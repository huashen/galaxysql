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

package com.alibaba.polardbx.druid.bvt.sql.mysql.resolve;

import com.alibaba.polardbx.druid.DbType;
import com.alibaba.polardbx.druid.sql.SQLUtils;
import com.alibaba.polardbx.druid.sql.ast.SQLStatement;
import com.alibaba.polardbx.druid.sql.repository.SchemaRepository;
import com.alibaba.polardbx.druid.sql.repository.SchemaResolveVisitor;
import junit.framework.TestCase;

public class Resolve_AllColumn_Test_1 extends TestCase {
    public void test_resolve() throws Exception {
        SchemaRepository repository = new SchemaRepository(DbType.mysql);

        repository.acceptDDL("create table t_emp(emp_id bigint, name varchar(20));");


        SQLStatement stmt = SQLUtils.parseSingleMysqlStatement("select 1 as tag, * from t_emp");
        repository.resolve(stmt, SchemaResolveVisitor.Option.ResolveAllColumn);

        assertEquals("SELECT 1 AS tag, emp_id, name\n" +
                "FROM t_emp", stmt.toString());


    }
}
