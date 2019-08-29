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

package net.gcolin.rest.test.parambuilder;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.rest.Environment;
import net.gcolin.rest.param.Param;
import net.gcolin.rest.parambuilder.ContextParamBuilder;
import net.gcolin.rest.provider.SingletonSupplier;
import net.gcolin.rest.server.Contexts;
import net.gcolin.rest.server.ServerInvocationContext;
import net.gcolin.rest.server.ServerProviders;
import net.gcolin.rest.test.Icon;
import net.gcolin.rest.test.Point;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.function.Supplier;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ContextParamBuilderTest {

  ContextParamBuilder builder;
  ServerProviders providers;

  public static class StringContextResolver implements ContextResolver<String> {

    @Override
    public String getContext(Class<?> type) {
      return type.getSimpleName();
    }

  }

  public static class PointSupplier implements Supplier<Point> {

    @Override
    public Point get() {
      return new Point();
    }

  }

  public static class LongSupplier extends SingletonSupplier<Long> {

    public LongSupplier(Long val) {
      super(val);
    }

  }

  public static class Bean {

    @Context
    ContextResolver<String> stringcontext;
    @Context
    Supplier<Locale> localeProvider;
    @Context
    Locale locale;
    @Context
    Supplier<UriInfo> uriInfoProvider;
    @Context
    Point point;
    @Context
    Supplier<Point> pointProvider;
    @Context
    Supplier<Long> longProvider;

  }

  public static class Bean2 {
    @Context
    Supplier<Icon> iconProvider;
  }

  /**
   * Initialize test.
   */
  @Before
  public void before() {
    providers = new ServerProviders();
    providers.load(new Environment());
    providers.add(new PointSupplier());
    providers.add(new LongSupplier(3L));
    providers.add(new StringContextResolver(), String.class);
    builder = new ContextParamBuilder(providers);
  }

  @Test
  public void badArgumentTest() throws IllegalAccessException, IOException {
    try {
      builder.build(String.class, String.class, new Annotation[0], false, null);
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      // ok
    }
  }

  @Test
  public void simpleTest() throws IllegalArgumentException, IllegalAccessException, IOException,
      NoSuchFieldException, SecurityException {
    try {
      Field field = Bean2.class.getDeclaredField("iconProvider");
      builder.build(field.getType(), field.getGenericType(), new Annotation[0], false, null);
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      // ok
    }

    Param[] array = new Param[7];
    int idx = 0;
    for (Field field : Bean.class.getDeclaredFields()) {
      if (field.isAnnotationPresent(Context.class)) {
        Reflect.enable(field);
        array[idx++] = builder.build(field.getType(), field.getGenericType(),
            field.getAnnotations(), false, field.getAnnotation(Context.class));
      }
    }

    ServerInvocationContext rc = Mockito.mock(ServerInvocationContext.class);
    Mockito.when(rc.getLanguage()).thenReturn(Locale.FRENCH);
    UriInfo info = Mockito.mock(UriInfo.class);
    Mockito.when(rc.getUriInfo()).thenReturn(info);
    Contexts.instance().set(rc);

    Bean bean = new Bean();
    idx = 0;
    for (Field field : Bean.class.getDeclaredFields()) {
      if (field.isAnnotationPresent(Context.class)) {
        field.set(bean, array[idx++].update(rc));
      }
    }
    Assert.assertNotNull(bean.stringcontext);
    Assert.assertNotNull(bean.localeProvider);
    Assert.assertNotNull(bean.locale);
    Assert.assertNotNull(bean.uriInfoProvider);

    Assert.assertEquals("Bean", bean.stringcontext.getContext(Bean.class));
    Assert.assertEquals(Locale.FRENCH, bean.localeProvider.get());
    Assert.assertEquals(Locale.FRENCH, bean.locale);
    Assert.assertEquals(info, bean.uriInfoProvider.get());
    Assert.assertTrue(bean.pointProvider.get().getClass() == Point.class);
    Assert.assertTrue(bean.point.getClass() == Point.class);
    Assert.assertEquals(3L, bean.longProvider.get().longValue());

  }

}
