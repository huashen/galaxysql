/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.calcite.sql;

import com.google.common.collect.ImmutableList;
import org.apache.calcite.sql.parser.SqlParserPos;

import com.google.common.base.Preconditions;

/** Base class for CREATE, DROP and other DDL statements. */
public abstract class SqlDdl extends SqlCall implements SqlTable, SqlHint{
  /** Use this operator only if you don't have a better one. */
  protected static final SqlOperator DDL_OPERATOR =
      new SqlSpecialOperator("DDL", SqlKind.OTHER_DDL);

  private final SqlOperator operator;

  protected SqlNode name;

  protected SqlNodeList hints = new SqlNodeList(ImmutableList.<SqlNode>of(), SqlParserPos.ZERO);

  /** Creates a SqlDdl. */
  public SqlDdl(SqlOperator operator, SqlParserPos pos) {
    super(pos);
    this.operator = Preconditions.checkNotNull(operator);
  }

  public SqlOperator getOperator() {
    return operator;
  }

  public SqlNode getTargetTable() {
    return this.name;
  }

  public void setTargetTable(SqlNode sqlIdentifier) {
    this.name = sqlIdentifier;
  }

  @Override
  public SqlNodeList getHints() {
    return hints;
  }

  @Override
  public void setHints(SqlNodeList hints) {
    this.hints = hints;
  }
}

// End SqlDdl.java
