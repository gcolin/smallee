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

import java.math.BigInteger;

import javax.json.JsonNumber;

/**
 * The {@code AbstractJsonNumber} class give a basic implementation of JsonNumber. The child classes
 * may override some methods.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public abstract class AbstractJsonNumber implements JsonNumber {

  @Override
  public ValueType getValueType() {
    return ValueType.NUMBER;
  }

  @Override
  public int intValue() {
    return (int) longValue();
  }

  @Override
  public boolean isIntegral() {
    return bigDecimalValue().scale() == 0;
  }

  @Override
  public int intValueExact() {
    return intValue();
  }

  @Override
  public long longValue() {
    return intValue();
  }

  @Override
  public long longValueExact() {
    return longValue();
  }

  @Override
  public BigInteger bigIntegerValueExact() {
    return bigDecimalValue().toBigIntegerExact();
  }

  @Override
  public BigInteger bigIntegerValue() {
    return bigDecimalValue().toBigInteger();
  }

  @Override
  public double doubleValue() {
    return intValue();
  }

  @Override
  public int hashCode() {
    return bigDecimalValue().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof JsonNumber)) {
      return false;
    }
    JsonNumber other = (JsonNumber) obj;
    return bigDecimalValue().equals(other.bigDecimalValue());
  }

}
