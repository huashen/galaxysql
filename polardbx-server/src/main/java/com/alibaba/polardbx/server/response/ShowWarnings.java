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

package com.alibaba.polardbx.server.response;

import com.alibaba.polardbx.ErrorCode;
import com.alibaba.polardbx.Fields;
import com.alibaba.polardbx.config.SchemaConfig;
import com.alibaba.polardbx.net.buffer.ByteBufferHolder;
import com.alibaba.polardbx.net.compress.IPacketOutputProxy;
import com.alibaba.polardbx.net.compress.PacketOutputProxyFactory;
import com.alibaba.polardbx.net.packet.EOFPacket;
import com.alibaba.polardbx.net.packet.FieldPacket;
import com.alibaba.polardbx.net.packet.MySQLPacket;
import com.alibaba.polardbx.net.packet.ResultSetHeaderPacket;
import com.alibaba.polardbx.net.packet.RowDataPacket;
import com.alibaba.polardbx.server.ServerConnection;
import com.alibaba.polardbx.server.util.LongUtil;
import com.alibaba.polardbx.server.util.PacketUtil;
import com.alibaba.polardbx.server.util.StringUtil;
import com.google.common.collect.Lists;
import com.alibaba.polardbx.common.utils.TStringUtil;
import com.alibaba.polardbx.matrix.jdbc.TConnection;
import com.alibaba.polardbx.optimizer.context.ExecutionContext;
import com.alibaba.polardbx.optimizer.context.ExecutionContext.ErrorMessage;

import java.util.List;
import java.util.Map;

/**
 * show warnings实现
 *
 * @author agapple 2015年3月27日 下午4:47:46
 * @since 5.1.19
 */
public final class ShowWarnings {

    private static final int FIELD_COUNT = 3;
    private static final ResultSetHeaderPacket header = PacketUtil.getHeader(FIELD_COUNT);
    private static final FieldPacket[] fields = new FieldPacket[FIELD_COUNT];
    private static final EOFPacket eof = new EOFPacket();
    private static final String cmd = "Show Warnings";

    static {
        int i = 0;
        byte packetId = 0;
        header.packetId = ++packetId;

        fields[i] = PacketUtil.getField("Level", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("Code", Fields.FIELD_TYPE_LONG);
        fields[i++].packetId = ++packetId;

        fields[i] = PacketUtil.getField("Message", Fields.FIELD_TYPE_VAR_STRING);
        fields[i++].packetId = ++packetId;
        eof.packetId = ++packetId;
    }

    public static void execute(ServerConnection c, boolean hasMore) {
        String db = c.getSchema();
        if (db == null) {
            c.writeErrMessage(ErrorCode.ER_NO_DB_ERROR, "No database selected");
            return;
        }

        SchemaConfig schema = c.getSchemaConfig();
        if (schema == null) {
            c.writeErrMessage(ErrorCode.ER_BAD_DB_ERROR, "Unknown database '" + db + "'");
            return;
        }

        TConnection conn = c.getTddlConnection();
        if (conn == null || conn.getExecutionContext() == null) {
            c.execute(cmd, hasMore);
            return;
        }

        Map<String, Object> extraDatas = conn.getExecutionContext().getExtraDatas();
        if (extraDatas == null) {
            c.execute(cmd, hasMore);
            return;
        }

        List<ErrorMessage> messagesFailed =
            (List<ErrorMessage>) extraDatas.getOrDefault(ExecutionContext.LastFailedMessage, Lists.newLinkedList());
        List<ErrorMessage> messagesWarning =
            (List<ErrorMessage>) extraDatas.getOrDefault(ExecutionContext.WARNING_MESSAGE, Lists.newLinkedList());

        messagesFailed.addAll(messagesWarning);

        if (messagesFailed.size() == 0) {
            c.execute(cmd, hasMore);
            return;
        }

        ByteBufferHolder buffer = c.allocate();
        IPacketOutputProxy proxy = PacketOutputProxyFactory.getInstance().createProxy(c, buffer);
        proxy.packetBegin();

        // write header
        proxy = header.write(proxy);

        // write fields
        for (FieldPacket field : fields) {
            proxy = field.write(proxy);
        }

        // write eof
        proxy = eof.write(proxy);

        // write rows
        byte packetId = eof.packetId;

        for (ErrorMessage msg : messagesFailed) {
            RowDataPacket row = getRow(msg, c.getCharset());
            row.packetId = ++packetId;
            proxy = row.write(proxy);
        }

        // write last eof
        EOFPacket lastEof = new EOFPacket();
        lastEof.packetId = ++packetId;
        if (hasMore) {
            lastEof.status |= MySQLPacket.SERVER_MORE_RESULTS_EXISTS;
        }
        proxy = lastEof.write(proxy);

        // write buffer
        proxy.packetEnd();
    }

    private static RowDataPacket getRow(ErrorMessage msg, String charset) {
        RowDataPacket row = new RowDataPacket(FIELD_COUNT);
        row.add(StringUtil.encode("Error", charset));
        row.add(LongUtil.toBytes(msg.getCode()));
        String messageText;
        if (TStringUtil.isEmpty(msg.getGroupName())) {
            messageText = msg.getMessage();
        } else {
            messageText = "From " + msg.getGroupName() + " , " + msg.getMessage();
        }
        row.add(StringUtil.encode(messageText, charset));
        return row;
    }

}
