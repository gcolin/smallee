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

package net.gcolin.rest.ext.json.test;

import net.gcolin.common.io.ByteArrayInputStream;
import net.gcolin.common.lang.Pair;
import net.gcolin.rest.ext.json.JsonArrayProvider;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;

import javax.json.Json;
import javax.json.JsonArray;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;

/**
 * Test JsonArrayProvider.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JsonArrayProviderTest {

  @Test
  public void isWriteableTest() {
    Assert.assertFalse(new JsonArrayProvider().isWriteable(Pair.class, Pair.class,
        new Annotation[0], MediaType.APPLICATION_JSON_TYPE));
    Assert.assertFalse(new JsonArrayProvider().isWriteable(JsonArray.class, JsonArray.class,
        new Annotation[0], MediaType.TEXT_HTML_TYPE));
    Assert.assertTrue(new JsonArrayProvider().isWriteable(JsonArray.class, JsonArray.class,
        new Annotation[0], MediaType.APPLICATION_JSON_TYPE));
  }

  @Test
  public void isReadableTest() {
    Assert.assertFalse(new JsonArrayProvider().isReadable(Pair.class, Pair.class, new Annotation[0],
        MediaType.APPLICATION_JSON_TYPE));
    Assert.assertFalse(new JsonArrayProvider().isReadable(JsonArray.class, JsonArray.class,
        new Annotation[0], MediaType.TEXT_HTML_TYPE));
    Assert.assertTrue(new JsonArrayProvider().isReadable(JsonArray.class, JsonArray.class,
        new Annotation[0], MediaType.APPLICATION_JSON_TYPE));
  }

  @Test
  public void sizeTest() {
    Assert.assertEquals(-1, new JsonArrayProvider().getSize(null, null, null, null, null));
  }

  @Test
  public void writeToTest() throws IOException {
    JsonArray array = Json.createArrayBuilder().add(1).build();
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    new JsonArrayProvider().writeTo(array, JsonArray.class, JsonArray.class, new Annotation[0],
        MediaType.APPLICATION_JSON_TYPE, new MultivaluedHashMap<String, Object>(), bout);
    Assert.assertEquals("[1]", new String(bout.toByteArray(), StandardCharsets.UTF_8));
  }

  @Test
  public void readFromTest() throws IOException {
    JsonArray array =
        new JsonArrayProvider().readFrom(JsonArray.class, JsonArray.class, new Annotation[0],
            MediaType.APPLICATION_JSON_TYPE, new MultivaluedHashMap<String, String>(),
            new ByteArrayInputStream("[1]".getBytes(StandardCharsets.UTF_8)));
    Assert.assertEquals(1, array.size());
    Assert.assertEquals(1, array.getInt(0));
  }

}
