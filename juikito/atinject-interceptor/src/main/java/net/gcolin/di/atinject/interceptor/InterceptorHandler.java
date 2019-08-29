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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import javax.interceptor.InvocationContext;

import net.gcolin.di.atinject.Instance;

/**
 * InvocationHandler for interceptors.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class InterceptorHandler implements InvocationHandler {

  private Map<Method, InterceptorManager[]> invokes;
  private Map<Method, Method> invokesTranslate;
  private Instance delegate;

  public InterceptorHandler(Map<Method, InterceptorManager[]> invokes, Map<Method, Method> invokesTranslate, Instance delegate) {
    this.invokes = invokes;
    this.invokesTranslate = invokesTranslate;
    this.delegate = delegate;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Method translated = invokesTranslate.get(method);
    InterceptorManager[] interceptors = invokes.get(translated);
    if (interceptors != null) {
      InvocationContext ctx = new MethodInvocationContext(delegate.get(), translated, args);
      for (int i = 0; i < interceptors.length; i++) {
        ctx = new InterceptorInvocationContext(ctx, interceptors[i].getAroundInvoke(),
            delegate.getDependents().get(interceptors[i].getInterceptorType()).get());
      }
      return ctx.proceed();
    } else {
      return CallUtil.call(translated, delegate.get(), args);
    }
  }

}
