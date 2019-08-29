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

package net.gcolin.di.atinject;

import java.lang.reflect.Type;
import java.util.OptionalDouble;

/**
 * Resolve an OptionalDouble.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class OptionalDoubleResolver extends OptionalResolver {

  public OptionalDoubleResolver(Environment environment) {
    super(environment);
  }

  @Override
  protected Class<?> optionalType() {
    return OptionalDouble.class;
  }
  
  @Override
  protected Object of(Object val) {
    return OptionalDouble.of((double) val);
  }
  
  @Override
  protected Object empty() {
    return OptionalDouble.empty();
  }

  @Override
  protected Type getType(Type genericType) {
    return Double.class;
  }
  
  @Override
  protected Class<?> getPrimitiveType() {
    return double.class;
  }
}
