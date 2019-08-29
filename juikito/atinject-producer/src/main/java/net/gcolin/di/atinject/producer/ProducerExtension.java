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
package net.gcolin.di.atinject.producer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.inject.Provider;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.di.atinject.AbstractProvider;
import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.Extension;
import net.gcolin.di.atinject.InstanceBuilder;
import net.gcolin.di.atinject.ProviderBuilder;
import net.gcolin.di.atinject.Reflects;
import net.gcolin.di.atinject.SupplierProvider;
import net.gcolin.di.core.InjectException;
import net.gcolin.di.core.Key;

/**
 * Enable Produces/Disposes annotations.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ProducerExtension implements Extension {

  private Set<Class<? extends Annotation>> producesAnnotations = new HashSet<>();
  private Set<Class<? extends Annotation>> disposesAnnotations = new HashSet<>();

  public ProducerExtension() {
    producesAnnotations.add(Produces.class);
    disposesAnnotations.add(Disposes.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void doStarted(Environment environment) {
    Map<Key, Method> disposes = new HashMap<>();

    for (Class<?> cl : environment.getBeanClasses()) {
      for (Method method : cl.getDeclaredMethods()) {
        if (!Modifier.isStatic(method.getModifiers()) && method.getParameterCount() == 1
            && Reflects.hasAnnotation(method.getParameterAnnotations()[0], disposesAnnotations)) {
          Reflect.enable(method);
          disposes.put(environment.createKey(method.getParameterTypes()[0],
              method.getGenericParameterTypes()[0],
              environment.findQualifiers(method.getParameterAnnotations()[0])), method);
        }
      }
    }

    for (Class<?> cl : environment.getBeanClasses()) {
      for (Field field : cl.getDeclaredFields()) {
        if (!Modifier.isStatic(field.getModifiers())
            && Reflects.hasAnnotation(field.getAnnotations(), producesAnnotations)) {
          Reflect.enable(field);
          Provider<Object> parent = (Provider<Object>) environment.getProvider(cl);

          Key key = environment.createKey(field.getType(), field.getGenericType(),
              environment.findQualifiers(field.getAnnotations()));
          Method dispose = disposes.get(key);

          AbstractProvider<Object> fieldProvider;

          if (dispose == null) {
            fieldProvider =
                new SupplierProvider<>((Class<Object>) field.getType(), new Supplier<Object>() {

                  @Override
                  public Object get() {
                    try {
                      return field.get(parent.get());
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                      throw new InjectException(ex);
                    }
                  }

                }, null, environment);
          } else {
            fieldProvider = new SupplierProvider<Object>((Class<Object>) field.getType(),
                new Supplier<Object>() {

                  @Override
                  public Object get() {
                    try {
                      return field.get(parent.get());
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                      throw new InjectException(ex);
                    }
                  }

                }, null, environment) {
              @Override
              public boolean hasDestroyMethods() {
                return true;
              }

              @Override
              public void destroyInstance(Object o) {
                try {
                  dispose.invoke(parent.get(), o);
                } catch (IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException ex) {
                  throw new InjectException(ex);
                }
              }
            };
          }


          ProviderBuilder scope = environment.getScope(field.getAnnotations());
          environment.addProvider(key, field.getType(),
              scope == null ? fieldProvider : scope.decorate(fieldProvider));
        }
      }
    }

    for (Class<?> cl : environment.getBeanClasses()) {
      for (Method method : cl.getDeclaredMethods()) {
        if (!Modifier.isStatic(method.getModifiers())
            && Reflects.hasAnnotation(method.getAnnotations(), producesAnnotations)) {
          Reflect.enable(method);
          Provider<Object> parent = (Provider<Object>) environment.getProvider(cl);

          Key key = environment.createKey(method.getReturnType(), method.getGenericReturnType(),
              environment.findQualifiers(method.getAnnotations()));
          Method dispose = disposes.get(key);
          AbstractProvider<?>[] args = InstanceBuilder.findProviders(method.getParameterTypes(),
              method.getGenericParameterTypes(), method.getParameterAnnotations(), environment);

          AbstractProvider<Object> methodProvider;
          if (dispose == null) {
            methodProvider = new SupplierProvider<>((Class<Object>) method.getReturnType(),
                new Supplier<Object>() {

                  @Override
                  public Object get() {
                    try {
                      return method.invoke(parent.get(), InstanceBuilder.getArguments(args));
                    } catch (IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException ex) {
                      throw new InjectException(ex);
                    }
                  }

                }, null, environment);
          } else {
            methodProvider = new SupplierProvider<Object>((Class<Object>) method.getReturnType(),
                new Supplier<Object>() {

                  @Override
                  public Object get() {
                    try {
                      return method.invoke(parent.get(), InstanceBuilder.getArguments(args));
                    } catch (IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException ex) {
                      throw new InjectException(ex);
                    }
                  }

                }, null, environment) {
              @Override
              public boolean hasDestroyMethods() {
                return true;
              }

              @Override
              public void destroyInstance(Object o) {
                try {
                  dispose.invoke(parent.get(), o);
                } catch (IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException ex) {
                  throw new InjectException(ex);
                }
              }
            };
          }

          ProviderBuilder scope = environment.getScope(method.getAnnotations());
          environment.addProvider(key, method.getReturnType(),
              scope == null ? methodProvider : scope.decorate(methodProvider));
        }
      }
    }
  }

  public Set<Class<? extends Annotation>> getDisposesAnnotations() {
    return disposesAnnotations;
  }

  public Set<Class<? extends Annotation>> getProducesAnnotations() {
    return producesAnnotations;
  }

}
