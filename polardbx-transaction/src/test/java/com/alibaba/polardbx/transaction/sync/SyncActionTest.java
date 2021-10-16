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

package com.alibaba.polardbx.transaction.sync;

import org.junit.Assert;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class SyncActionTest {

    @Test
    public void testFetchTransForDeadlockDetectionSyncAction() {
        FetchTransForDeadlockDetectionSyncAction action = new FetchTransForDeadlockDetectionSyncAction("db2");
        String data = JSON.toJSONString(action, SerializerFeature.WriteClassName);
        System.out.println(data);

        FetchTransForDeadlockDetectionSyncAction obj =
            (FetchTransForDeadlockDetectionSyncAction) JSON.parse(data, Feature.SupportAutoType);
        System.out.println(obj.toString());

        Assert.assertEquals(obj.getSchema(), action.getSchema());
    }

    @Test
    public void testFetchAllTranSyncAction() {
        FetchAllTransSyncAction action = new FetchAllTransSyncAction("db2", true);
        String data = JSON.toJSONString(action, SerializerFeature.WriteClassName);
        System.out.println(data);

        FetchAllTransSyncAction obj =
            (FetchAllTransSyncAction) JSON.parse(data, Feature.SupportAutoType);
        System.out.println(obj.toString());

        Assert.assertEquals(obj.getSchema(), action.getSchema());
        Assert.assertEquals(obj.isFetchSql(), action.isFetchSql());
    }
}
