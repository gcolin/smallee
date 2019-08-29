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

package net.gcolin.rest.util;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.rest.param.Param;
import net.gcolin.rest.parambuilder.ContextParamBuilder;
import net.gcolin.rest.provider.SimpleProviders;
import net.gcolin.rest.server.Contexts;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;

/**
 * Some utility methods for Context parameters.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ContextUtil {

  public static Field[] getContextFields(Object instance) {
    return Reflect.find(instance.getClass(), Reflect.FIELD_ITERATOR,
        x -> x.isAnnotationPresent(Context.class), Reflect.flat(Field.class));
  }

  /**
   * Create injectors.
   * 
   * @param cf field to inject
   * @param providers providers
   * @return injectors
   */
  @SuppressWarnings("unchecked")
  public static Consumer<Object>[] buildContextInjector(Field[] cf, SimpleProviders providers) {
    Consumer<Object>[] injectors = new Consumer[cf.length];
    ContextParamBuilder builder = new ContextParamBuilder(providers);
    for (int i = 0; i < cf.length; i++) {
      Field field = cf[i];
      field.setAccessible(true);
      if (field.getType() == Supplier.class) {
        Type genericType =
            Reflect.getGenericTypeArguments(Supplier.class, field.getGenericType(), null).get(0);
        final Param pa = builder.build(Reflect.toClass(genericType), genericType,
            field.getAnnotations(), false, null);
        injectors[i] = o -> {
          try {
            field.set(o, new Supplier<Object>() {

              @Override
              public Object get() {
                try {
                  return pa.update(Contexts.instance().get());
                } catch (IOException ex) {
                  throw new WebApplicationException(ex);
                }
              }
            });
          } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new WebApplicationException(ex);
          }
        };
      } else {
        final Param p = builder.build(field.getType(), field.getGenericType(),
            field.getAnnotations(), false, null);
        injectors[i] = o -> {
          try {
            field.set(o, p.update(Contexts.instance().get()));
          } catch (IOException | IllegalArgumentException | IllegalAccessException ex) {
            throw new WebApplicationException(ex);
          }
        };
      }
    }
    return injectors;
  }

}
