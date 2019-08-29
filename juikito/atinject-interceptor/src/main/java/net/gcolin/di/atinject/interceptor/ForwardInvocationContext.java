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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

import javax.interceptor.InvocationContext;

/**
 * ForwardInvocationContext.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public abstract class ForwardInvocationContext implements InvocationContext {

  private InvocationContext delegate;
  
  public ForwardInvocationContext(InvocationContext delegate) {
    this.delegate = delegate;
  }
  
  public InvocationContext getDelegate() {
    return delegate;
  }

  public Object getTarget() {
    return delegate.getTarget();
  }

  public Object getTimer() {
    return delegate.getTimer();
  }

  public Method getMethod() {
    return delegate.getMethod();
  }

  public Constructor<?> getConstructor() {
    return delegate.getConstructor();
  }

  public Object[] getParameters() {
    return delegate.getParameters();
  }

  public void setParameters(Object[] params) {
    delegate.setParameters(params);
  }

  public Map<String, Object> getContextData() {
    return delegate.getContextData();
  }
  
  
}
