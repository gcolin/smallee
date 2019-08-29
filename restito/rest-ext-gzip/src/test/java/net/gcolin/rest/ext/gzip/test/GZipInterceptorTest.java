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

package net.gcolin.rest.ext.gzip.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.ext.ReaderInterceptorContext;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import net.gcolin.common.io.Io;
import net.gcolin.common.reflect.BeanAccess;
import net.gcolin.rest.ext.gzip.GZipInterceptor;

/**
 * Test GZipInterceptor.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class GZipInterceptorTest {

  int nb;
  OutputStream bout;
  InputStream bin;

  @Before
  public void before() {
    nb = 0;
  }

  @Test
  public void aroundWriteToTest() throws WebApplicationException, IOException {
    WriterInterceptorContext ctx = Mockito.mock(WriterInterceptorContext.class);
    MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
    headers.add(HttpHeaders.ACCEPT_ENCODING, "gzip");
    Mockito.when(ctx.getHeaders()).thenReturn(headers);
    bout = new ByteArrayOutputStream();
    Mockito.when(ctx.getOutputStream()).then(new Answer<OutputStream>() {

      @Override
      public OutputStream answer(InvocationOnMock invocation) throws Throwable {
        return bout;
      }
    });
    Mockito.doAnswer(new Answer<Object>() {

      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        bout = (OutputStream) invocation.getArguments()[0];
        return null;
      }
    }).when(ctx).setOutputStream(Mockito.any(OutputStream.class));

    Mockito.doAnswer(new Answer<Object>() {

      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        nb++;
        ctx.getOutputStream().write("hello".getBytes(StandardCharsets.UTF_8));
        return null;
      }
    }).when(ctx).proceed();
    GZipInterceptor interceptor = new GZipInterceptor();
    BeanAccess.setProperty(interceptor, "requestHeaders", new Supplier<HttpHeaders>() {

      @Override
      public HttpHeaders get() {
        HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);
        Mockito.when(httpHeaders.getHeaderString(HttpHeaders.ACCEPT_ENCODING)).thenReturn("gzip");
        return httpHeaders;
      }
      
    });
    interceptor.aroundWriteTo(ctx);
    Assert.assertEquals(1, nb);

    Assert.assertEquals("gzip", headers.getFirst(HttpHeaders.CONTENT_ENCODING));
    byte[] out = ((ByteArrayOutputStream) bout).toByteArray();

    Assert.assertEquals("hello",
        Io.toString(new GZIPInputStream(new ByteArrayInputStream(out), out.length)));
  }


  @Test
  public void aroundWriteToNothingTest() throws WebApplicationException, IOException {
    WriterInterceptorContext ctx = Mockito.mock(WriterInterceptorContext.class);
    MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
    Mockito.when(ctx.getHeaders()).thenReturn(headers);

    Mockito.doAnswer(new Answer<Object>() {

      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        nb++;
        return null;
      }
    }).when(ctx).proceed();
    GZipInterceptor interceptor = new GZipInterceptor();
    BeanAccess.setProperty(interceptor, "requestHeaders", new Supplier<HttpHeaders>() {

      @Override
      public HttpHeaders get() {
        HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);
        Mockito.when(httpHeaders.getHeaderString(Mockito.anyString())).thenReturn(null);
        return httpHeaders;
      }
      
    });
    interceptor.aroundWriteTo(ctx);
    Assert.assertEquals(1, nb);
  }

  @Test
  public void aroundReadFromNothingTest() throws WebApplicationException, IOException {
    ReaderInterceptorContext ctx = Mockito.mock(ReaderInterceptorContext.class);
    MultivaluedHashMap<String, String> headers = new MultivaluedHashMap<>();
    Mockito.when(ctx.getHeaders()).thenReturn(headers);

    Mockito.doAnswer(new Answer<Object>() {

      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        nb++;
        return null;
      }
    }).when(ctx).proceed();
    new GZipInterceptor().aroundReadFrom(ctx);
    Assert.assertEquals(1, nb);
  }

  @Test
  public void aroundReadFromTest() throws WebApplicationException, IOException {
    ReaderInterceptorContext ctx = Mockito.mock(ReaderInterceptorContext.class);
    MultivaluedHashMap<String, String> headers = new MultivaluedHashMap<>();
    headers.add(HttpHeaders.CONTENT_ENCODING, "gzip");
    Mockito.when(ctx.getHeaders()).thenReturn(headers);

    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    GZIPOutputStream gout = new GZIPOutputStream(bout);
    gout.write("hello".getBytes(StandardCharsets.UTF_8));
    gout.close();

    bin = new ByteArrayInputStream(bout.toByteArray());
    Mockito.when(ctx.getInputStream()).then(new Answer<InputStream>() {

      @Override
      public InputStream answer(InvocationOnMock invocation) throws Throwable {
        return bin;
      }
    });
    Mockito.doAnswer(new Answer<Object>() {

      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        bin = (InputStream) invocation.getArguments()[0];
        return null;
      }
    }).when(ctx).setInputStream(Mockito.any(InputStream.class));

    Mockito.doAnswer(new Answer<Object>() {

      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        nb++;
        Assert.assertEquals("hello", Io.toString(ctx.getInputStream()));
        return null;
      }
    }).when(ctx).proceed();
    new GZipInterceptor().aroundReadFrom(ctx);
    Assert.assertEquals(1, nb);

    Assert.assertNull(headers.getFirst(HttpHeaders.CONTENT_ENCODING));
  }

}
