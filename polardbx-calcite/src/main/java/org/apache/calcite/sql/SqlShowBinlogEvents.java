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

package org.apache.calcite.sql;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.calcite.sql.parser.SqlParserPos;

/**
 * @Author ShuGuang
 * @Description
 * @Date 2020/11/2 2:33 下午
 */
public class SqlShowBinlogEvents extends SqlShow {

    private SqlNode logName, pos, limit;

    public SqlShowBinlogEvents(SqlParserPos parserPos,
                               List<SqlSpecialIdentifier> specialIdentifiers,
                               List<SqlNode> operands, SqlNode logName, SqlNode pos, SqlNode limit) {
        super(parserPos, specialIdentifiers, operands);
        this.logName = logName;
        this.pos = pos;
        this.limit = limit;
    }


    @Override
    public SqlKind getShowKind() {
        return SqlKind.SHOW_BINLOG_EVENTS;
    }

    public SqlNode getLogName() {
        return logName;
    }

    public SqlNode getPos() {
        return pos;
    }

    public SqlNode getLimit() {
        return limit;
    }
}
