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

package com.alibaba.polardbx.executor.handler.subhandler;

import com.alibaba.polardbx.executor.cursor.Cursor;
import com.alibaba.polardbx.executor.cursor.impl.ArrayResultCursor;
import com.alibaba.polardbx.executor.handler.VirtualViewHandler;
import com.alibaba.polardbx.optimizer.context.ExecutionContext;
import com.alibaba.polardbx.optimizer.view.InformationSchemaCollations;
import com.alibaba.polardbx.optimizer.view.VirtualView;

/**
 * @author shengyu
 */
public class InformationSchemaCollationsHandler extends BaseVirtualViewSubClassHandler {

    public InformationSchemaCollationsHandler(VirtualViewHandler virtualViewHandler) {
        super(virtualViewHandler);
    }

    @Override
    public boolean isSupport(VirtualView virtualView) {
        return virtualView instanceof InformationSchemaCollations;
    }

    @Override
    public Cursor handle(VirtualView virtualView, ExecutionContext executionContext, ArrayResultCursor cursor) {
        InformationSchemaCollations informationSchemaCollations = (InformationSchemaCollations) virtualView;
        cursor.addRow(new Object[] {"armscii8_general_ci", "armscii8", 32, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"armscii8_bin", "armscii8", 64, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"ascii_general_ci", "ascii", 11, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"ascii_bin", "ascii", 65, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"big5_chinese_ci", "big5", 1, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"big5_bin", "big5", 84, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"binary", "binary", 63, "Yes", "Yes", 1, "NO PAD"});
        cursor.addRow(new Object[] {"cp1250_general_ci", "cp1250", 26, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"cp1250_czech_cs", "cp1250", 34, "", "Yes", 2, "PAD SPACE"});
        cursor.addRow(new Object[] {"cp1250_croatian_ci", "cp1250", 44, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"cp1250_bin", "cp1250", 66, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"cp1250_polish_ci", "cp1250", 99, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"cp1251_bulgarian_ci", "cp1251", 14, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"cp1251_ukrainian_ci", "cp1251", 23, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"cp1251_bin", "cp1251", 50, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"cp1251_general_ci", "cp1251", 51, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"cp1251_general_cs", "cp1251", 52, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"cp1256_general_ci", "cp1256", 57, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"cp1256_bin", "cp1256", 67, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"cp1257_lithuanian_ci", "cp1257", 29, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"cp1257_bin", "cp1257", 58, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"cp1257_general_ci", "cp1257", 59, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"cp850_general_ci", "cp850", 4, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"cp850_bin", "cp850", 80, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"cp852_general_ci", "cp852", 40, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"cp852_bin", "cp852", 81, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"cp866_general_ci", "cp866", 36, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"cp866_bin", "cp866", 68, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"cp932_japanese_ci", "cp932", 95, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"cp932_bin", "cp932", 96, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"dec8_swedish_ci", "dec8", 3, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"dec8_bin", "dec8", 69, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"eucjpms_japanese_ci", "eucjpms", 97, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"eucjpms_bin", "eucjpms", 98, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"euckr_korean_ci", "euckr", 19, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"euckr_bin", "euckr", 85, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"gb18030_chinese_ci", "gb18030", 248, "Yes", "Yes", 2, "PAD SPACE"});
        cursor.addRow(new Object[] {"gb18030_bin", "gb18030", 249, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"gb18030_unicode_520_ci", "gb18030", 250, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"gb2312_chinese_ci", "gb2312", 24, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"gb2312_bin", "gb2312", 86, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"gbk_chinese_ci", "gbk", 28, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"gbk_bin", "gbk", 87, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"geostd8_general_ci", "geostd8", 92, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"geostd8_bin", "geostd8", 93, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"greek_general_ci", "greek", 25, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"greek_bin", "greek", 70, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"hebrew_general_ci", "hebrew", 16, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"hebrew_bin", "hebrew", 71, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"hp8_english_ci", "hp8", 6, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"hp8_bin", "hp8", 72, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"keybcs2_general_ci", "keybcs2", 37, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"keybcs2_bin", "keybcs2", 73, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"koi8r_general_ci", "koi8r", 7, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"koi8r_bin", "koi8r", 74, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"koi8u_general_ci", "koi8u", 22, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"koi8u_bin", "koi8u", 75, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"latin1_german1_ci", "latin1", 5, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"latin1_swedish_ci", "latin1", 8, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"latin1_danish_ci", "latin1", 15, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"latin1_german2_ci", "latin1", 31, "", "Yes", 2, "PAD SPACE"});
        cursor.addRow(new Object[] {"latin1_bin", "latin1", 47, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"latin1_general_ci", "latin1", 48, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"latin1_general_cs", "latin1", 49, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"latin1_spanish_ci", "latin1", 94, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"latin2_czech_cs", "latin2", 2, "", "Yes", 4, "PAD SPACE"});
        cursor.addRow(new Object[] {"latin2_general_ci", "latin2", 9, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"latin2_hungarian_ci", "latin2", 21, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"latin2_croatian_ci", "latin2", 27, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"latin2_bin", "latin2", 77, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"latin5_turkish_ci", "latin5", 30, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"latin5_bin", "latin5", 78, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"latin7_estonian_cs", "latin7", 20, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"latin7_general_ci", "latin7", 41, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"latin7_general_cs", "latin7", 42, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"latin7_bin", "latin7", 79, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"macce_general_ci", "macce", 38, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"macce_bin", "macce", 43, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"macroman_general_ci", "macroman", 39, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"macroman_bin", "macroman", 53, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"sjis_japanese_ci", "sjis", 13, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"sjis_bin", "sjis", 88, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"swe7_swedish_ci", "swe7", 10, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"swe7_bin", "swe7", 82, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"tis620_thai_ci", "tis620", 18, "Yes", "Yes", 4, "PAD SPACE"});
        cursor.addRow(new Object[] {"tis620_bin", "tis620", 89, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_general_ci", "ucs2", 35, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_bin", "ucs2", 90, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_unicode_ci", "ucs2", 128, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_icelandic_ci", "ucs2", 129, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_latvian_ci", "ucs2", 130, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_romanian_ci", "ucs2", 131, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_slovenian_ci", "ucs2", 132, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_polish_ci", "ucs2", 133, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_estonian_ci", "ucs2", 134, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_spanish_ci", "ucs2", 135, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_swedish_ci", "ucs2", 136, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_turkish_ci", "ucs2", 137, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_czech_ci", "ucs2", 138, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_danish_ci", "ucs2", 139, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_lithuanian_ci", "ucs2", 140, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_slovak_ci", "ucs2", 141, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_spanish2_ci", "ucs2", 142, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_roman_ci", "ucs2", 143, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_persian_ci", "ucs2", 144, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_esperanto_ci", "ucs2", 145, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_hungarian_ci", "ucs2", 146, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_sinhala_ci", "ucs2", 147, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_german2_ci", "ucs2", 148, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_croatian_ci", "ucs2", 149, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_unicode_520_ci", "ucs2", 150, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_vietnamese_ci", "ucs2", 151, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"ucs2_general_mysql500_ci", "ucs2", 159, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"ujis_japanese_ci", "ujis", 12, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"ujis_bin", "ujis", 91, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16_general_ci", "utf16", 54, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16_bin", "utf16", 55, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16_unicode_ci", "utf16", 101, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16_icelandic_ci", "utf16", 102, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16_latvian_ci", "utf16", 103, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16_romanian_ci", "utf16", 104, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16_slovenian_ci", "utf16", 105, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16_polish_ci", "utf16", 106, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16_estonian_ci", "utf16", 107, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16_spanish_ci", "utf16", 108, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16_swedish_ci", "utf16", 109, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16_turkish_ci", "utf16", 110, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16_czech_ci", "utf16", 111, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16_danish_ci", "utf16", 112, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16_lithuanian_ci", "utf16", 113, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16_slovak_ci", "utf16", 114, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16_spanish2_ci", "utf16", 115, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16_roman_ci", "utf16", 116, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16_persian_ci", "utf16", 117, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16_esperanto_ci", "utf16", 118, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16_hungarian_ci", "utf16", 119, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16_sinhala_ci", "utf16", 120, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16_german2_ci", "utf16", 121, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16_croatian_ci", "utf16", 122, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16_unicode_520_ci", "utf16", 123, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16_vietnamese_ci", "utf16", 124, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16le_general_ci", "utf16le", 56, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf16le_bin", "utf16le", 62, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf32_general_ci", "utf32", 60, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf32_bin", "utf32", 61, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf32_unicode_ci", "utf32", 160, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf32_icelandic_ci", "utf32", 161, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf32_latvian_ci", "utf32", 162, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf32_romanian_ci", "utf32", 163, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf32_slovenian_ci", "utf32", 164, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf32_polish_ci", "utf32", 165, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf32_estonian_ci", "utf32", 166, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf32_spanish_ci", "utf32", 167, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf32_swedish_ci", "utf32", 168, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf32_turkish_ci", "utf32", 169, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf32_czech_ci", "utf32", 170, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf32_danish_ci", "utf32", 171, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf32_lithuanian_ci", "utf32", 172, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf32_slovak_ci", "utf32", 173, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf32_spanish2_ci", "utf32", 174, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf32_roman_ci", "utf32", 175, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf32_persian_ci", "utf32", 176, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf32_esperanto_ci", "utf32", 177, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf32_hungarian_ci", "utf32", 178, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf32_sinhala_ci", "utf32", 179, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf32_german2_ci", "utf32", 180, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf32_croatian_ci", "utf32", 181, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf32_unicode_520_ci", "utf32", 182, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf32_vietnamese_ci", "utf32", 183, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_general_ci", "utf8", 33, "Yes", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_tolower_ci", "utf8", 76, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_bin", "utf8", 83, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_unicode_ci", "utf8", 192, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_icelandic_ci", "utf8", 193, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_latvian_ci", "utf8", 194, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_romanian_ci", "utf8", 195, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_slovenian_ci", "utf8", 196, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_polish_ci", "utf8", 197, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_estonian_ci", "utf8", 198, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_spanish_ci", "utf8", 199, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_swedish_ci", "utf8", 200, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_turkish_ci", "utf8", 201, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_czech_ci", "utf8", 202, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_danish_ci", "utf8", 203, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_lithuanian_ci", "utf8", 204, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_slovak_ci", "utf8", 205, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_spanish2_ci", "utf8", 206, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_roman_ci", "utf8", 207, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_persian_ci", "utf8", 208, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_esperanto_ci", "utf8", 209, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_hungarian_ci", "utf8", 210, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_sinhala_ci", "utf8", 211, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_german2_ci", "utf8", 212, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_croatian_ci", "utf8", 213, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_unicode_520_ci", "utf8", 214, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_vietnamese_ci", "utf8", 215, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8_general_mysql500_ci", "utf8", 223, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_general_ci", "utf8mb4", 45, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_bin", "utf8mb4", 46, "", "Yes", 1, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_unicode_ci", "utf8mb4", 224, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_icelandic_ci", "utf8mb4", 225, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_latvian_ci", "utf8mb4", 226, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_romanian_ci", "utf8mb4", 227, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_slovenian_ci", "utf8mb4", 228, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_polish_ci", "utf8mb4", 229, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_estonian_ci", "utf8mb4", 230, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_spanish_ci", "utf8mb4", 231, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_swedish_ci", "utf8mb4", 232, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_turkish_ci", "utf8mb4", 233, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_czech_ci", "utf8mb4", 234, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_danish_ci", "utf8mb4", 235, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_lithuanian_ci", "utf8mb4", 236, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_slovak_ci", "utf8mb4", 237, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_spanish2_ci", "utf8mb4", 238, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_roman_ci", "utf8mb4", 239, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_persian_ci", "utf8mb4", 240, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_esperanto_ci", "utf8mb4", 241, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_hungarian_ci", "utf8mb4", 242, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_sinhala_ci", "utf8mb4", 243, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_german2_ci", "utf8mb4", 244, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_croatian_ci", "utf8mb4", 245, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_unicode_520_ci", "utf8mb4", 246, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_vietnamese_ci", "utf8mb4", 247, "", "Yes", 8, "PAD SPACE"});
        cursor.addRow(new Object[] {"utf8mb4_0900_ai_ci", "utf8mb4", 255, "Yes", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_de_pb_0900_ai_ci", "utf8mb4", 256, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_is_0900_ai_ci", "utf8mb4", 257, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_lv_0900_ai_ci", "utf8mb4", 258, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_ro_0900_ai_ci", "utf8mb4", 259, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_sl_0900_ai_ci", "utf8mb4", 260, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_pl_0900_ai_ci", "utf8mb4", 261, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_et_0900_ai_ci", "utf8mb4", 262, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_es_0900_ai_ci", "utf8mb4", 263, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_sv_0900_ai_ci", "utf8mb4", 264, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_tr_0900_ai_ci", "utf8mb4", 265, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_cs_0900_ai_ci", "utf8mb4", 266, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_da_0900_ai_ci", "utf8mb4", 267, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_lt_0900_ai_ci", "utf8mb4", 268, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_sk_0900_ai_ci", "utf8mb4", 269, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_es_trad_0900_ai_ci", "utf8mb4", 270, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_la_0900_ai_ci", "utf8mb4", 271, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_eo_0900_ai_ci", "utf8mb4", 273, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_hu_0900_ai_ci", "utf8mb4", 274, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_hr_0900_ai_ci", "utf8mb4", 275, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_vi_0900_ai_ci", "utf8mb4", 277, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_0900_as_cs", "utf8mb4", 278, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_de_pb_0900_as_cs", "utf8mb4", 279, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_is_0900_as_cs", "utf8mb4", 280, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_lv_0900_as_cs", "utf8mb4", 281, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_ro_0900_as_cs", "utf8mb4", 282, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_sl_0900_as_cs", "utf8mb4", 283, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_pl_0900_as_cs", "utf8mb4", 284, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_et_0900_as_cs", "utf8mb4", 285, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_es_0900_as_cs", "utf8mb4", 286, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_sv_0900_as_cs", "utf8mb4", 287, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_tr_0900_as_cs", "utf8mb4", 288, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_cs_0900_as_cs", "utf8mb4", 289, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_da_0900_as_cs", "utf8mb4", 290, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_lt_0900_as_cs", "utf8mb4", 291, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_sk_0900_as_cs", "utf8mb4", 292, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_es_trad_0900_as_cs", "utf8mb4", 293, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_la_0900_as_cs", "utf8mb4", 294, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_eo_0900_as_cs", "utf8mb4", 296, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_hu_0900_as_cs", "utf8mb4", 297, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_hr_0900_as_cs", "utf8mb4", 298, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_vi_0900_as_cs", "utf8mb4", 300, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_ja_0900_as_cs", "utf8mb4", 303, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_ja_0900_as_cs_ks", "utf8mb4", 304, "", "Yes", 24, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_0900_as_ci", "utf8mb4", 305, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_ru_0900_ai_ci", "utf8mb4", 306, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_ru_0900_as_cs", "utf8mb4", 307, "", "Yes", 0, "NO PAD"});
        cursor.addRow(new Object[] {"utf8mb4_zh_0900_as_cs", "utf8mb4", 308, "", "Yes", 0, "NO PAD"});
        return cursor;
    }
}
