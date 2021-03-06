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

package com.alibaba.polardbx.druid.bvt.sql;

import com.alibaba.polardbx.druid.sql.parser.SymbolTable;
import com.alibaba.polardbx.druid.util.FnvHash;
import junit.framework.TestCase;

public class SymbolTableTest extends TestCase {
    public void test_symbols() throws Exception {
        SymbolTable symbols = new SymbolTable(65535);

        String[] strings = new String[10];
        for (int i = 0; i < strings.length; ++i) {
            String str = "abc" + i;
            strings[i] = str;
            symbols.addSymbol(str, FnvHash.fnv1a_64(str));
        }

        for (int i = 0; i < strings.length; ++i) {
            String str = strings[i];
            long hash = FnvHash.fnv1a_64(str);
            String symbol = symbols.findSymbol(hash);
            assertSame(str, symbol);
        }
        {
            byte[] bytes = "kkk#abc0#aa".getBytes();
            long hash = FnvHash.fnv1a_64(bytes, 4, 8);
            String symbol = symbols.addSymbol(bytes, 4, 8, hash);
            assertSame(strings[0], symbol);
        }

        byte[] bytes = "xab#time:3333".getBytes();
        System.out.println(indexOfTime(bytes, 1));
        System.out.println("xab#time:3333".indexOf("#time"));
    }

    public static int indexOfTime(byte[] bytes, int fromIndex) {
        int end = bytes.length - 5;
        for (int i = fromIndex; i < end; ++i) {
            if (bytes[i] == '#'
                    && bytes[i + 1] == 't'
                    && bytes[i + 2] == 'i'
                    && bytes[i + 3] == 'm'
                    && bytes[i + 4] == 'e') {
                return i;
            }
        }

        return -1;
    }
}
