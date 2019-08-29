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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import net.gcolin.json.JsonStringImpl;

import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 * JSON object tests.
 * 
 * @author Jitendra Kotamraju
 */
public class JsonObjectTest {

  @Test
  public void testToString() {
    JsonObject o1 =
        Json.createObjectBuilder().add("hello", "world").add("hello2", "world2").build();
    Assert.assertEquals("{\"hello\":\"world\",\"hello2\":\"world2\"}", o1.toString());

    JsonObject o2 = Json.createReader(new StringReader(o1.toString())).readObject();
    Assert.assertEquals("world", o2.getString("hello"));
  }

  @Test
  public void testToString2() {
    JsonObject o2 = Json
        .createReader(
            new StringReader("{\"code\":621456,\"code2\":426803,\"email\":\"gael8@gmail.com\"}"))
        .readObject();
    Assert.assertEquals("gael8@gmail.com", o2.getString("email"));
  }

  @Test
  public void testEmptyObjectEquals() throws Exception {
    JsonObject empty1 = Json.createObjectBuilder().build();

    JsonObject empty2 = Json.createObjectBuilder().build();

    assertEquals(empty1, empty2);
    testEmpty(empty1);
    testEmpty(empty2);
  }

  @Test
  public void testPersonObjectEquals() throws Exception {
    JsonObject person1 = JsonBuilderTest.buildPerson();
    JsonObject person2 = JsonReaderTest.readPerson();

    assertEquals(person1, person2);
    testPerson(person1);
    testPerson(person2);
  }

  static void testPerson(JsonObject person) {
    assertEquals(5, person.size());
    assertEquals("John", person.getString("firstName"));
    assertEquals("Smith", person.getString("lastName"));
    assertEquals(25, person.getJsonNumber("age").intValue());
    assertEquals(25, person.getInt("age"));

    JsonObject address = person.getJsonObject("address");
    assertTrue(address.containsKey("city"));
    assertFalse(address.containsKey("city2"));
    assertTrue(address.containsValue(new JsonStringImpl("NY")));
    assertFalse(address.containsValue(new JsonStringImpl("NY2")));
    assertEquals(4, address.size());
    assertEquals("21 2nd Street", address.getString("streetAddress"));
    assertEquals("New York", address.getString("city"));
    assertEquals("NY", address.getString("state"));
    assertEquals("10021", address.getString("postalCode"));

    JsonArray phoneNumber = person.getJsonArray("phoneNumber");
    assertEquals(2, phoneNumber.size());
    JsonObject home = phoneNumber.getJsonObject(0);
    assertEquals(2, home.size());
    assertEquals("home", home.getString("type"));
    assertEquals("212 555-1234", home.getString("number"));
    assertEquals("212 555-1234", home.getString("number"));

    JsonObject fax = phoneNumber.getJsonObject(1);
    assertEquals(2, fax.size());
    assertEquals("fax", fax.getString("type"));
    assertEquals("646 555-4567", fax.getString("number"));

    assertEquals("\"646 555-4567\"", fax.getJsonString("number").toString());
  }

  static void testEmpty(JsonObject empty) {
    assertTrue(empty.isEmpty());
  }

  @Test
  public void testClassCastException() {
    JsonObject obj = Json.createObjectBuilder().add("foo", JsonValue.FALSE).build();
    try {
      obj.getJsonNumber("foo");
      fail("Expected ClassCastException for casting JsonValue.FALSE to JsonNumber");
    } catch (ClassCastException ce) {
      // Expected
    }
  }

  @Test
  public void testObjectBuilderNpe() {
    try {
      Json.createObjectBuilder().add(null, 1).build();
      fail("JsonObjectBuilder#add(null, 1) should throw NullPointerException");
    } catch (NullPointerException ex) {
      // Expected
    }
  }

  @Test
  public void testDefaultValue() {
    JsonObject expected = Json.createObjectBuilder().add("0", JsonValue.TRUE)
        .add("1", JsonValue.FALSE).add("2", JsonValue.NULL).add("3", Integer.MAX_VALUE)
        .add("4", "hello").add("5", Double.MAX_VALUE).add("6", Json.createArrayBuilder().build())
        .add("7", Json.createObjectBuilder().build()).add("8", BigDecimal.TEN)
        .add("9", BigInteger.TEN).addNull("10").add("11", 11L).add("12", true)
        .add("13", false).build();

    Assert.assertTrue(expected.getBoolean("0", false));
    Assert.assertTrue(expected.getBoolean("0"));
    Assert.assertFalse(expected.getBoolean("100", false));
    Assert.assertTrue(expected.getBoolean("100", true));
    Assert.assertFalse(expected.getBoolean("1"));
    Assert.assertFalse(expected.getBoolean("1", true));
    Assert.assertTrue(expected.getBoolean("2", true));
    Assert.assertEquals(JsonValue.NULL, expected.get("2"));
    Assert.assertEquals(Integer.MAX_VALUE, expected.getInt("3"));
    Assert.assertEquals(Integer.MAX_VALUE, expected.getInt("3", 0));
    Assert.assertEquals(7, expected.getInt("100", 7));
    Assert.assertEquals("hello", expected.getString("4"));
    Assert.assertEquals("hello", expected.getString("4", "t"));
    Assert.assertEquals("t", expected.getString("100", "t"));
    Assert.assertEquals("hello", expected.getJsonString("4").getString());
    Assert.assertEquals(Double.MAX_VALUE, expected.getJsonNumber("5").doubleValue(), 1.0);
    Assert.assertEquals(Json.createArrayBuilder().build(), expected.getJsonArray("6"));
    Assert.assertEquals(Json.createObjectBuilder().build(), expected.getJsonObject("7"));

    Assert.assertEquals(10, expected.getJsonNumber("8").intValue());
    Assert.assertEquals(10, expected.getJsonNumber("9").intValue());

    Assert.assertEquals(11, expected.getJsonNumber("11").longValue());
    Assert.assertTrue(expected.getBoolean("12"));
    Assert.assertFalse(expected.getBoolean("13"));
  }

}
