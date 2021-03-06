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
import com.alibaba.polardbx.druid.util.JdbcUtils;
import junit.framework.TestCase;
import org.junit.Assert;

public class MybatisTest2 extends TestCase {

    private String sql = "select * from t where id = ${id}";

    public void test_mysql() throws Exception {
        Assert.assertEquals("SELECT *\nFROM t\nWHERE id = ${id}", SQLUtils.format(sql, JdbcUtils.MYSQL));
    }

    public void test_sql92() throws Exception {
        Assert.assertEquals("SELECT *\nFROM t\nWHERE id = ${id}", SQLUtils.format(sql, null));
    }
}
