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

import net.gcolin.common.io.Io;
import net.gcolin.rest.provider.ReaderProvider;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ReaderProviderTest {

  @Test
  public void writeToTest() throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    new ReaderProvider().writeTo(new StringReader("hello"), Reader.class, Reader.class, null, null,
        new MultivaluedHashMap<>(), bout);
    Assert.assertEquals("hello", new String(bout.toByteArray(), StandardCharsets.UTF_8));

    MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
    headers.add(HttpHeaders.CONTENT_TYPE,
        MediaType.TEXT_HTML + ";charset=" + StandardCharsets.UTF_8.name());
    bout = new ByteArrayOutputStream();
    new ReaderProvider().writeTo(
        new InputStreamReader(new ByteArrayInputStream("hello".getBytes("Big5")), "Big5"),
        Reader.class, Reader.class, null, null, headers, bout);
    Assert.assertEquals("hello", new String(bout.toByteArray(), "Big5"));
  }

  @Test
  public void readFromTest() throws IOException {
    MultivaluedHashMap<String, String> headers = new MultivaluedHashMap<>();
    headers.add(HttpHeaders.CONTENT_TYPE,
        MediaType.TEXT_HTML + ";charset=" + StandardCharsets.UTF_8.name());
    Reader in = new ReaderProvider().readFrom(Reader.class, Reader.class, null, null, headers,
        new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)));
    Assert.assertEquals("hello", Io.toString(in));

    headers = new MultivaluedHashMap<>();
    headers.add(HttpHeaders.CONTENT_TYPE,
        MediaType.TEXT_HTML + ";charset=" + StandardCharsets.UTF_8.name() + ";a=1");
    in = new ReaderProvider().readFrom(Reader.class, Reader.class, null, null, headers,
        new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)));
    Assert.assertEquals("hello", Io.toString(in));

    headers = new MultivaluedHashMap<>();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML);
    in = new ReaderProvider().readFrom(Reader.class, Reader.class, null, null, headers,
        new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)));
    Assert.assertEquals("hello", Io.toString(in));
  }

}
