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

package com.alibaba.polardbx.druid.bvt.sql.eval;

import com.alibaba.polardbx.druid.sql.visitor.SQLEvalVisitorUtils;
import com.alibaba.polardbx.druid.util.JdbcConstants;
import junit.framework.TestCase;
import org.junit.Assert;

public class EvalMethodPowerTest extends TestCase {

    public void test_reverse() throws Exception {
        Assert.assertEquals(Math.pow(1, 2), SQLEvalVisitorUtils.evalExpr(JdbcConstants.MYSQL, "power(1, 2)"));
        Assert.assertEquals(Math.pow(3, 4), SQLEvalVisitorUtils.evalExpr(JdbcConstants.MYSQL, "power(3, 4)"));
        Assert.assertEquals(Math.pow(4, 5), SQLEvalVisitorUtils.evalExpr(JdbcConstants.MYSQL, "pow(4, 5)"));
    }

    public void test_error() throws Exception {
        Exception error = null;
        try {
            SQLEvalVisitorUtils.evalExpr(JdbcConstants.MYSQL, "pow()", 12L);
        } catch (Exception e) {
            error = e;
        }
        Assert.assertNotNull(error);
    }

    public void test_error_1() throws Exception {
        Exception error = null;
        try {
            SQLEvalVisitorUtils.evalExpr(JdbcConstants.MYSQL, "pow(a,b)");
        } catch (Exception e) {
            error = e;
        }
        Assert.assertNotNull(error);
    }
}
