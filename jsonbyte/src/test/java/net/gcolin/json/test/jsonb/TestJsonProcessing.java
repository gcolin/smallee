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

package net.gcolin.json.test.jsonb;

import org.junit.Assert;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;

/**
 * @author Gaël COLIN
 */
public class TestJsonProcessing extends AbstractMultiCharsetTest {

  public static class Obj {
    JsonObject object;
    JsonArray array;
    JsonStructure structure;
    JsonValue value;
    JsonString string;
    JsonNumber number;
  }

  @Test
  public void testJsonObject() {
    Obj o = new Obj();
    o.object = Json.createObjectBuilder().add("value", "hello").build();
    Obj o2 = test0(Obj.class, o, "{\"object\":{\"value\":\"hello\"}}");
    Assert.assertEquals("hello", o2.object.getString("value"));
  }

  @Test
  public void testJsonArray() {
    Obj o = new Obj();
    o.array = Json.createArrayBuilder().add("hello").build();
    Obj o2 = test0(Obj.class, o, "{\"array\":[\"hello\"]}");
    Assert.assertEquals("hello", o2.array.getString(0));
  }

  @Test
  public void testJsonStructure() {
    Obj o = new Obj();
    o.structure = Json.createObjectBuilder().add("value", "hello").build();
    Obj o2 = test0(Obj.class, o, "{\"structure\":{\"value\":\"hello\"}}");
    Assert.assertEquals("hello", ((JsonObject) o2.structure).getString("value"));
  }

  @Test
  public void testJsonValue() {
    Obj o = new Obj();
    o.value = JsonValue.NULL;
    Obj o2 = test0(Obj.class, o, "{\"value\":null}");
    Assert.assertEquals(JsonValue.NULL, o2.value);
  }

  @Test
  public void testJsonString() {
    Obj o = new Obj();
    o.string = (JsonString) Json.createArrayBuilder().add("hello").build().get(0);
    Obj o2 = test0(Obj.class, o, "{\"string\":\"hello\"}");
    Assert.assertEquals("hello", o2.string.getString());
  }

  @Test
  public void testJsonNumber() {
    Obj o = new Obj();
    o.number = (JsonNumber) Json.createArrayBuilder().add(123).build().get(0);
    Obj o2 = test0(Obj.class, o, "{\"number\":123}");
    Assert.assertEquals(123, o2.number.intValue());
  }

}
