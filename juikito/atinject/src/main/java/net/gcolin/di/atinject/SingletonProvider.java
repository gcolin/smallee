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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Singleton;

/**
 * A singleton provider.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class SingletonProvider<T> extends AbstractProvider<T> {

  protected T singleton;
  protected Instance instance;
  private Class<T> clazz;
  private Class<? extends T> resolvedClazz;
  private Type resolvedGenericType;
  private Type genericType;
  protected Environment env;

  public SingletonProvider(Class<T> clazz, Type genericType, Class<? extends T> resolvedClazz,
      Type resolvedGenericType, Environment env) {
    this.resolvedClazz = resolvedClazz;
    this.resolvedGenericType = resolvedGenericType;
    this.clazz = clazz;
    this.env = env;
    this.genericType = genericType;
  }

  @SuppressWarnings("unchecked")
  public SingletonProvider(T singleton, Type genericType) {
    this.singleton = singleton;
    this.genericType = genericType;
    this.resolvedGenericType = genericType;
    this.clazz = (Class<T>) singleton.getClass();
    this.resolvedClazz = clazz;
  }

  @Override
  public synchronized T get() {
    if (singleton == null) {
      singleton = create();
    }
    return singleton;
  }

  @Override
  public T getNoCreate() {
    return singleton;
  }

  @Override
  public Class<T> getType() {
    return clazz;
  }

  @Override
  public Class<? extends Annotation> getScope() {
    return Singleton.class;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T create() {
    InstanceCreator buidler = getBuilder();
    InstanceBuilderMetaData medataData = buidler.getMetaData();
    instance = buidler.createInstance(medataData);
    singleton = (T) instance.get();
    buidler.bind(singleton, medataData);
    instance = buidler.completeInstance(instance);
    singleton = (T) instance.get();
    return singleton;
  }
  
  @Override
  public Environment getEnvironment() {
    return env;
  }

  @Override
  public Type getGenericType() {
    return genericType;
  }

  @SuppressWarnings("unchecked")
  @Override
  public InstanceCreator createBuilder() {
    return new InstanceBuilder(env, false, (AbstractProvider<Object>) this);
  }

  @Override
  public Class<? extends T> getResolvedType() {
    return resolvedClazz;
  }

  @Override
  public Type getResolvedGenericType() {
    return resolvedGenericType;
  }

  @Override
  public void stop() {
    if (instance != null) {
      instance.destroy(env);
      singleton = null;
      instance = null;
    }
  }
}
