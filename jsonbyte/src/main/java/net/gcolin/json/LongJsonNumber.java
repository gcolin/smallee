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

/**
 * The {@code IntegerJsonNumber} class represents a Long JsonNumber.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class LongJsonNumber extends AbstractJsonNumber {
  private final long lv;
  private BigDecimal bd;

  public LongJsonNumber(long value) {
    lv = value;
  }

  @Override
  public long longValue() {
    return lv;
  }

  @Override
  public boolean isIntegral() {
    return true;
  }

  @Override
  public double doubleValue() {
    return (double) lv;
  }

  @Override
  public BigDecimal bigDecimalValue() {
    if (bd == null) {
      bd = new BigDecimal(lv);
    }
    return bd;
  }

  @Override
  public String toString() {
    return String.valueOf(lv);
  }

}
