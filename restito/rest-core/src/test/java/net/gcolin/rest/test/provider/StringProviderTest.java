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

import net.gcolin.rest.provider.StringProvider;
import net.gcolin.rest.util.HeaderObjectMap;
import net.gcolin.rest.util.HttpHeader;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class StringProviderTest {

  @Test
  public void writeToTest() throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    new StringProvider().writeTo("hello", null, null, null, null, null, bout);
    Assert.assertEquals("hello world", new String(bout.toByteArray(), StandardCharsets.UTF_8));

    bout = new ByteArrayOutputStream();
    MultivaluedMap<String, Object> headers = HeaderObjectMap.createHeaders().getKey();
    new StringProvider().writeTo("123", null, null, null, null, headers, bout);
    Assert.assertEquals(MediaType.TEXT_PLAIN + ";charset=" + Charset.defaultCharset().name(),
        headers.getFirst(HttpHeader.CONTENT_TYPE).toString());
    Assert.assertEquals("123", new String(bout.toByteArray(), StandardCharsets.UTF_8));

    bout = new ByteArrayOutputStream();
    headers = HeaderObjectMap.createHeaders().getKey();
    new StringProvider().writeTo("123", null, null, null, MediaType.WILDCARD_TYPE, headers, bout);
    Assert.assertEquals(MediaType.TEXT_PLAIN + ";charset=" + Charset.defaultCharset().name(),
        headers.getFirst(HttpHeader.CONTENT_TYPE).toString());
    Assert.assertEquals("123", new String(bout.toByteArray(), StandardCharsets.UTF_8));

    bout = new ByteArrayOutputStream();
    headers = HeaderObjectMap.createHeaders().getKey();
    headers.add(HttpHeader.CONTENT_TYPE, MediaType.TEXT_PLAIN);
    new StringProvider().writeTo("123", null, null, null, null, headers, bout);
    Assert.assertEquals(MediaType.TEXT_PLAIN, headers.getFirst(HttpHeader.CONTENT_TYPE).toString());
    Assert.assertEquals("123", new String(bout.toByteArray(), StandardCharsets.UTF_8));

    bout = new ByteArrayOutputStream();
    headers = HeaderObjectMap.createHeaders().getKey();
    new StringProvider().writeTo("123", null, null, null, MediaType.TEXT_XML_TYPE, headers, bout);
    Assert.assertNull(headers.getFirst(HttpHeader.CONTENT_TYPE));
    Assert.assertEquals("123", new String(bout.toByteArray(), StandardCharsets.UTF_8));

  }

}
