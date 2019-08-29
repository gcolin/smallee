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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;

import net.gcolin.di.core.Key;

/**
 * A serializable object that hold an instance and the dependent instances.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class Instance implements Serializable, Provider<Object> {

  private static final long serialVersionUID = -7072830052236033667L;
  private Object value;
  private final Key key;
  private transient AbstractProvider<Object> provider;
  private Map<Class<?>, Instance> dependents = null;

  public Instance(Object value, AbstractProvider<Object> provider) {
    this.value = value;
    this.key = provider.getKey();
    this.provider = provider;
  }
  
  public Instance(AbstractProvider<Object> provider) {
    this.key = provider.getKey();
    this.provider = provider;
  }
  
  public void setValue(Object value) {
    this.value = value;
  }

  public Object get() {
    return value;
  }

  public Key getKey() {
    return key;
  }

  public void addDependent(Instance instance) {
    if (dependents == null) {
      dependents = new HashMap<>();
    }
    dependents.put(instance.getProvider().getType(), instance);
  }

  public Map<Class<?>, Instance> getDependents() {
    return dependents == null ? Collections.emptyMap() : dependents;
  }
  
  public AbstractProvider<Object> getProvider() {
    return provider;
  }
  
  public void destroy(Environment env) {
    if (dependents != null) {
      for(Instance dependent : dependents.values()) {
        dependent.destroy(env);
      }
    }
    if(provider == null) {
      provider = env.getProvider(key);
    }
    InstanceCreator creator = provider.getBuilder();
    if(creator != null) {
      creator.destroyInstance(value);
    }
  }
}
