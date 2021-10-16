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

package com.alibaba.polardbx.common.utils.compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by simiao on 15-4-16.
 */
public class ZlibUtilTest extends TestCase {

    /**
     * 测试case的命名必须是testXXXX
     */
    @Test
    public void testCompress() {
        byte[] data = new byte[] { (byte) 0x5b, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0a, (byte) 0x35,
                (byte) 0x2e, (byte) 0x35, (byte) 0x2e, (byte) 0x34, (byte) 0x31, (byte) 0x2d, (byte) 0x30, (byte) 0x75,
                (byte) 0x62, (byte) 0x75, (byte) 0x6e, (byte) 0x74, (byte) 0x75, (byte) 0x30, (byte) 0x2e, (byte) 0x31,
                (byte) 0x34, (byte) 0x2e, (byte) 0x30, (byte) 0x34, (byte) 0x2e, (byte) 0x31, (byte) 0x00, (byte) 0xca,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x60, (byte) 0x4d, (byte) 0x42, (byte) 0x41, (byte) 0x61,
                (byte) 0x44, (byte) 0x6e, (byte) 0x7d, (byte) 0x00, (byte) 0xff, (byte) 0xf7, (byte) 0x08, (byte) 0x02,
                (byte) 0x00, (byte) 0x0f, (byte) 0x80, (byte) 0x15, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x4a, (byte) 0x3b,
                (byte) 0x28, (byte) 0x53, (byte) 0x4b, (byte) 0x5d, (byte) 0x71, (byte) 0x41, (byte) 0x2f, (byte) 0x67,
                (byte) 0x39, (byte) 0x28, (byte) 0x00, (byte) 0x6d, (byte) 0x79, (byte) 0x73, (byte) 0x71, (byte) 0x6c,
                (byte) 0x5f, (byte) 0x6e, (byte) 0x61, (byte) 0x74, (byte) 0x69, (byte) 0x76, (byte) 0x65, (byte) 0x5f,
                (byte) 0x70, (byte) 0x61, (byte) 0x73, (byte) 0x73, (byte) 0x77, (byte) 0x6f, (byte) 0x72, (byte) 0x64,
                (byte) 0x00 };

        byte[] compressed_data = ZlibUtil.compress(data);
        byte[] decompressed_data = ZlibUtil.decompress(compressed_data);

        Assert.assertArrayEquals(data, decompressed_data);
    }

    @Test
    public void testCompressStream() {
        byte[] data = new byte[] { (byte) 0x5b, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0a, (byte) 0x35,
                (byte) 0x2e, (byte) 0x35, (byte) 0x2e, (byte) 0x34, (byte) 0x31, (byte) 0x2d, (byte) 0x30, (byte) 0x75,
                (byte) 0x62, (byte) 0x75, (byte) 0x6e, (byte) 0x74, (byte) 0x75, (byte) 0x30, (byte) 0x2e, (byte) 0x31,
                (byte) 0x34, (byte) 0x2e, (byte) 0x30, (byte) 0x34, (byte) 0x2e, (byte) 0x31, (byte) 0x00, (byte) 0xca,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x60, (byte) 0x4d, (byte) 0x42, (byte) 0x41, (byte) 0x61,
                (byte) 0x44, (byte) 0x6e, (byte) 0x7d, (byte) 0x00, (byte) 0xff, (byte) 0xf7, (byte) 0x08, (byte) 0x02,
                (byte) 0x00, (byte) 0x0f, (byte) 0x80, (byte) 0x15, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x4a, (byte) 0x3b,
                (byte) 0x28, (byte) 0x53, (byte) 0x4b, (byte) 0x5d, (byte) 0x71, (byte) 0x41, (byte) 0x2f, (byte) 0x67,
                (byte) 0x39, (byte) 0x28, (byte) 0x00, (byte) 0x6d, (byte) 0x79, (byte) 0x73, (byte) 0x71, (byte) 0x6c,
                (byte) 0x5f, (byte) 0x6e, (byte) 0x61, (byte) 0x74, (byte) 0x69, (byte) 0x76, (byte) 0x65, (byte) 0x5f,
                (byte) 0x70, (byte) 0x61, (byte) 0x73, (byte) 0x73, (byte) 0x77, (byte) 0x6f, (byte) 0x72, (byte) 0x64,
                (byte) 0x00 };

        ByteArrayOutputStream compress_stream = new ByteArrayOutputStream();

        ZlibUtil.compress(data, compress_stream);

        byte[] decompress_data = ZlibUtil.decompress(new ByteArrayInputStream(compress_stream.toByteArray()));

        Assert.assertArrayEquals(data, decompress_data);
    }

