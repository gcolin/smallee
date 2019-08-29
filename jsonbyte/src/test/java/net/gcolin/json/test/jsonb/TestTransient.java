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

import static org.junit.Assert.assertEquals;

import net.gcolin.json.test.jsonb.model.JsonbTransientValue;

import org.junit.Test;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.PropertyOrderStrategy;

/**
 * @author Gaël COLIN
 */
public class TestTransient {

  @Test
  public void testOrderingWithTransientField() {
    Jsonb jsonb = JsonbBuilder
        .create(new JsonbConfig().withPropertyOrderStrategy(PropertyOrderStrategy.LEXICOGRAPHICAL));

    final JsonbTransientValue pojo = new JsonbTransientValue();
    pojo.setProperty("propertyValue");
    pojo.setTransientProperty("hello");
    pojo.setTransientProperty2("hello");
    pojo.setTransientProperty3("hello");
    pojo.setTransientProperty4("hello");
    String result = jsonb.toJson(pojo);
    assertEquals(result, "{\"property\":\"propertyValue\"}");
  }

}
