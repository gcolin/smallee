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
import net.gcolin.rest.ext.json.JsonStructureProvider;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;

/**
 * Test JsonStructureProvider.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JsonStructureProviderTest {

  @Test
  public void isWriteableTest() {
    Assert.assertFalse(new JsonStructureProvider().isWriteable(Pair.class, Pair.class,
        new Annotation[0], MediaType.APPLICATION_JSON_TYPE));
    Assert.assertFalse(new JsonStructureProvider().isWriteable(JsonStructure.class,
        JsonStructure.class, new Annotation[0], MediaType.TEXT_HTML_TYPE));
    Assert.assertTrue(new JsonStructureProvider().isWriteable(JsonStructure.class,
        JsonStructure.class, new Annotation[0], MediaType.APPLICATION_JSON_TYPE));
  }

  @Test
  public void isReadableTest() {
    Assert.assertFalse(new JsonStructureProvider().isReadable(Pair.class, Pair.class,
        new Annotation[0], MediaType.APPLICATION_JSON_TYPE));
    Assert.assertFalse(new JsonStructureProvider().isReadable(JsonStructure.class,
        JsonStructure.class, new Annotation[0], MediaType.TEXT_HTML_TYPE));
    Assert.assertTrue(new JsonStructureProvider().isReadable(JsonStructure.class,
        JsonStructure.class, new Annotation[0], MediaType.APPLICATION_JSON_TYPE));
  }

  @Test
  public void sizeTest() {
    Assert.assertEquals(-1, new JsonStructureProvider().getSize(null, null, null, null, null));
  }

  @Test
  public void writeToTest() throws IOException {
    JsonObject object = Json.createObjectBuilder().add("value", 1).build();
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    new JsonStructureProvider().writeTo(object, JsonStructure.class, JsonStructure.class,
        new Annotation[0], MediaType.APPLICATION_JSON_TYPE,
        new MultivaluedHashMap<String, Object>(), bout);
    Assert.assertEquals("{\"value\":1}", new String(bout.toByteArray(), StandardCharsets.UTF_8));
  }

  @Test
  public void readFromTest() throws IOException {
    JsonObject object = (JsonObject) new JsonStructureProvider().readFrom(JsonStructure.class,
        JsonStructure.class, new Annotation[0], MediaType.APPLICATION_JSON_TYPE,
        new MultivaluedHashMap<String, String>(),
        new ByteArrayInputStream("{\"value\":1}".getBytes(StandardCharsets.UTF_8)));
    Assert.assertEquals(1, object.size());
    Assert.assertEquals(1, object.getInt("value"));
  }

}
