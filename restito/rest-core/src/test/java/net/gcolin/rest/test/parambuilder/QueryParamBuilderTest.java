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
import net.gcolin.rest.parambuilder.QueryParamBuilder;
import net.gcolin.rest.server.ServerInvocationContext;
import net.gcolin.rest.server.ServerProviders;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;

import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class QueryParamBuilderTest {

  QueryParamBuilder builder;
  ServerProviders providers;

  public static class Bean {

    @QueryParam("hello")
    String hello;
    @QueryParam("hello2")
    Long hello2;
    @QueryParam("hello3")
    String hello3;

  }

  /**
   * Initialize test.
   */
  @Before
  public void before() {
    providers = new ServerProviders();
    providers.load(new Environment());
    builder = new QueryParamBuilder(providers);
  }

  @Test
  public void simpleTest() throws IllegalArgumentException, IllegalAccessException, IOException {
    Param[] array = new Param[4];
    int idx = 0;
    for (Field field : Bean.class.getDeclaredFields()) {
      if (field.isAnnotationPresent(QueryParam.class)) {
        Reflect.enable(field);
        array[idx++] = builder.build(field.getType(), field.getGenericType(),
            field.getAnnotations(), false, field.getAnnotation(QueryParam.class));
      }
    }

    ServerInvocationContext rc = Mockito.mock(ServerInvocationContext.class);
    UriInfo info = Mockito.mock(UriInfo.class);
    MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();
    Mockito.when(info.getQueryParameters()).thenReturn(params);
    Mockito.when(rc.getUriInfo()).thenReturn(info);

    params.add("hello", "world");
    params.add("hello2", "3");

    Bean bean = new Bean();
    idx = 0;
    for (Field field : Bean.class.getDeclaredFields()) {
      if (field.isAnnotationPresent(QueryParam.class)) {
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
