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

package net.gcolin.rest.test.provider;

import net.gcolin.rest.provider.SimpleProviders;
import net.gcolin.rest.test.Point;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class SimpleProvidersTest {
  SimpleProviders provider;

  @Before
  public void before() {
    provider = new SimpleProviders(RuntimeType.SERVER);
    provider.load();
  }

  @Test
  public void addMessageBodyReaderTest() {
    MessageBodyReader<String> mr = new MessageBodyReader<String>() {

      @Override
      public String readFrom(Class<String> type, Type genericType, Annotation[] annotations,
          MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
          throws IOException, WebApplicationException {
        return null;
      }

      @Override
      public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations,
          MediaType mediaType) {
        return true;
      }
    };
    provider.add(mr);

    Assert.assertEquals(mr, provider.getMessageBodyReader(Point.class, Point.class,
        new Annotation[0], MediaType.APPLICATION_XHTML_XML_TYPE));
  }

  @Test
  public void addMessageBodyWriterTest() {
    MessageBodyWriter<String> mw = new MessageBodyWriter<String>() {

      @Override
      public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
          MediaType mediaType) {
        return true;
      }

      @Override
      public long getSize(String str, Class<?> type, Type genericType, Annotation[] annotations,
          MediaType mediaType) {
        return 0;
      }

      @Override
      public void writeTo(String str, Class<?> type, Type genericType, Annotation[] annotations,
          MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
          OutputStream entityStream) throws IOException, WebApplicationException {}

    };
    provider.add(mw);

    Assert.assertEquals(mw, provider.getMessageBodyWriter(Point.class, Point.class,
        new Annotation[0], MediaType.APPLICATION_XHTML_XML_TYPE));
  }

  @Test
  public void noMessageBodyWriterTest() {
    try {
      provider.getMessageBodyWriter(Point.class, Point.class, new Annotation[0],
          MediaType.APPLICATION_XHTML_XML_TYPE);
      Assert.fail();
    } catch (ProcessingException ex) {
      // ok
    }
  }

  @Test
  public void noMessageBodyReaderTest() {
    try {
      provider.getMessageBodyReader(Point.class, Point.class, new Annotation[0],
          MediaType.APPLICATION_XHTML_XML_TYPE);
      Assert.fail();
    } catch (ProcessingException ex) {
      // ok
    }
  }

  @Test
  public void noContextResolverTest() {
    Assert
        .assertNull(provider.getContextResolver(String.class, MediaType.APPLICATION_ATOM_XML_TYPE));
  }

  @Test
  public void isWritableTest() {
    Assert.assertTrue(provider.isWriteable(Response.class, Response.class, null, null));
    Assert.assertFalse(provider.isWriteable(String.class, String.class, null, null));
  }

}
