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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.interceptor.Interceptor;

import net.gcolin.di.atinject.AbstractProvider;
import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.Extension;

/**
 * Enable interceptors.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class InterceptorExtension implements Extension {

  @SuppressWarnings("unchecked")
  @Override
  public void doStarted(Environment environment) {
    List<InterceptorManager> interceptors = new ArrayList<>();
    for (Class<?> type : environment.getBeanClasses()) {
      if (type.isAnnotationPresent(Interceptor.class)) {
        interceptors.add(
            new InterceptorManager(type, (AbstractProvider<Object>) environment.getProvider(type)));
      }
    }
    Collections.sort(interceptors, (a,b) -> {
      int n = b.getPriority()  - a.getPriority();
      if(n > 0) {
        return 1;
      }else if(n < 0) {
        return -1;
      }
      return 0;
    });
    environment.addDecoratorBuilders(new InterceptorDecorator(interceptors));
  }

}
