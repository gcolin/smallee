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

package net.gcolin.rest.ext.cdi;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.rest.Environment;

import java.lang.reflect.Type;
import java.util.function.Supplier;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTargetFactory;
import javax.inject.Inject;

/**
 * A REST Environment that uses CDI for getting beans.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class CdiEnvironment extends Environment {

  private BeanManager beanManager;

  public CdiEnvironment(BeanManager beanManager) {
    this.beanManager = beanManager;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Supplier<Object> createProvider(Class<?> impl, Class<?> type, boolean proxy) {
    Type tp = type;
    if (type != impl && type.getTypeParameters().length > 0) {
      tp = Reflect.getGenericType(impl, type);
    }
    AnnotatedType<Object> atype = beanManager.createAnnotatedType((Class<Object>) impl);
    BeanAttributes<Object> attrs = beanManager.createBeanAttributes(atype);
    InjectionTargetFactory<Object> factory = beanManager.getInjectionTargetFactory(atype);
    Bean<Object> bean = beanManager.createBean(attrs, (Class<Object>) impl, factory);
    CreationalContext<Object> ctx = beanManager.createCreationalContext(bean);
    Type tf = tp;
    return decorate(impl, type, () -> beanManager.getReference(bean, tf, ctx), proxy);
  }

  @Override
  public void put(Object obj) {
    AnnotatedType<?> type = beanManager.createAnnotatedType(obj.getClass());
    for (AnnotatedField<?> f : type.getFields()) {
      if (f.isAnnotationPresent(Inject.class)) {
        Reflect.enable(f.getJavaMember());
        if (!f.getJavaMember().getType().isPrimitive()) {
          try {
            f.getJavaMember().set(obj, null);
          } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new InjectionException(ex);
          }
        }
      }
    }
  }

  @Override
  public boolean isMutable(Class<?> clazz) {
    AnnotatedType<?> type = beanManager.createAnnotatedType(clazz);
    return type.isAnnotationPresent(RequestScoped.class)
        || type.isAnnotationPresent(SessionScoped.class);
  }

}