    @Test
    public void testdeCompress() {
        byte[] test = new byte[] { (byte) 0x78, (byte) 0x9c, (byte) 0x6d, (byte) 0x8d, (byte) 0x41, (byte) 0x0a,
                (byte) 0xc2, (byte) 0x30, (byte) 0x00, (byte) 0x04, (byte) 0x37, (byte) 0xd6, (byte) 0x26, (byte) 0x15,
                (byte) 0x3d, (byte) 0xd5, (byte) 0x17, (byte) 0xd8, (byte) 0x9f, (byte) 0x04, (byte) 0x2d, (byte) 0x08,
                (byte) 0x52, (byte) 0x2f, (byte) 0x7a, (byte) 0x2f, (byte) 0x69, (byte) 0x93, (byte) 0xd2, (byte) 0x80,
                (byte) 0x49, (byte) 0x63, (byte) 0x13, (byte) 0x10, (byte) 0x7f, (byte) 0xe7, (byte) 0xc7, (byte) 0x14,
                (byte) 0x43, (byte) 0x45, (byte) 0xbc, (byte) 0x78, (byte) 0x59, (byte) 0x96, (byte) 0x65, (byte) 0x98,
                (byte) 0x25, (byte) 0x00, (byte) 0x21, (byte) 0x07, (byte) 0x60, (byte) 0x96, (byte) 0x48, (byte) 0xd5,
                (byte) 0xe5, (byte) 0xda, (byte) 0x76, (byte) 0xc3, (byte) 0x68, (byte) 0x44, (byte) 0xd0, (byte) 0x83,
                (byte) 0xad, (byte) 0x7d, (byte) 0xdb, (byte) 0x2b, (byte) 0x23, (byte) 0xb2, (byte) 0xd3, (byte) 0x76,
                (byte) 0x5f, (byte) 0x56, (byte) 0xfc, (byte) 0xcc, (byte) 0x7f, (byte) 0x65, (byte) 0x27, (byte) 0x82,
                (byte) 0x68, (byte) 0x84, (byte) 0x57, (byte) 0xcb, (byte) 0xcf, (byte) 0x52, (byte) 0x1f, (byte) 0x79,
                (byte) 0x55, (byte) 0xae, (byte) 0x36, (byte) 0x78, (byte) 0x00, (byte) 0x78, (byte) 0x92, (byte) 0x18,
                (byte) 0x48, (byte) 0x81, (byte) 0xe4, (byte) 0x05, (byte) 0x14, (byte) 0x58, (byte) 0x03, (byte) 0xf3,
                (byte) 0x3f, (byte) 0x4e, (byte) 0x1a, (byte) 0x91, (byte) 0xd4, (byte) 0xdc, (byte) 0xfd, (byte) 0xf5,
                (byte) 0x12, (byte) 0x01, (byte) 0x9a, (byte) 0x3b, (byte) 0x35, (byte) 0x4e, (byte) 0x84, (byte) 0x6d,
                (byte) 0xd5, (byte) 0xf7, (byte) 0x14, (byte) 0x60, (byte) 0xcc, (byte) 0xf5, (byte) 0xee, (byte) 0xa6,
                (byte) 0xad, (byte) 0x64, (byte) 0x40, (byte) 0x46, (byte) 0x83, (byte) 0xf2, (byte) 0x41, (byte) 0x36,
                (byte) 0xd1, (byte) 0xbb, (byte) 0x98, (byte) 0xbc, (byte) 0x6f, (byte) 0xd6, (byte) 0x22, (byte) 0x2f,
                (byte) 0x5e };
        byte[] output = ZlibUtil.decompress(test);
        System.out.println(output);
        test = new byte[] { (byte) 0x78, (byte) 0x9c, (byte) 0x63, (byte) 0x64, (byte) 0x60, (byte) 0x60, (byte) 0x64,
                (byte) 0x54, (byte) 0x67, (byte) 0x60, (byte) 0x60, (byte) 0x62, (byte) 0x4e, (byte) 0x49, (byte) 0x4d,
                (byte) 0x63, (byte) 0x60, (byte) 0x60, (byte) 0x10, (byte) 0x74, (byte) 0x70, (byte) 0x08, (byte) 0x73,
                (byte) 0x0d, (byte) 0x0a, (byte) 0xf6, (byte) 0xf4, (byte) 0xf7, (byte) 0x8b, (byte) 0x77, (byte) 0xf6,
                (byte) 0xf7, (byte) 0xf5, (byte) 0x75, (byte) 0xf5, (byte) 0x0b, (byte) 0x61, (byte) 0xe0, (byte) 0x61,
                (byte) 0x00, (byte) 0x83, (byte) 0xbf, (byte) 0x60, (byte) 0x92, (byte) 0x95, (byte) 0x81, (byte) 0x81,
                (byte) 0xf9, (byte) 0x1f, (byte) 0x50, (byte) 0x39, (byte) 0x83, (byte) 0x18, (byte) 0x03, (byte) 0x03,
                (byte) 0x8b, (byte) 0x68, (byte) 0x48, (byte) 0x4a, (byte) 0x4a, (byte) 0x8e, (byte) 0x42, (byte) 0x70,
                (byte) 0x6a, (byte) 0x51, (byte) 0x59, (byte) 0x6a, (byte) 0x91, (byte) 0x82, (byte) 0x86, (byte) 0xa3,
                (byte) 0x8f, (byte) 0xa7, (byte) 0x93, (byte) 0xa3, (byte) 0x93, (byte) 0xa3, (byte) 0x26, (byte) 0x50,
                (byte) 0x09, (byte) 0x2b, (byte) 0x58, (byte) 0x09, (byte) 0x00, (byte) 0xb9, (byte) 0x7f, (byte) 0x10,
                (byte) 0x3e };
        output = ZlibUtil.decompress(test);
    }
}
