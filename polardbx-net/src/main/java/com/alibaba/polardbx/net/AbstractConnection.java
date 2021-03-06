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

package com.alibaba.polardbx.net;

import com.alibaba.polardbx.ErrorCode;
import com.alibaba.polardbx.net.buffer.BufferQueue;
import com.alibaba.polardbx.net.buffer.ByteBufferHolder;
import com.alibaba.polardbx.net.packet.AuthPacket;
import com.alibaba.polardbx.net.util.MySQLMessage;
import com.alibaba.polardbx.net.util.SslHandler;
import com.alibaba.polardbx.net.util.TimeUtil;
import com.alibaba.polardbx.common.exception.TddlRuntimeException;
import com.alibaba.polardbx.common.utils.compress.ZlibUtil;

import javax.net.ssl.SSLException;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xianmao.hexm
 */
public abstract class AbstractConnection implements NIOConnection {

    private static final int OP_NOT_READ = ~SelectionKey.OP_READ;
    private static final int OP_NOT_WRITE = ~SelectionKey.OP_WRITE;

    protected final SocketChannel channel;
    protected NIOProcessor processor;
    protected SelectionKey processKey;
    protected final ReentrantLock keyLock;
    protected int packetHeaderSize;
    protected int maxPacketSize;
    protected int readBufferOffset;
    protected ByteBufferHolder readBuffer;
    protected ByteBufferHolder sslReadBuffer;
    protected BufferQueue writeQueue;
    protected final ReentrantLock writeLock;
    protected boolean isRegistered;
    protected final AtomicBoolean isClosed;
    protected boolean isSocketClosed;
    protected long startupTime;
    protected long lastReadTime;
    protected long lastWriteTime;
    protected long netInBytes;
    protected long netOutBytes;
    protected int writeAttempts;
    protected byte packetId = 0;
    protected int compressPacketHeaderSize;
    protected boolean compressProto = false;

    protected byte[] fullCompressPack;

    protected int compressReadOffset;
    protected SslHandler sslHandler;
    protected AtomicBoolean checkSsl = new AtomicBoolean(true);
    protected boolean sslEnable = false;                  // ??????????????????????????????ssl?????????

