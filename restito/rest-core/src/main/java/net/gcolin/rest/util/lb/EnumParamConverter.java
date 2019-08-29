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

package net.gcolin.rest.util.lb;

import net.gcolin.common.lang.Strings;
import net.gcolin.common.reflect.Reflect;

/**
 * Converter String to Enum.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class EnumParamConverter implements Converter<Enum<?>> {

  private Class<?> enumClass;

  private static final EnumParamConverter EPC = new EnumParamConverter();

  public static EnumParamConverter getInstance() {
    return EPC;
  }

  /**
   * Create an instance for an enum class.
   * 
   * @param enumClass type of enumeration
   * @return an EnumParamConverter
   */
  public static EnumParamConverter getInstance(Class<?> enumClass) {
    EnumParamConverter bc = new EnumParamConverter();
    bc.enumClass = enumClass;
    return bc;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public Enum<?> fromString(String str) {
    if (Strings.isNullOrEmpty(str)) {
      return null;
    }
    return Reflect.enumValue((Class) enumClass, str);
  }

  @Override
  public String toString(Enum<?> obj) {
    return obj.toString();
  }
}
