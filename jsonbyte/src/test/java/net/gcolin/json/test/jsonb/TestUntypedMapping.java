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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

/**
 * @author Gaël COLIN
 */
public class TestUntypedMapping {

  public static class Sub {
    String value;
  }

  public static class Obj {

    boolean bool;
    Boolean bool2;
    String string;
    int number;
    long[] array;
    Sub sub;

  }

  @SuppressWarnings("unchecked")
  @Test
  public void test() {
    Sub sub = new Sub();
    sub.value = "hello";
    Obj obj = new Obj();
    obj.sub = sub;
    obj.array = new long[1];
    obj.array[0] = 10L;
    obj.bool = true;
    obj.bool2 = false;
    obj.number = 10;
    obj.string = "world";

    Jsonb jsonb = JsonbBuilder.create();
    String result = jsonb.toJson(obj);
    Assert.assertEquals(
        "{\"bool\":true,\"bool2\":false,\"string\":\"world\",\"number\":10,\"array\":[10],\"sub\":{\"value\":\"hello\"}}",
        result);
    Map<String, Object> map = (Map<String, Object>) jsonb.fromJson(result, Object.class);
    Assert.assertTrue((Boolean) map.get("bool"));
    Assert.assertFalse((Boolean) map.get("bool2"));
    Assert.assertEquals("hello", ((Map<String, Object>) map.get("sub")).get("value"));
    Assert.assertEquals(10, ((BigDecimal) map.get("number")).intValue());
    Assert.assertEquals(Arrays.asList(BigDecimal.TEN), map.get("array"));
  }

}
