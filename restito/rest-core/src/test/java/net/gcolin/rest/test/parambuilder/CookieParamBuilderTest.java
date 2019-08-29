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
import net.gcolin.rest.parambuilder.CookieParamBuilder;
import net.gcolin.rest.server.ServerInvocationContext;
import net.gcolin.rest.server.ServerProviders;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.CookieParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class CookieParamBuilderTest {

  CookieParamBuilder builder;
  ServerProviders providers;

  public static class Bean {

    @CookieParam("hello")
    String hello;
    @CookieParam("hello2")
    Long hello2;
    @CookieParam("hello3")
    String hello3;

  }

  /**
   * Initialize test.
   */
  @Before
  public void before() {
    providers = new ServerProviders();
    providers.load(new Environment());
    builder = new CookieParamBuilder(providers);
  }

  @Test
  public void simpleTest() throws IllegalArgumentException, IllegalAccessException, IOException {
    Param[] array = new Param[4];
    int idx = 0;
    for (Field f : Bean.class.getDeclaredFields()) {
      if (f.isAnnotationPresent(CookieParam.class)) {
        Reflect.enable(f);
        array[idx++] = builder.build(f.getType(), f.getGenericType(), f.getAnnotations(), false,
            f.getAnnotation(CookieParam.class));
      }
    }

    ServerInvocationContext rc = Mockito.mock(ServerInvocationContext.class);
    HttpHeaders headers = Mockito.mock(HttpHeaders.class);
    Mockito.when(rc.getHttpHeaders()).thenReturn(headers);
    Map<String, Cookie> cookies = new HashMap<>();
    cookies.put("hello", new Cookie("hello", "world"));
    cookies.put("hello2", new Cookie("hello2", "3"));
    Mockito.when(headers.getCookies()).thenReturn(cookies);

    Bean bean = new Bean();
    idx = 0;
    for (Field f : Bean.class.getDeclaredFields()) {
      if (f.isAnnotationPresent(CookieParam.class)) {
        f.set(bean, array[idx++].update(rc));
      }
    }
    Assert.assertNotNull(bean.hello);
    Assert.assertNotNull(bean.hello2);
    Assert.assertNull(bean.hello3);

    Assert.assertEquals("world", bean.hello);
    Assert.assertEquals(3L, bean.hello2.longValue());

  }

}
