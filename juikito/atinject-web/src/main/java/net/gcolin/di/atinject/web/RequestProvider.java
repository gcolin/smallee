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
package net.gcolin.di.atinject.web;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;

import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.Instance;
import net.gcolin.di.core.Key;

/**
 * Request scoped provider.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class RequestProvider<T> extends WebProvider<T> {

  private WebExtension webExtension;
  
  public RequestProvider(Class<T> clazz, Type genericType, Class<? extends T> resolvedClazz,
      Type resolvedGenericType, Environment env) {
    super(clazz, genericType, resolvedClazz, resolvedGenericType, env);
    webExtension = env.getExtension(WebExtension.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public T get() {
    ServletRequest req = webExtension.getRequestProvider().get();
    if (req == null) {
      return null;
    }
    Map<Key, Instance> holderMap =
        (Map<Key, Instance>) req.getAttribute(DiFilter.DI_INSTANCES);
    if (holderMap == null) {
      holderMap = new HashMap<>();
      req.setAttribute(DiFilter.DI_INSTANCES, holderMap);
    }
    return (T) getHolder(holderMap).get();
  }

}
