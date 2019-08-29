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

import net.gcolin.rest.param.Param;
import net.gcolin.rest.parambuilder.BeanParamBuilder;
import net.gcolin.rest.server.AbstractResource;
import net.gcolin.rest.server.ServerInvocationContext;
import net.gcolin.rest.server.ServerProviders;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class BeanParamBuilderTest {

  BeanParamBuilder builder;
  ServerProviders providers;

  /**
   * Initialize test.
   */
  @Before
  public void before() {
    providers = new ServerProviders();
    providers.load();
    builder = new BeanParamBuilder(providers);
  }

  public static class Bean {

    @QueryParam("q")
    transient String qv;

    @QueryParam("q")
    String query;

    String post;

    String post2;
  }

  @Test
  @SuppressWarnings("unchecked")
  public void simpleTest() throws IOException {
    ServerInvocationContext rc = Mockito.mock(ServerInvocationContext.class);
    UriInfo info = Mockito.mock(UriInfo.class);
    Mockito.when(rc.getUriInfo()).thenReturn(info);
    MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
    queryParams.add("q", "hello");
    MessageBodyReader<Object> mr = Mockito.mock(MessageBodyReader.class);
    Mockito.when(mr.readFrom(Mockito.any(Class.class), Mockito.any(Type.class),
        Mockito.any(Annotation[].class), Mockito.any(MediaType.class),
        Mockito.any(MultivaluedMap.class), Mockito.any(InputStream.class))).thenReturn("world");
    Mockito.when(rc.getReader()).thenReturn(mr);
    AbstractResource resource = Mockito.mock(AbstractResource.class);
    Mockito.when(rc.getResource()).thenReturn(resource);
    Mockito.when(info.getQueryParameters()).thenReturn(queryParams);

    Param param = builder.build(Bean.class, Bean.class, new Annotation[0], false, null);
    Bean bean = (Bean) param.update(rc);
    Assert.assertNull(bean.qv);
    Assert.assertNull(bean.post2);
    Assert.assertEquals("hello", bean.query);
    Assert.assertEquals("world", bean.post);
  }

}
