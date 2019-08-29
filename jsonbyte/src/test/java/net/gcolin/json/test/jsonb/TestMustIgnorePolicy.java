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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

/**
 * TestMustIgnorePolicy test.
 * 
 * @author Gaël COLIN
 */
public class TestMustIgnorePolicy {

  public static class Obj {
    String value;
  }

  @Test
  public void test() {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("hello", "world");
    map.put("array", Arrays.asList(1, 2, 3));
    map.put("obj", new HashMap<>());
    map.put("value", "hello");
    map.put("number", 123);
    map.put("bool", true);

    Jsonb jsonb = JsonbBuilder.create();
    String result = jsonb.toJson(map);
    Obj obj = jsonb.fromJson(result, Obj.class);
    Assert.assertEquals("hello", obj.value);
  }

}
