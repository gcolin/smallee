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
import java.util.Map;

import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.Instance;
import net.gcolin.di.atinject.InstanceBuilderMetaData;
import net.gcolin.di.atinject.InstanceCreator;
import net.gcolin.di.atinject.PrototypeProvider;
import net.gcolin.di.core.Key;

/**
 * A abstract class for web scope providers.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public abstract class WebProvider<T> extends PrototypeProvider<T> {

  public WebProvider(Class<T> clazz, Type genericType, Class<? extends T> resolvedClazz,
      Type resolvedGenericType, Environment env) {
    super(clazz, genericType, resolvedClazz, resolvedGenericType, env);
  }
  
  protected Instance newWebInstance(Map<Key, Instance> holderMap) {
    InstanceCreator builder = getBuilder();
    InstanceBuilderMetaData medataData = builder.getMetaData();
    Instance o = builder.createInstance(medataData);
    holderMap.put(getKey(), o);
    builder.bind(o.get(), medataData);
    return o;
  }

  protected Instance getHolder(Map<Key, Instance> holderMap) {
    Instance o = holderMap.get(getKey());
    if (o == null) {
      o = newWebInstance(holderMap);
    }
    return o;
  }

  protected Instance getSyncHolder(Map<Key, Instance> holderMap, Object mutex) {
    synchronized (mutex) {
      Instance o = holderMap.get(getKey());
      if (o == null) {
        o = newWebInstance(holderMap);
      }
      return o;
    }
    
  }

}
