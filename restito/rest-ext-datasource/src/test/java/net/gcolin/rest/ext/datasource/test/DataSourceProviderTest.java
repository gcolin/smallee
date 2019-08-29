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

package net.gcolin.rest.ext.datasource.test;

import net.gcolin.common.io.Io;
import net.gcolin.rest.ext.datasource.DataSourceProvider;
import net.gcolin.rest.ext.datasource.SimpleDataSource;
import net.gcolin.rest.util.HeaderObjectMap;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.activation.DataSource;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class DataSourceProviderTest {

  @Test
  public void getSizeTest() {
    Assert.assertEquals(-1, new DataSourceProvider().getSize(null, DataSource.class,
        DataSource.class, null, MediaType.TEXT_PLAIN_TYPE));
  }

  @Test
  public void isWriteableTest() {
    Assert.assertTrue(new DataSourceProvider().isWriteable(DataSource.class, DataSource.class, null,
        MediaType.TEXT_PLAIN_TYPE));
    Assert.assertFalse(new DataSourceProvider().isWriteable(String.class, String.class, null,
        MediaType.TEXT_PLAIN_TYPE));
  }

  @Test
  public void writeToTest() throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    MultivaluedMap<String, Object> httpHeaders = HeaderObjectMap.createHeaders().getKey();
    new DataSourceProvider().writeTo(
        new SimpleDataSource("text/xml", "hello",
            new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8))),
        DataSource.class, DataSource.class, null, null, httpHeaders, bout);
    Assert.assertEquals("hello", httpHeaders.getFirst("dataSourceName"));
    Assert.assertEquals(MediaType.TEXT_XML_TYPE, httpHeaders.getFirst(HttpHeaders.CONTENT_TYPE));
    Assert.assertEquals("content", new String(bout.toByteArray(), StandardCharsets.UTF_8));
  }

  @Test
  public void readFromTest() throws IOException {
    MultivaluedMap<String, String> httpHeaders = new MultivaluedHashMap<>();
    httpHeaders.add(HttpHeaders.CONTENT_TYPE, "XML");
    httpHeaders.add("dataSourceName", "hello");

    ByteArrayInputStream bout =
        new ByteArrayInputStream("content".getBytes(StandardCharsets.UTF_8));
    DataSource ds = new DataSourceProvider().readFrom(DataSource.class, DataSource.class, null,
        null, httpHeaders, bout);
    Assert.assertEquals("hello", ds.getName());
    Assert.assertEquals("XML", ds.getContentType());
    Assert.assertEquals("content", Io.toString(ds.getInputStream()));

    try {
      ds.getOutputStream();
      Assert.fail();
    } catch (IOException ex) {
      // ok
    }
  }

}
