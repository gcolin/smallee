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

package net.gcolin.rest.test;

import net.gcolin.rest.FastMediaType;
import net.gcolin.rest.MessageBodyWriterDecorator;
import net.gcolin.rest.server.ServerInvocationContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class MessageBodyWriterDecoratorTest {

  WriterInterceptor interceptor;
  WriterInterceptor interceptor2;
  MessageBodyWriterDecorator decorator;
  ServerInvocationContext ctx;

  @Retention(RetentionPolicy.RUNTIME)
  @interface Annotation1 {

  }

  @Annotation1
  public static class A {

  }

  /**
   * Initialize test.
   */
  @SuppressWarnings("unchecked")
  @Before
  public void before() {
    ctx = new ServerInvocationContext(null);
    interceptor = Mockito.mock(WriterInterceptor.class);
    interceptor2 = Mockito.mock(WriterInterceptor.class);
    decorator = new MessageBodyWriterDecorator();
    decorator.add(interceptor);
    decorator.add(interceptor2);
    ctx.setWriter(Mockito.mock(MessageBodyWriter.class));
    ctx.setProduce(FastMediaType.valueOf(MediaType.APPLICATION_JSON_TYPE));
  }

  @Test
  public void test() throws IOException {
    Mockito.doAnswer(new Answer<Object>() {

      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        WriterInterceptorContext rc = (WriterInterceptorContext) invocation.getArguments()[0];
        Assert.assertNull(ctx.getProperty("hello"));
        Assert.assertNull(rc.getProperty("hello"));
        rc.setProperty("hello", "world");
        Assert.assertEquals("world", ctx.getProperty("hello"));
        Assert.assertEquals("world", rc.getProperty("hello"));
        Assert.assertEquals(1, rc.getPropertyNames().size());
        Assert.assertTrue(rc.getPropertyNames().contains("hello"));
        rc.removeProperty("hello");
        Assert.assertTrue(rc.getPropertyNames().isEmpty());

        Assert.assertEquals(0, rc.getAnnotations().length);
        rc.setAnnotations(A.class.getAnnotations());
        Assert.assertEquals(1, rc.getAnnotations().length);

        Assert.assertEquals(String.class, rc.getType());
        rc.setType(A.class);
        Assert.assertEquals(A.class, rc.getType());

        Assert.assertEquals(String.class, rc.getGenericType());
        rc.setGenericType(A.class);
        Assert.assertEquals(A.class, rc.getGenericType());

        Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, rc.getMediaType());
        rc.setMediaType(MediaType.APPLICATION_ATOM_XML_TYPE);
        Assert.assertEquals(MediaType.APPLICATION_ATOM_XML_TYPE, rc.getMediaType());

        Assert.assertTrue(rc.getOutputStream() instanceof ByteArrayOutputStream);
        rc.setOutputStream(new net.gcolin.common.io.ByteArrayOutputStream());
        Assert
            .assertTrue(rc.getOutputStream() instanceof net.gcolin.common.io.ByteArrayOutputStream);
        Assert.assertTrue(rc.getHeaders().isEmpty());

        Assert.assertEquals("hello", rc.getEntity());
        rc.setEntity("hello world");
        Assert.assertEquals("hello world", rc.getEntity());

        rc.proceed();
        return null;
      }
    }).when(interceptor).aroundWriteTo(Mockito.any(WriterInterceptorContext.class));

    Mockito.doAnswer(new Answer<Object>() {

      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        WriterInterceptorContext rc = (WriterInterceptorContext) invocation.getArguments()[0];
        rc.proceed();
        return null;
      }
    }).when(interceptor2).aroundWriteTo(Mockito.any(WriterInterceptorContext.class));

    decorator.writeTo(ctx, "hello", String.class, String.class, new Annotation[0],
        new MultivaluedHashMap<>(), new ByteArrayOutputStream());
  }

}
