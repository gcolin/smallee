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

package net.gcolin.di.core;

import java.util.function.Supplier;

/**
 * An interface for being used in a ServiceLoader.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public interface InjectService {
  /**
   * Add bean classes.
   * 
   * @param classes bean classes
   */
  void add(Class<?>... classes);

  /**
   * Remove bean classes.
   * 
   * @param classes bean classes
   */
  void remove(Class<?>... classes);

  /**
   * Add a singleton.
   * 
   * @param obj singleton
   */
  void bind(Object obj);

  /**
   * Remove a singleton.
   * 
   * @param obj singleton
   */
  void unbind(Object obj);

  <T> Supplier<T> findSupplier(Class<T> clazz);

  /**
   * Find by name.
   * 
   * @param name bean name
   * @return a bean reference or {@code null}
   */
  Object find(String name);

  /**
   * Check if a bean can have different instances.
   * 
   * @param type bean class
   * @return {@code true} if the bean can have different instances
   */
  boolean isMutable(Class<?> type);
}