    public AbstractConnection(SocketChannel channel) {
        this.channel = channel;
        this.keyLock = new ReentrantLock();
        this.writeLock = new ReentrantLock();
        this.isClosed = new AtomicBoolean(false);
        this.startupTime = TimeUtil.currentTimeMillis();
        this.lastReadTime = startupTime;
        this.lastWriteTime = startupTime;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public int getPacketHeaderSize() {
        return packetHeaderSize;
    }

    public void setPacketHeaderSize(int packetHeaderSize) {
        this.packetHeaderSize = packetHeaderSize;
    }

    public void setCompressPacketHeaderSize(int compressPacketHeaderSize) {
        this.compressPacketHeaderSize = compressPacketHeaderSize;
    }

    public int getCompressPacketHeaderSize() {
        return compressPacketHeaderSize;
    }

    public int getMaxPacketSize() {
        return maxPacketSize;
    }

    public void setMaxPacketSize(int maxPacketSize) {
        this.maxPacketSize = maxPacketSize;
    }

    public long getStartupTime() {
        return startupTime;
    }

    public long getLastReadTime() {
        return lastReadTime;
    }

    public long getLastWriteTime() {
        return lastWriteTime;
    }

    public long getNetInBytes() {
        return netInBytes;
    }

    public long getNetOutBytes() {
        return netOutBytes;
    }

    public int getWriteAttempts() {
        return writeAttempts;
    }

    public NIOProcessor getProcessor() {
        return processor;
    }

    public ByteBufferHolder getReadBuffer() {
        return readBuffer;
    }

    public BufferQueue getWriteQueue() {
        return writeQueue;
    }

    public void setWriteQueue(BufferQueue writeQueue) {
        this.writeQueue = writeQueue;
    }

    /**
     * ????????????
     */
    public ByteBufferHolder allocate() {
        return processor.getBufferPool().allocate();
    }

    /**
     * ????????????
     */
    public void recycle(ByteBufferHolder bufferHolder) {
        processor.getBufferPool().recycle(bufferHolder);
    }

    @Override
    public void register(Selector selector) throws IOException {
        try {
            processKey = channel.register(selector, SelectionKey.OP_READ, this);
            isRegistered = true;
        } finally {
            if (isClosed.get()) {
                clearSelectionKey();
            }
        }
    }

    @Override
    public void read() throws IOException {
        ByteBufferHolder buffer = this.readBuffer;
        if (buffer == null || buffer.getBuffer() == null) {
            return;
        }

        int got = channel.read(buffer.getBuffer());
        lastReadTime = TimeUtil.currentTimeMillis();
        if (got < 0) {
            throw new EOFException();
        }
        buffer.writerIndex(buffer.writerIndex() + got);
        netInBytes += got;
        addNetInBytes(got);
        if (sslEnable) {
            doReadSsl(buffer);
        } else {
            doRead(buffer);
        }
        // ????????????
    }

    private void doReadSsl(ByteBufferHolder buffer) throws SSLException {
        sslHandler.decode(buffer);
        Deque<ByteBufferHolder> results = sslHandler.getResults();
        // this loop for get a whole package
        for (; ; ) {
            ByteBufferHolder peek = results.peek();
            if (peek == null) {
                break;
            }
            if (sslReadBuffer == null) {
                sslReadBuffer = allocate();
            }

            int length = sslReadBuffer.remaining() > peek.readable() ? peek.readable() : sslReadBuffer.remaining();

            sslReadBuffer.put(peek.array(), peek.readerIndex(), length);
            peek.skipBytes(length);
            doRead(sslReadBuffer);
            if (!peek.isReadable()) {
                ByteBufferHolder remove = results.remove();
                recycle(remove);
            }
        }
        if (!buffer.isReadable()) {
            buffer.clear();
        }
        // buffer free space empty ,adjust buffer
        if (!buffer.hasRemaining()) {
            readBuffer = checkReadBuffer(buffer, buffer.readerIndex(), buffer.writerIndex(), false);
        }
    }

    private void doRead(ByteBufferHolder buffer) throws SSLException {
        int offset = readBufferOffset, length = 0, position = buffer.position();
        for (; ; ) {
            if (buffer.getBuffer() == null) {
                // In case the buffer is recycled asynchronously
                break;
            }
            length = getPacketLength(buffer, offset); /* ???????????????????????? */
            if (length == -1 || position < offset + length) {// ??????????????????????????????????????????
                if (!buffer.hasRemaining()) {
                    ByteBufferHolder tmpBuffer = checkReadBuffer(buffer, offset, position, true);
                    // when ssl mode ,update sslReadBuffer,else readBuffer
                    if (sslEnable) {
                        sslReadBuffer = tmpBuffer;
                    } else {
                        readBuffer = tmpBuffer;
                    }
                }
                break;
            }

            // ??????????????????????????????????????????
            buffer.position(offset);

            int beforeCompressPayloadLen = 0;
            if (compressProto) {
                /**
                 * ??????????????????????????? ????????????????????????????????????????????????????????????
                 * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????packet
                 */
                byte[] compressHeader = new byte[compressPacketHeaderSize];
                buffer.get(compressHeader, 0, compressPacketHeaderSize);

                /* ?????????????????? */
                MySQLMessage mm = new MySQLMessage(compressHeader);

                /* ?????????????????????????????? */
                mm.readUB3();
                byte compressedSequenceId = mm.read(); // ???????????????sequenceId?????????0
                if (compressedSequenceId < 0) {
                    throw new TddlRuntimeException(com.alibaba.polardbx.common.exception.code.ErrorCode.ERR_PACKET_READ,
                        "compress sequenceId is: " + compressedSequenceId);
                }
                beforeCompressPayloadLen = mm.readUB3();

                offset += compressPacketHeaderSize;
                length -= compressPacketHeaderSize;

                buffer.position(offset);

                if (length < 0) {
                    throw new TddlRuntimeException(com.alibaba.polardbx.common.exception.code.ErrorCode.ERR_PACKET_READ,
                        "length: " + length + " is invalid, beforeCompressPayloadLen: " + beforeCompressPayloadLen);
                }

                if (length == 0) {
                    /**
                     * ????????????????????????load data?????????????????????????????? 00 00 00 02 00 00
                     * 00????????????????????????seqId?????????????????????????????????0??? ?????????????????????seq???1??????????????????????????????
                     */
                    if (position == offset) {// ??????????????????????????????
                        if (readBufferOffset != 0) {
                            readBufferOffset = 0;
                        }
                        buffer.clear();
                        break;
                    } else {// ???????????????????????????
                        readBufferOffset = offset;
                        buffer.readerIndex(readBufferOffset);
                        buffer.position(position);
                        continue;
                    }
                }
            }

            byte[] data = new byte[length];
            buffer.get(data, 0, length);

            if (compressProto) {
                ByteBuffer byteBuffer = null;
                /* ?????????????????????????????? */

                // ?????????
                if (beforeCompressPayloadLen != 0) {
                    data = ZlibUtil.decompress(data);

                    if (data == null) {
                        throw new TddlRuntimeException(com.alibaba.polardbx.common.exception.code.ErrorCode.ERR_PACKET_READ,
                            "beforeCompressPayloadLen: " + beforeCompressPayloadLen);
                    }
                }
                final byte[] decompressData = data;
                byteBuffer = ByteBuffer.wrap(decompressData);
                byteBuffer.position(0);
                byteBuffer.limit(decompressData.length);
                /**
                 * ??????jdbc
                 * ??????16M??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                 */
                for (; ; ) {

                    if (!byteBuffer.hasRemaining()) {
                        break;
                    }
                    int readContentLen = 0;
                    if (fullCompressPack != null) {
                        if (fullCompressPack.length < 4) {
                            if (byteBuffer.remaining() + fullCompressPack.length < 4) {
                                throw new TddlRuntimeException(
                                    com.alibaba.polardbx.common.exception.code.ErrorCode.ERR_PACKET_READ,
                                    "can not get compress length: ");
                            }

                            byte[] header = new byte[4];
                            System.arraycopy(fullCompressPack, 0, header, 0, fullCompressPack.length);
                            for (int i = fullCompressPack.length; i < 4; i++) {
                                header[i] = byteBuffer.get();
                            }

                            readContentLen = header[0] & 0xff;
                            readContentLen |= (header[1] & 0xff) << 8;
                            readContentLen |= (header[2] & 0xff) << 16;
                            readContentLen = readContentLen + packetHeaderSize;
                            fullCompressPack = new byte[readContentLen];
                            System.arraycopy(header, 0, fullCompressPack, 0, 4);

                        }

                        readContentLen = fullCompressPack[0] & 0xff;
                        readContentLen |= (fullCompressPack[1] & 0xff) << 8;
                        readContentLen |= (fullCompressPack[2] & 0xff) << 16;
                        readContentLen = readContentLen + packetHeaderSize;
                    }

                    if (fullCompressPack == null && byteBuffer.remaining() < 4) {

                        fullCompressPack = new byte[byteBuffer.remaining()];
                        byteBuffer.get(fullCompressPack, byteBuffer.position(), byteBuffer.remaining());
                        break;
                    }

                    if (fullCompressPack == null && byteBuffer.remaining() >= 4) {
                        readContentLen = byteBuffer.get() & 0xff;
                        readContentLen |= (byteBuffer.get() & 0xff) << 8;
                        readContentLen |= (byteBuffer.get() & 0xff) << 16;

                        readContentLen = readContentLen + packetHeaderSize;

                        /**
                         * ????????????????????????
                         */
                        if (decompressData.length == readContentLen) {
                            boolean isFirstSslAuthPacket = false;
                            if (sslHandler != null && checkSsl.compareAndSet(true, false)) {
                                isFirstSslAuthPacket = AuthPacket.checkSsl(decompressData);
                            }

                            if (isFirstSslAuthPacket) {
                                // ???????????????????????????????????????,??????????????????
                                sslEnable = true;
                            } else {
                                handleData(decompressData);
                            }
                            break;
                        }
                        fullCompressPack = new byte[readContentLen];

                        byteBuffer.position(byteBuffer.position() - 3);
                    }

                    if (byteBuffer.remaining() >= readContentLen - compressReadOffset) {
                        byteBuffer.get(fullCompressPack, compressReadOffset, readContentLen - compressReadOffset);
                        handleData(fullCompressPack);
                        fullCompressPack = null;
                        compressReadOffset = 0;
                        continue;
                    } else {
                        int remaining = byteBuffer.remaining();
                        byteBuffer.get(fullCompressPack, compressReadOffset, remaining);
                        compressReadOffset += remaining;
                        break;
                    }

                }
            }

            // ?????????SSL??????????????????ssl auth????????????
            // ssl auth???????????????packet
            // ??????????????????????????????CLIENT_SSL?????????
            // ??????????????????????????????????????????(?????????????????????ssl decode)
            boolean isFirstSslAuthPacket = false;
            if (!compressProto) {
                if (sslHandler != null && checkSsl.compareAndSet(true, false)) {
                    isFirstSslAuthPacket = AuthPacket.checkSsl(data);
                }

                if (isFirstSslAuthPacket) {
                    // ???????????????????????????????????????,??????????????????
                    sslEnable = true;
                } else {
                    handleData(data);
                }
            }

            // ???????????????
            offset += length;
            if (position == offset) {// ??????????????????????????????
                if (readBufferOffset != 0) {
                    readBufferOffset = 0;
                }
                buffer.clear();
                break;
            } else {// ???????????????????????????
                readBufferOffset = offset;
                buffer.position(position);
                buffer.readerIndex(offset);
                // **plaintext ???ssl text ?????????????????????ssl ??????

                if (isFirstSslAuthPacket && sslEnable) {
                    readBufferOffset = 0;
                    doReadSsl(buffer);
                }
                continue;
            }
        }

    }

    public void write(byte[] data) {
        ByteBufferHolder buffer = allocate();
        buffer = writeToBuffer(data, buffer);
        write(buffer);
    }

    @Override
    public void write(ByteBufferHolder buffer) {
        if (sslEnable) {
            try {
                this.sslHandler.write(buffer);
                this.sslHandler.flush();
            } catch (Exception e) {
                throw new TddlRuntimeException(com.alibaba.polardbx.common.exception.code.ErrorCode.ERR_PACKET_SSL_SEND, e);
            }
        } else {
            doWrite(buffer);
        }
    }

    public void doWrite(ByteBufferHolder buffer) {
        if (isClosed.get()) {
            recycle(buffer);
            return;
        }
        if (isRegistered) {
            try {
                writeQueue.put(buffer);
            } catch (InterruptedException e) {
                handleError(ErrorCode.ERR_PUT_WRITE_QUEUE, e);
                return;
            }
            processor.postWrite(this);
        } else {
            recycle(buffer);
            close();
        }
    }

    @Override
    public void writeByQueue() throws IOException {
        if (isClosed.get()) {
            return;
        }
        final ReentrantLock lock = this.writeLock;
        lock.lock();
        try {
            // ??????????????????????????????????????????????????????????????????
            // 1.??????key???????????????????????????
            // 2.write0()??????false???
            if ((processKey.interestOps() & SelectionKey.OP_WRITE) == 0 && !write0()) {
                enableWrite();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void writeByEvent() throws IOException {
        if (isClosed.get()) {
            return;
        }
        final ReentrantLock lock = this.writeLock;
        lock.lock();
        try {
            // ??????????????????????????????????????????????????????????????????
            // 1.write0()??????true???
            // 2.???????????????buffer?????????
            if (write0() && writeQueue.size() == 0) {
                disableWrite();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * ???????????????
     */
    public void enableRead() {
        final Lock lock = this.keyLock;
        lock.lock();
        try {
            SelectionKey key = this.processKey;
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
        } finally {
            lock.unlock();
        }
        processKey.selector().wakeup();
    }

    /**
     * ???????????????
     */
    public void disableRead() {
        final Lock lock = this.keyLock;
        lock.lock();
        try {
            SelectionKey key = this.processKey;
            key.interestOps(key.interestOps() & OP_NOT_READ);
        } finally {
            lock.unlock();
        }
    }

    /**
     * ??????WriteBuffer??????????????????????????????????????????????????????????????????
     */
    public ByteBufferHolder checkWriteBuffer(ByteBufferHolder buffer, int capacity) {
        if (buffer.position() != 0 && capacity > buffer.remaining()) {
            write(buffer);
            return allocate();
        } else {
            return buffer;
        }
    }

    /**
     * ?????????????????????????????????????????????????????????????????????????????????????????????
     */
    public ByteBufferHolder writeToBuffer(byte[] src, ByteBufferHolder buffer) {
        return writeToBuffer(src, 0, src.length, buffer);
    }

    /**
     * ?????????????????????????????????????????????????????????????????????????????????????????????
     */
    public ByteBufferHolder writeToBuffer(byte[] src, int offset, int length, ByteBufferHolder buffer) {
        int remaining = buffer.remaining();
        while (length > 0) {
            if (remaining >= length) {
                buffer.put(src, offset, length);
                break;
            } else {
                buffer.put(src, offset, remaining);
                write(buffer);
                buffer = allocate();
                offset += remaining;
                length -= remaining;
                remaining = buffer.remaining();
                continue;
            }
        }
        return buffer;
    }

    @Override
    public boolean close() {
        if (isClosed.get()) {
            return false;
        } else {
            if (closeSocket()) {
                if (sslHandler != null) {
                    sslHandler.close();
                }
                if (isClosed.compareAndSet(false, true)) {
                    closeConfirm();
                    return true;
                }
                return false;
            } else {
                return false;
            }
        }
    }

    protected void closeConfirm() {
        // do nothing
    }

    public boolean isClosed() {
        return isClosed.get();
    }

    /**
     * ???Processor?????????????????????
     */
    protected abstract void idleCheck();

    /**
     * ??????????????????
     */
    protected void cleanup() {
        // ??????????????????
        ByteBufferHolder buffer = this.readBuffer;
        if (buffer != null) {
            this.readBuffer = null;
            recycle(buffer);
        }

        buffer = this.sslReadBuffer;
        if (buffer != null) {
            this.sslReadBuffer = null;
            recycle(buffer);
        }
        // ??????????????????
        while ((buffer = writeQueue.poll()) != null) {
            recycle(buffer);
        }
    }

    /**
     * ?????????????????????????????????MySQL?????????????????????????????????????????????
     */
    protected int getPacketLength(ByteBufferHolder buffer, int offset) {
        if (buffer.position() < offset + packetHeaderSize) {
            return -1;
        } else {
            int length = buffer.get(offset) & 0xff;
            length |= (buffer.get(++offset) & 0xff) << 8;
            length |= (buffer.get(++offset) & 0xff) << 16;
            /**
             * ????????????/???????????????packet?????????(??????header)
             * http://dev.mysql.com/doc/internals/en
             * /example-several-mysql-packets.html
             */
            if (compressProto) {
                return length + compressPacketHeaderSize;
            } else {
                return length + packetHeaderSize;
            }
        }
    }

    /**
     * ??????ReadBuffer?????????????????????????????????????????????????????????
     */
    private ByteBufferHolder checkReadBuffer(ByteBufferHolder buffer, int offset, int position, boolean resetOffest) {
        // ???????????????0???????????????????????????????????????????????????0????????????
        if (offset == 0) {
            if (buffer.capacity() >= maxPacketSize) {
                throw new IllegalArgumentException("Packet size over the limit.");
            }
            int size = buffer.capacity() << 1;
            size = (size > maxPacketSize) ? maxPacketSize : size;
            ByteBuffer newBuffer = ByteBuffer.allocate(size);
            buffer.position(offset);
            newBuffer.put(buffer.getBuffer());
            ByteBufferHolder newBufferHolder = new ByteBufferHolder(newBuffer);
            newBufferHolder.setIndex(buffer.readerIndex(), buffer.writerIndex());
            // ???????????????????????????
            recycle(buffer);
            return newBufferHolder;
        } else {
            buffer.position(offset);
            buffer.compact();
            buffer.setIndex(0, buffer.position());
            if (resetOffest) {
                // ??????readBuffer,???sslEnable???????????????readBuffer???sslReadBuffer??????
                // ??????ssl?????????readBuffer?????????????????????????????????????????????,?????????offest??????0,???????????????????????????sslReadBuffer???offest
                readBufferOffset = 0;
            }
            return buffer;
        }
    }

    protected void addNetInBytes(long bytes) {
        processor.addNetInBytes(bytes);
    }

    protected void addNetOutBytes(long bytes) {
        processor.addNetOutBytes(bytes);
    }

    private boolean write0() throws IOException {
        // ????????????????????????????????????
        ByteBufferHolder buffer = writeQueue.attachment();
        if (buffer != null) {
            int written = channel.write(buffer.getBuffer());
            if (written > 0) {
                netOutBytes += written;
                addNetOutBytes(written);
            }
            lastWriteTime = TimeUtil.currentTimeMillis();
            if (buffer.hasRemaining()) {
                writeAttempts++;
                return false;
            } else {
                writeQueue.attach(null);
                recycle(buffer);
            }
        }
        // ?????????????????????????????????
        if ((buffer = writeQueue.poll()) != null) {
            // ??????????????????????????????buffer???????????????????????????
            if (buffer.position() == 0) {
                recycle(buffer);
                close();
                return true;
            }
            buffer.flip();
            int written = channel.write(buffer.getBuffer());
            if (written > 0) {
                netOutBytes += written;
                addNetOutBytes(written);
            }
            lastWriteTime = TimeUtil.currentTimeMillis();
            if (buffer.hasRemaining()) {
                writeQueue.attach(buffer);
                writeAttempts++;
                return false;
            } else {
                recycle(buffer);
            }
        }
        return true;
    }

    /**
     * ???????????????
     */
    private void enableWrite() {
        final Lock lock = this.keyLock;
        lock.lock();
        try {
            SelectionKey key = this.processKey;
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        } finally {
            lock.unlock();
        }
        processKey.selector().wakeup();
    }

    /**
     * ???????????????
     */
    private void disableWrite() {
        final Lock lock = this.keyLock;
        lock.lock();
        try {
            SelectionKey key = this.processKey;
            key.interestOps(key.interestOps() & OP_NOT_WRITE);
        } finally {
            lock.unlock();
        }
    }

    private void clearSelectionKey() {
        final Lock lock = this.keyLock;
        lock.lock();
        try {
            SelectionKey key = this.processKey;
            if (key != null && key.isValid()) {
                key.attach(null);
                key.cancel();
            }
        } finally {
            lock.unlock();
        }
    }

    private boolean closeSocket() {
        clearSelectionKey();
        SocketChannel channel = this.channel;
        if (channel != null) {
            boolean isSocketClosed = true;
            Socket socket = channel.socket();
            if (socket != null) {
                try {
                    socket.close();
                } catch (Throwable e) {
                }
                isSocketClosed = socket.isClosed();
            }
            try {
                channel.close();
            } catch (Throwable e) {
            }

            boolean closed = isSocketClosed && (!channel.isOpen());

            return closed;
        } else {
            return true;
        }
    }

    public boolean isCompressProto() {
        return compressProto;
    }

    public void setCompressProto(boolean compressProto) {
        this.compressProto = compressProto;
    }

    public boolean isSslEnable() {
        return sslEnable;
    }

    public void setSslHandler(SslHandler sslHandler) {
        this.sslHandler = sslHandler;
    }

}
