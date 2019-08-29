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
import net.gcolin.rest.MessageBodyReaderDecorator;
import net.gcolin.rest.server.ServerInvocationContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class MessageBodyReaderDecoratorTest {

  ReaderInterceptor interceptor;
  ReaderInterceptor interceptor2;
  MessageBodyReaderDecorator decorator;
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
    ctx.setEntityStream(new ByteArrayInputStream(new byte[0]));
    interceptor = Mockito.mock(ReaderInterceptor.class);
    interceptor2 = Mockito.mock(ReaderInterceptor.class);
    decorator = new MessageBodyReaderDecorator();
    decorator.add(interceptor);
    decorator.add(interceptor2);
    ctx.setReader(Mockito.mock(MessageBodyReader.class));
    ctx.setConsume(FastMediaType.valueOf(MediaType.APPLICATION_JSON_TYPE));
  }

  @Test
  public void test() throws IOException {
    Mockito.when(interceptor.aroundReadFrom(Mockito.any(ReaderInterceptorContext.class)))
        .thenAnswer(new Answer<Object>() {

          @Override
          public Object answer(InvocationOnMock invocation) throws Throwable {
            ReaderInterceptorContext rc = (ReaderInterceptorContext) invocation.getArguments()[0];
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

            Assert.assertTrue(rc.getInputStream() instanceof ByteArrayInputStream);
            rc.setInputStream(new net.gcolin.common.io.ByteArrayInputStream(new byte[0]));
            Assert.assertTrue(
                rc.getInputStream() instanceof net.gcolin.common.io.ByteArrayInputStream);
            Assert.assertTrue(rc.getHeaders().isEmpty());

            return rc.proceed();
          }
        });

    Mockito.when(interceptor2.aroundReadFrom(Mockito.any(ReaderInterceptorContext.class)))
        .thenAnswer(new Answer<Object>() {

          @Override
          public Object answer(InvocationOnMock invocation) throws Throwable {
            ReaderInterceptorContext rc = (ReaderInterceptorContext) invocation.getArguments()[0];
            return rc.proceed();
          }
        });

    decorator.readFrom(ctx, new Annotation[0], String.class, String.class,
        new MultivaluedHashMap<>());
  }

}
