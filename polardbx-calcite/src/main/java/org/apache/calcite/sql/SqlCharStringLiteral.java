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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.util.Bug;
import org.apache.calcite.util.NlsString;
import org.apache.calcite.util.Util;
import org.openjdk.jol.info.ClassLayout;

import java.util.List;

/**
 * A character string literal.
 *
 * <p>Its {@link #value} field is an {@link NlsString} and {@link #typeName} is
 * {@link SqlTypeName#CHAR}.
 */
public class SqlCharStringLiteral extends SqlAbstractStringLiteral {
  /**
   * 40 for empty String size estimation
   */
  private static final long INSTANCE_SIZE_WITH_OBJECT =
      ClassLayout.parseClass(SqlCharStringLiteral.class).instanceSize() +
      ClassLayout.parseClass(NlsString.class).instanceSize() + 40;

  private static final Function<SqlLiteral, NlsString> F =
      new Function<SqlLiteral, NlsString>() {
        public NlsString apply(SqlLiteral literal) {
          return ((SqlCharStringLiteral) literal).getNlsString();
        }
      };

  //~ Constructors -----------------------------------------------------------

  protected SqlCharStringLiteral(NlsString val, SqlParserPos pos) {
    super(val, SqlTypeName.CHAR, pos);
  }

  //~ Methods ----------------------------------------------------------------

  /**
   * @return the underlying NlsString
   */
  public NlsString getNlsString() {
    return (NlsString) value;
  }

  /**
   * @return the collation
   */
  public SqlCollation getCollation() {
    return getNlsString().getCollation();
  }

  @Override public SqlCharStringLiteral clone(SqlParserPos pos) {
    return new SqlCharStringLiteral((NlsString) value, pos);
  }

  public void unparse(
      SqlWriter writer,
      int leftPrec,
      int rightPrec) {
    if (false) {
      Util.discard(Bug.FRG78_FIXED);
      String stringValue = ((NlsString) value).getValue();
      writer.literal(
          writer.getDialect().quoteStringLiteral(stringValue));
    }
    assert value instanceof NlsString;
    writer.literal(value.toString());
  }

  protected SqlAbstractStringLiteral concat1(List<SqlLiteral> literals) {
    return new SqlCharStringLiteral(
        NlsString.concat(Lists.transform(literals, F)),
        literals.get(0).getParserPosition());
  }

  @Override
  public int getValueLength() {
    return ((NlsString)value).getValue().length();
  }

  @Override
  public long estimateSize() {
    return INSTANCE_SIZE_WITH_OBJECT + getValueBytes();
  }
}

// End SqlCharStringLiteral.java
