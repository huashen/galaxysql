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

package com.alibaba.polardbx.instance;

/**
 * 实例类型
 *
 * @author arnkore 2017-01-22 11:03
 */
public enum InstanceType {
    PRIVATE("private"),
    PUBLIC("public");

    private String typeStr;

    private InstanceType(String typeStr) {
        this.typeStr = typeStr;
    }

    public static InstanceType parse(String typeStr) {
        for (InstanceType instanceType : InstanceType.values()) {
            if (instanceType.typeStr.equals(typeStr)) {
                return instanceType;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return typeStr;
    }
}
