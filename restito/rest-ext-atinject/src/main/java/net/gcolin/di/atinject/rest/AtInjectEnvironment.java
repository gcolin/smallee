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
package net.gcolin.di.atinject.rest;

import java.util.function.Supplier;

import net.gcolin.di.atinject.web.RequestScoped;
import net.gcolin.di.atinject.web.SessionScoped;
import net.gcolin.rest.Environment;

/**
 * At inject rest environment.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class AtInjectEnvironment extends Environment {

  private net.gcolin.di.atinject.Environment env;

  public AtInjectEnvironment(net.gcolin.di.atinject.Environment env) {
    this.env = env;
  }

  @Override
  public Supplier<Object> createProvider(Class<?> impl, Class<?> type, boolean proxy) {
    return decorate(impl, type, () -> env.get(impl), proxy);
  }

  @Override
  public void put(Object obj) {
    env.bind(obj);
  }

  @Override
  public boolean isMutable(Class<?> clazz) {
    return clazz.isAnnotationPresent(RequestScoped.class)
        || clazz.isAnnotationPresent(SessionScoped.class);
  }

}
