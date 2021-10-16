/*
 * Copyright 1999-2017 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.polardbx.druid.sql.ast.expr;

import com.alibaba.polardbx.druid.sql.ast.SQLObject;
import com.alibaba.polardbx.druid.sql.visitor.SQLASTVisitor;

import java.util.Collections;
import java.util.List;

public class SQLBigIntExpr extends SQLNumericLiteralExpr implements SQLValuableExpr {

    private Long value;

    public SQLBigIntExpr(){

    }

    public SQLBigIntExpr(Long value){
        super();
        this.value = value;
    }

    public SQLBigIntExpr(String value){
        super();
        this.value = Long.valueOf(value);
    }

    public SQLBigIntExpr clone() {
        return new SQLBigIntExpr(value);
    }

    @Override
    public List<SQLObject> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public Number getNumber() {
        return value;
    }

    @Override
    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    protected void accept0(SQLASTVisitor visitor) {
        visitor.visit(this);

        visitor.endVisit(this);
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SQLBigIntExpr other = (SQLBigIntExpr) obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public void setNumber(Number number) {
        if (number == null) {
            this.setValue(null);
            return;
        }

        this.setValue(((Long) number));
    }

}
