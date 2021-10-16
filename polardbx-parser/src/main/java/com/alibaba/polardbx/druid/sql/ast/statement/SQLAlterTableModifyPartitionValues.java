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

package com.alibaba.polardbx.druid.sql.ast.statement;

import com.alibaba.polardbx.druid.sql.ast.SQLObjectImpl;
import com.alibaba.polardbx.druid.sql.ast.SQLPartition;
import com.alibaba.polardbx.druid.sql.visitor.SQLASTVisitor;

/**
 * Created by luoyanxin.
 *
 * for list partition add/drop value
 * @author luoyanxin
 */
public class SQLAlterTableModifyPartitionValues extends SQLObjectImpl implements SQLAlterTableItem,SQLAlterTableGroupItem {
    final boolean isAdd;
    final boolean isDrop;
    final SQLPartition sqlPartition;

    public SQLAlterTableModifyPartitionValues(boolean isAdd, SQLPartition sqlPartition) {
        this.isAdd = isAdd;
        this.isDrop = !this.isAdd;
        this.sqlPartition = sqlPartition;
    }

    @Override
    protected void accept0(SQLASTVisitor visitor) {
        visitor.visit(this);
        visitor.endVisit(this);
    }

    public boolean isAdd() {
        return isAdd;
    }

    public boolean isDrop() {
        return isDrop;
    }

    public SQLPartition getSqlPartition() {
        return sqlPartition;
    }
}
