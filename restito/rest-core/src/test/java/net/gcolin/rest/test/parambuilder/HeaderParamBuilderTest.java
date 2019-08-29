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
import net.gcolin.rest.parambuilder.HeaderParamBuilder;
import net.gcolin.rest.server.ServerInvocationContext;
import net.gcolin.rest.server.ServerProviders;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;

import javax.ws.rs.HeaderParam;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class HeaderParamBuilderTest {

  HeaderParamBuilder builder;
  ServerProviders providers;

  public static class Bean {

    @HeaderParam("hello")
    String hello;
    @HeaderParam("hello2")
    Long hello2;
    @HeaderParam("hello3")
    String hello3;

  }

  /**
   * Initialize test.
   */
  @Before
  public void before() {
    providers = new ServerProviders();
    providers.load(new Environment());
    builder = new HeaderParamBuilder(providers);
  }

  @Test
  public void simpleTest() throws IllegalArgumentException, IllegalAccessException, IOException {
    Param[] array = new Param[3];
    int idx = 0;
    for (Field field : Bean.class.getDeclaredFields()) {
      if (field.isAnnotationPresent(HeaderParam.class)) {
        Reflect.enable(field);
        array[idx++] = builder.build(field.getType(), field.getGenericType(),
            field.getAnnotations(), false, field.getAnnotation(HeaderParam.class));
      }
    }

    ServerInvocationContext rc = Mockito.mock(ServerInvocationContext.class);
    Mockito.when(rc.getHeaderString("hello")).thenReturn("world");
    Mockito.when(rc.getHeaderString("hello2")).thenReturn("3");

    Bean bean = new Bean();
    idx = 0;
    for (Field field : Bean.class.getDeclaredFields()) {
      if (field.isAnnotationPresent(HeaderParam.class)) {
        field.set(bean, array[idx++].update(rc));
      }
    }
    Assert.assertNotNull(bean.hello);
    Assert.assertNotNull(bean.hello2);
    Assert.assertNull(bean.hello3);

    Assert.assertEquals("world", bean.hello);
    Assert.assertEquals(3L, bean.hello2.longValue());

  }

}
