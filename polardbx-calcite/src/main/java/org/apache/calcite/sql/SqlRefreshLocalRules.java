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

public class SqlRefreshLocalRules extends SqlDal {

    private static final SqlSpecialOperator OPERATOR = new SqlAffectedRowsOperator("REFRESH_LOCAL_RULES", SqlKind.REFRESH_LOCAL_RULES);

    public SqlRefreshLocalRules(SqlParserPos pos) {
        super(pos);
    }

    @Override
    public void unparse(SqlWriter writer, int leftPrec, int rightPrec) {
        final SqlWriter.Frame selectFrame = writer.startList(SqlWriter.FrameTypeEnum.SELECT);
        writer.sep("REFRESH LOCAL RULES");
        writer.endList(selectFrame);
    }

    @Override
    public SqlOperator getOperator() {
        return OPERATOR;
    }

    @Override
    public SqlKind getKind() {
        return SqlKind.REFRESH_LOCAL_RULES;
    }

}
