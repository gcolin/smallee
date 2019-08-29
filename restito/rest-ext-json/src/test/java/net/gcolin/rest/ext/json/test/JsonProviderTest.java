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
import net.gcolin.rest.ext.json.JsonProvider;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;

/**
 * Test JsonProvider.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JsonProviderTest {

  @Test
  public void isWriteableTest() {
    Assert.assertTrue(new JsonProvider().isWriteable(Pair.class, Pair.class, new Annotation[0],
        MediaType.APPLICATION_JSON_TYPE));
    Assert.assertFalse(new JsonProvider().isWriteable(Pair.class, Pair.class, new Annotation[0],
        MediaType.TEXT_HTML_TYPE));
  }

  @Test
  public void isReadableTest() {
    Assert.assertTrue(new JsonProvider().isReadable(Pair.class, Pair.class, new Annotation[0],
        MediaType.APPLICATION_JSON_TYPE));
    Assert.assertFalse(new JsonProvider().isReadable(Pair.class, Pair.class, new Annotation[0],
        MediaType.TEXT_HTML_TYPE));
  }

  @Test
  public void sizeTest() {
    Assert.assertEquals(-1, new JsonProvider().getSize(null, null, null, null, null));
  }

  @Test
  public void writeToTest() throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    new JsonProvider().writeTo(new Pair<>(1, 2), Entry.class, Entry.class, new Annotation[0],
        MediaType.APPLICATION_JSON_TYPE, new MultivaluedHashMap<String, Object>(), bout);
    Assert.assertTrue(new String(bout.toByteArray(), StandardCharsets.UTF_8)
        .startsWith("{\"key\":1,\"value\":2,"));

    bout = new ByteArrayOutputStream();
    new JsonProvider().writeTo(null, Entry.class, Entry.class, new Annotation[0],
        MediaType.APPLICATION_JSON_TYPE, new MultivaluedHashMap<String, Object>(), bout);
    Assert.assertEquals("null", new String(bout.toByteArray(), StandardCharsets.UTF_8));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void readFromTest() throws IOException {
    A pair = (A) new JsonProvider().readFrom((Class) A.class, A.class, new Annotation[0],
        MediaType.APPLICATION_JSON_TYPE, new MultivaluedHashMap<String, String>(),
        new ByteArrayInputStream("{\"value\":1}".getBytes(StandardCharsets.UTF_8)));
    Assert.assertEquals(1, pair.getValue());

    try {
      new JsonProvider().readFrom((Class) A.class, A.class, new Annotation[0],
          MediaType.APPLICATION_JSON_TYPE, new MultivaluedHashMap<String, String>(),
          new ByteArrayInputStream("\"value\":1}".getBytes(StandardCharsets.UTF_8)));
      Assert.fail();
    } catch (Exception ex) {
      // ok
    }

  }

  public static class A {
    private int value;

    public int getValue() {
      return value;
    }

    public void setValue(int value) {
      this.value = value;
    }


  }

}
