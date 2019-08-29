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

import java.util.Objects;

import javax.json.JsonString;

/**
 * The {@code JsonStringImpl} class represents a JsonString.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JsonStringImpl implements JsonString {

  private String value;

  public JsonStringImpl(String value) {
    this.value = value;
  }

  @Override
  public ValueType getValueType() {
    return ValueType.STRING;
  }

  @Override
  public CharSequence getChars() {
    return value;
  }

  @Override
  public String getString() {
    return value;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('"');

    for (int i = 0; i < value.length(); i++) {
      char ch = value.charAt(i);
      // unescaped = %x20-21 | %x23-5B | %x5D-10FFFF
      if (ch >= 0x20 && ch != 0x22 && ch != 0x5c) {
        sb.append(ch);
      } else {
        sb.append(CharEncode.escape(ch));
      }
    }

    sb.append('"');
    return sb.toString();
  }

  @Override
  public int hashCode() {
    return 31 * value.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj != null && obj instanceof JsonString) {
      return Objects.equals(((JsonString) obj).getString(), value);
    }
    return false;
  }



}
