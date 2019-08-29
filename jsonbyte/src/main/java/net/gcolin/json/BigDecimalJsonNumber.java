/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package net.gcolin.json;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * The {@code BigDecimalJsonNumber} class represents a BigDecimal JsonNumber.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class BigDecimalJsonNumber extends AbstractJsonNumber {
  private final BigDecimal bigDecimal;

  public BigDecimalJsonNumber(BigDecimal value) {
    bigDecimal = value;
  }

  @Override
  public int intValue() {
    return bigDecimalValue().intValue();
  }

  @Override
  public int intValueExact() {
    return bigDecimalValue().intValueExact();
  }

  @Override
  public long longValue() {
    return bigDecimalValue().longValue();
  }

  @Override
  public long longValueExact() {
    return bigDecimalValue().longValueExact();
  }

  @Override
  public BigInteger bigIntegerValue() {
    return bigDecimalValue().toBigInteger();
  }

  @Override
  public BigInteger bigIntegerValueExact() {
    return bigDecimalValue().toBigIntegerExact();
  }

  @Override
  public double doubleValue() {
    return bigDecimalValue().doubleValue();
  }

  @Override
  public BigDecimal bigDecimalValue() {
    return bigDecimal;
  }

  @Override
  public String toString() {
    return bigDecimalValue().toString();
  }

}
