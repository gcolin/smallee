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

package net.gcolin.rest;

import net.gcolin.common.collection.Func;
import net.gcolin.common.reflect.Reflect;
import net.gcolin.common.reflect.TypedInvocationHandler;
import net.gcolin.rest.param.Param;
import net.gcolin.rest.parambuilder.ContextParamBuilder;
import net.gcolin.rest.provider.SimpleProviders;
import net.gcolin.rest.server.Contexts;
import net.gcolin.rest.server.ServerInvocationContext;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Context;

/**
 * A simple bean provider.
 * 
 * <p>
 * It injects the context parameters.
 * </p>
 * 
 * <p>
 * It can be extended in order to use a dependency injection implementation. To see how, look at the
 * CDIEnvironment.
 * </p>
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class Environment {

  private Map<Class<?>, Map<Class<?>, Supplier<Object>>> cache =
      Collections.synchronizedMap(new WeakHashMap<>());
  private SimpleProviders providers;

  public Supplier<Object> createProvider(final Class<?> impl, final Class<?> clazz, boolean proxy) {
    return decorate(impl, clazz, supplier(impl), proxy);
  }

  private Supplier<Object> supplier(final Class<?> impl) {
    return () -> Reflect.newInstance(impl);
  }

  /**
   * Get a provider of a class.
   * 
   * @param impl the internal implementation type
   * @param clazz the mandatory type of the object generated from the provider
   * @param proxy support proxy
   * @return a factory
   */
  public final Supplier<Object> getProvider(final Class<?> impl, final Class<?> clazz,
      boolean proxy) {
    Map<Class<?>, Supplier<Object>> map = cache.get(impl);
    if (map == null) {
      map = new ConcurrentHashMap<>();
      cache.put(impl, map);
    }

    Supplier<Object> factory = map.get(clazz);
    if (factory == null) {
      factory = createProvider(impl, clazz, proxy);
      map.put(clazz, factory);
    }
    return factory;
  }

  public boolean isMutable(Class<?> clazz) {
    return false;
  }

  /**
   * Decorate an instance. Inject the context parameter fields.
   * 
   * @param <T> the desired type
   * @param instance an object
   * @param type the mandatory type to return (the contract type)
   * @return a decorated object
   */
  @SuppressWarnings("unchecked")
  public <T> T decorate(Object instance, Class<T> type) {
    List<RestInjection> list = findContextInjections(instance.getClass());
    if (list.isEmpty()) {
      return (T) instance;
    }

    if (hasContextual(list)) {
      if (hasOnlyInterface(list)) {
        return (T) proxyRestInjection(instance, list);
      } else {
        return (T) proxySupplier(instance.getClass(), type, supplier(instance.getClass()), list)
            .get();
      }
    } else {
      return decorate(instance, list);
    }

  }

  @SuppressWarnings("unchecked")
  private <T> T decorate(Object instance, List<RestInjection> list) {
    for (int i = 0; i < list.size(); i++) {
      list.get(i).update(instance, null);
    }
    return (T) instance;
  }
  
  public <T> T decorate(T instance) {
    return decorate(instance, findContextInjections(instance.getClass()));
  }

  /**
   * A provider of a decorated instance. Inject the context parameter fields.
   * 
   * @param impl the implementation type
   * @param type the mandatory type to return (the contract type)
   * @param provider a delegate provider
   * @param proxy supports proxy
   * @return a decorated provider
   */
  public Supplier<Object> decorate(Class<?> impl, Class<?> type, Supplier<Object> provider,
      boolean proxy) {

    List<RestInjection> list = findContextInjections(impl);
    Supplier<Object> result = provider;

    if (proxy) {
      if (isMutable(impl) || hasContextual(list)) {
        result = proxySupplier(impl, type, provider, list);
      } else {
        final Object instance = decorate(result.get(), list);
        result = () -> instance;
      }
    } else if (isMutable(impl) || hasContextual(list)) {
      result = decorateSupplier(result, list);
    }
    return result;
  }

  private Supplier<Object> proxySupplier(Class<?> impl, Class<?> type, Supplier<Object> provider,
      List<RestInjection> list) {
    Supplier<Object> ds = decorateSupplier(provider, list);
    return () -> Proxy.newProxyInstance(type.getClassLoader(), new Class[] {type},
        new TypedInvocationHandler(new InvocationHandler() {

          @Override
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return method.invoke(ds.get(), args);
          }
        }, impl));
  }

  private static class RestInjectionInvocationHandler implements InvocationHandler {

    private Param param;

    public RestInjectionInvocationHandler(Param param) {
      this.param = param;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      return method.invoke(param.update(Contexts.instance().get()), args);
    }

  }

  private <T> T proxyRestInjection(T instance, List<RestInjection> list) {
    for (int i = 0; i < list.size(); i++) {
      RestInjection ri = list.get(i);
      try {
        ri.field.set(instance, Proxy.newProxyInstance(ri.param.getClass().getClassLoader(),
            new Class[] {ri.field.getType()}, new RestInjectionInvocationHandler(ri.param)));
      } catch (IllegalAccessException ex) {
        throw new ProcessingException(ex);
      }
    }
    return (T) instance;
  }

  private Supplier<Object> decorateSupplier(Supplier<Object> provider, List<RestInjection> list) {
    RestInjection[] array = new RestInjection[list.size()];
    list.toArray(array);
    return new RestDecorateProvider(array, provider);
  }

  private boolean hasContextual(List<RestInjection> list) {
    return Func.find(list, x -> x.param.isContextual()) != null;
  }

  private boolean hasOnlyInterface(List<RestInjection> list) {
    return list.stream().anyMatch(x -> x.field.getType().isInterface());
  }

  private List<RestInjection> findContextInjections(Class<?> impl) {
    Class<?> clazz = impl;
    List<RestInjection> list = new ArrayList<>();
    while (clazz != Object.class) {
      for (Field field : clazz.getDeclaredFields()) {
        if (Reflect.hasAnnotation(field.getAnnotations(), Context.class)) {
          Reflect.enable(field);
          Param param =
              new ContextParamBuilder(providers).build(field.getType(), field.getGenericType(),
                  field.getAnnotations(), false, field.getAnnotation(Context.class));
          list.add(new RestInjection(field, param));
        }
      }
      clazz = clazz.getSuperclass();
    }
    return list;
  }

  private static class RestDecorateProvider implements Supplier<Object> {
    private RestInjection[] array;
    private Supplier<Object> delegate;

    public RestDecorateProvider(RestInjection[] array, Supplier<Object> delegate) {
      this.array = array;
      this.delegate = delegate;
    }

    @Override
    public Object get() {
      try {
        Object obj = delegate.get();
        ServerInvocationContext context = Contexts.instance().get();
        for (int i = 0; i < array.length; i++) {
          array[i].update(obj, context);
        }
        return obj;
      } catch (Exception ex) {
        throw new ProcessingException(ex);
      }
    }
  }

  private static class RestInjection {

    private Field field;
    private Param param;

    public RestInjection(Field field, Param param) {
      this.field = field;
      this.param = param;
    }

    public void update(Object obj, ServerInvocationContext context) {
      try {
        if (field.getType().isPrimitive() || field.get(obj) == null) {
          field.set(obj, param.update(context));
        }
      } catch (IllegalAccessException | IOException ex) {
        throw new ProcessingException(ex);
      }
    }
  }

  public ClassLoader getClassLoader() {
    return this.getClass().getClassLoader();
  }

  public void setProviders(SimpleProviders providers) {
    this.providers = providers;
  }

  public void put(Object obj) {
    // cannot add to environment
  }

}
