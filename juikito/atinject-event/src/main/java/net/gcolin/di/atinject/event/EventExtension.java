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

package net.gcolin.di.atinject.event;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import net.gcolin.di.atinject.AbstractProvider;
import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.Extension;
import net.gcolin.di.atinject.Reflects;
import net.gcolin.di.atinject.jmx.JmxExtension;

/**
 * Enable events.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class EventExtension implements Extension {

  private Events events;

  private Set<Class<? extends Annotation>> observesType = new HashSet<>();
  
  private Set<Class<? extends Annotation>> asyncType = new HashSet<>();

  public EventExtension() {
    observesType.add(Observes.class);
    asyncType.add(Async.class);
  }

  @Override
  public void doStart(Environment environment) {
    events = new Events(environment, environment.getExtension(JmxExtension.class));
    environment.addResolver(events);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void doStarted(Environment environment) {
    for (Class<?> clazz : environment.getBeanClasses()) {
      for (Method method : clazz.getDeclaredMethods()) {
        if (method.getParameterCount() == 1
            && Reflects.hasAnnotation(method.getParameterAnnotations()[0], observesType)) {
          events.observes(method,
              (AbstractProvider<Object>) environment.getProvider(method.getDeclaringClass()),
              Reflects.getAnnotation(method.getParameterAnnotations()[0], asyncType));
        }
      }
    }
  }
  
  @Override
  public void doStopped(Environment environment) {
    events.close();
  }

  public Set<Class<? extends Annotation>> getObservesType() {
    return observesType;
  }
  
  public Set<Class<? extends Annotation>> getAsyncType() {
    return asyncType;
  }

  public Events getEvents() {
    return events;
  }
  
}
