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
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU General Public License
 * Version 2 only ("GPL") or the Common Development and Distribution License("CDDL") (collectively,
 * the "License"). You may not use this file except in compliance with the License. You can obtain a
 * copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html or
 * packager/legal/LICENSE.txt. See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each file and include the
 * License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception: Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License file that accompanied
 * this code.
 *
 * Modifications: If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s): If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include this software in
 * this distribution under the [CDDL or GPL Version 2] license." If you don't indicate a single
 * choice of license, a recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its licensees as
 * provided above. However, if you add GPL Version 2 code and therefore, elected the GPL Version 2
 * license, then the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package net.gcolin.json.test.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonWriter;

/**
 * JSON array tests.
 * 
 * @author Jitendra Kotamraju
 */
public class JsonArrayTest {

  @Test
  public void testArrayEquals() throws Exception {
    JsonArray expected = Json.createArrayBuilder().add(JsonValue.TRUE).add(JsonValue.FALSE)
        .add(JsonValue.NULL).add(Integer.MAX_VALUE).add(Long.MAX_VALUE).add(Double.MAX_VALUE)
        .add(Integer.MIN_VALUE).add(Long.MIN_VALUE).add(Double.MIN_VALUE).build();

    StringWriter sw = new StringWriter();
    JsonWriter writer = Json.createWriter(sw);
    writer.writeArray(expected);
    writer.close();

    JsonReader reader = Json.createReader(new StringReader(sw.toString()));
    JsonArray actual = reader.readArray();
    reader.close();

    assertEquals(expected, actual);
  }

  @Test
  public void testStringValue() throws Exception {
    JsonArray array = Json.createArrayBuilder().add("John").build();
    assertEquals("John", array.getString(0));
  }

  @Test
  public void testIntValue() throws Exception {
    JsonArray array = Json.createArrayBuilder().add(20).build();
    assertEquals(20, array.getInt(0));
  }

  @Test
  public void testNumberView() throws Exception {
    JsonArray array = Json.createArrayBuilder().add(20).add(10).build();

    List<JsonNumber> numberList = array.getValuesAs(JsonNumber.class);
    for (JsonNumber num : numberList) {
      num.intValue();
    }

    assertEquals(20, array.getInt(0));
    assertEquals(10, array.getInt(1));
  }

  @Test
  public void testArrayBuilderNpe() {
    try {
      Json.createArrayBuilder().add((JsonValue) null).build();
      fail("JsonArrayBuilder#add(null) should throw NullPointerException");
    } catch (NullPointerException ex) {
      // Expected
    }
  }

  @Test
  public void testArrayDefaultValue() {

    testArrayDefaultValue0(Json.createArrayBuilder().add(JsonValue.TRUE).add(JsonValue.FALSE)
        .add(JsonValue.NULL).add(Integer.MAX_VALUE).add("hello").add(Double.MAX_VALUE)
        .add(Json.createArrayBuilder().build()).add(Json.createObjectBuilder().build()).build());

    testArrayDefaultValue0(Json.createArrayBuilder().add(true).add(false).addNull()
        .add(Integer.MAX_VALUE).add("hello").add(Double.MAX_VALUE)
        .add(Json.createArrayBuilder().build()).add(Json.createObjectBuilder().build()).build());
  }

  private void testArrayDefaultValue0(JsonArray expected) {
    Assert.assertEquals(0, expected.getInt(0, 0));
    Assert.assertEquals("", expected.getString(0, ""));
    Assert.assertTrue(expected.getBoolean(0, false));
    Assert.assertTrue(expected.getBoolean(0));
    Assert.assertFalse(expected.getBoolean(10, false));
    Assert.assertTrue(expected.getBoolean(10, true));
    Assert.assertFalse(expected.getBoolean(1));
    Assert.assertFalse(expected.getBoolean(1, true));
    Assert.assertTrue(expected.getBoolean(2, true));
    Assert.assertEquals(JsonValue.NULL, expected.get(2));
    Assert.assertEquals(Integer.MAX_VALUE, expected.getInt(3));
    Assert.assertEquals(Integer.MAX_VALUE, expected.getInt(3, 0));
    Assert.assertEquals(7, expected.getInt(10, 7));
    Assert.assertEquals("hello", expected.getString(4));
    Assert.assertEquals("hello", expected.getString(4, "t"));
    Assert.assertEquals("t", expected.getString(10, "t"));
    Assert.assertEquals("hello", expected.getJsonString(4).getString());
    Assert.assertEquals(Double.MAX_VALUE, expected.getJsonNumber(5).doubleValue(), 1.0);
    Assert.assertEquals(Json.createArrayBuilder().build(), expected.getJsonArray(6));
    Assert.assertEquals(Json.createObjectBuilder().build(), expected.getJsonObject(7));
  }

}
