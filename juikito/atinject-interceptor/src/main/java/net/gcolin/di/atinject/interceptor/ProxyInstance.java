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
package net.gcolin.di.atinject.interceptor;

import java.util.Map;

import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.Instance;

/**
 * An instance from a proxy.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ProxyInstance extends Instance {

  private static final long serialVersionUID = -7472591425893981753L;
  private Object proxy;
  private Instance delegate;
  
  public ProxyInstance(Object proxy, Instance delegate) {
    super(delegate.getProvider());
    this.proxy = proxy;
    this.delegate = delegate;
  }
  
  @Override
  public Map<Class<?>, Instance> getDependents() {
    return delegate.getDependents();
  }
  
  @Override
  public void addDependent(Instance instance) {
    delegate.addDependent(instance);
  }
  
  @Override
  public void destroy(Environment env) {
    delegate.destroy(env);
  }
  
  @Override
  public void setValue(Object value) {
    delegate.setValue(value);
  }
  
  @Override
  public Object get() {
    return proxy;
  }

}
