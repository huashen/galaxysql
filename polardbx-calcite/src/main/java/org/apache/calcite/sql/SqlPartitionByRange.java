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

import org.apache.calcite.sql.parser.SqlParserPos;

/**
 * Created by luoyanxin.
 *
 * @author luoyanxin
 */
public class SqlPartitionByRange extends SqlPartitionBy {

    protected SqlNode interval;
    protected boolean isColumns;

    public SqlPartitionByRange(SqlParserPos pos){
        super(pos);
    }

    public void setInterval(SqlNode interval) {
        this.interval = interval;
    }

    public SqlNode getInterval() {
        return this.interval;
    }

    @Override
    public SqlNode getSqlTemplate() {
        return this;
    }

    public boolean isColumns() {
        return isColumns;
    }

    public void setColumns(boolean columns) {
        isColumns = columns;
    }
}
