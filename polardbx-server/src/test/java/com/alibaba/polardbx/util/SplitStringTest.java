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

package com.alibaba.polardbx.util;

import com.alibaba.polardbx.common.utils.Pair;
import com.alibaba.polardbx.server.util.StringUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xianmao.hexm 2010-7-29 下午02:18:43
 */
public class SplitStringTest {

    @Test
    public void test() {
        String str = "mysql$1-3,mysql7,mysql9";
        String[] destStr = StringUtil.split(str, ',', '$', '-');
        Assert.assertEquals(5, destStr.length);
        Assert.assertEquals("mysql1", destStr[0]);
        Assert.assertEquals("mysql2", destStr[1]);
        Assert.assertEquals("mysql3", destStr[2]);
        Assert.assertEquals("mysql7", destStr[3]);
        Assert.assertEquals("mysql9", destStr[4]);
    }

    @Test
    public void test1() {
        String src = "offer$0-3";
        String[] dest = StringUtil.split(src, '$', true);
        Assert.assertEquals(2, dest.length);
        Assert.assertEquals("offer", dest[0]);
        Assert.assertEquals("0-3", dest[1]);
    }

    @Test
    public void test2() {
        String src = "OFFER_group";
        String[] dest = StringUtil.split2(src, '$', '-');
        Assert.assertEquals(1, dest.length);
        Assert.assertEquals("OFFER_group", dest[0]);
    }

    @Test
    public void test3() {
        String src = "OFFER_group$2";
        String[] dest = StringUtil.split2(src, '$', '-');
        Assert.assertEquals(1, dest.length);
        Assert.assertEquals("OFFER_group[2]", dest[0]);
    }

    @Test
    public void test4() {
        String src = "offer$0-3";
        String[] dest = StringUtil.split2(src, '$', '-');
        Assert.assertEquals(4, dest.length);
        Assert.assertEquals("offer[0]", dest[0]);
        Assert.assertEquals("offer[1]", dest[1]);
        Assert.assertEquals("offer[2]", dest[2]);
        Assert.assertEquals("offer[3]", dest[3]);
    }

    @Test
    public void splitIndexTest() {
        String src1 = "offer_group[10]";
        Pair<String, Integer> pair1 = StringUtil.splitIndex(src1, '[', ']');
        Assert.assertEquals("offer_group", pair1.getKey());
        Assert.assertEquals(Integer.valueOf(10), pair1.getValue());

        String src2 = "offer_group";
        Pair<String, Integer> pair2 = StringUtil.splitIndex(src2, '[', ']');
        Assert.assertEquals("offer_group", pair2.getKey());
        Assert.assertEquals(Integer.valueOf(-1), pair2.getValue());
    }

}
