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

package com.alibaba.polardbx.net.packet;

import java.io.IOException;

import com.alibaba.polardbx.net.compress.IPacketOutputProxy;

/**
 * @author xianmao.hexm
 */
public class Reply323Packet extends MySQLPacket {

    public byte[] seed;

    public IPacketOutputProxy write(IPacketOutputProxy proxy) throws IOException {
        proxy.packetBegin();

        proxy.writeUB3(getPacketLength());
        proxy.write(packetId);

        if (seed == null) {
            proxy.write((byte) 0);
        } else {
            proxy.writeWithNull(seed);
        }

        proxy.packetEnd();
        return proxy;
    }

    protected int getPacketLength() {
        return seed == null ? 1 : seed.length + 1;
    }

    @Override
    protected String packetInfo() {
        return "MySQL Auth323 Packet";
    }

}
