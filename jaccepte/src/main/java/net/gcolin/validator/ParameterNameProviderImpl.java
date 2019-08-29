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

package net.gcolin.validator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.validation.ParameterNameProvider;

/**
 * A ParameterNameProvider implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ParameterNameProviderImpl implements ParameterNameProvider {

  private static final String PREFIX = "arg";

  @Override
  public List<String> getParameterNames(Constructor<?> constructor) {
    return getParameterNames(constructor.getParameterTypes().length);
  }

  @Override
  public List<String> getParameterNames(Method method) {
    return getParameterNames(method.getParameterTypes().length);
  }

  private List<String> getParameterNames(int length) {
    List<String> list = new ArrayList<String>(length);
    for (int i = 0; i < length; i++) {
      list.add(PREFIX + i);
    }
    return list;
  }

}
