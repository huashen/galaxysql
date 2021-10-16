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

package com.alibaba.polardbx.gms.metadb.cdc;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.alibaba.polardbx.gms.metadb.record.SystemTableRecord;

/**
 *
 */
public class CdcDumperRecord implements SystemTableRecord {
    private String ip;
    private int port;
    private String role;

    @Override
    public CdcDumperRecord fill(ResultSet rs) throws SQLException {
        this.ip = rs.getString(1);
        this.port = rs.getInt(2);
        this.role = rs.getString(3);
        return this;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getRole() {
        return role;
    }
}
