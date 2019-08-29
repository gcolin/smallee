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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonBuilderFactory;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerationException;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

/**
 * {@link JsonGenerator} tests.
 *
 * @author Jitendra Kotamraju
 * @author Gaël COLIN
 */
public class JsonGeneratorTest {

  @Test
  public void testObjectWriter() throws Exception {
    StringWriter writer = new StringWriter();
    JsonGenerator generator = Json.createGenerator(writer);
    testObject(generator);
    generator.close();
    writer.close();

    JsonReader reader = Json.createReader(new StringReader(writer.toString()));
    JsonObject person = reader.readObject();
    JsonObjectTest.testPerson(person);
  }

  @Test
  public void testObjectStream() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    JsonGenerator generator = Json.createGenerator(out);
    testObject(generator);
    generator.close();
    out.close();

    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    JsonReader reader = Json.createReader(in);
    JsonObject person = reader.readObject();
    JsonObjectTest.testPerson(person);
    reader.close();
    in.close();
  }

  static void testObject(JsonGenerator generator) throws Exception {
    generator.writeStartObject().write("firstName", "John").write("lastName", "Smith")
        .write("age", 25).writeStartObject("address").write("streetAddress", "21 2nd Street")
        .write("city", "New York").write("state", "NY").write("postalCode", "10021").writeEnd()
        .writeStartArray("phoneNumber").writeStartObject().write("type", "home")
        .write("number", "212 555-1234").writeEnd().writeStartObject().write("type", "fax")
        .write("number", "646 555-4567").writeEnd().writeEnd().writeEnd();
  }

  @Test
  public void testArray() throws Exception {
    Writer sw = new StringWriter();
    JsonGenerator generator = Json.createGenerator(sw);
    generator.writeStartArray().writeStartObject().write("type", "home")
        .write("number", "212 555-1234").writeEnd().writeStartObject().write("type", "fax")
        .write("number", "646 555-4567").writeEnd().writeEnd();
    generator.close();
  }

  @Test
  // tests JsonGenerator when JsonValue is used for generation
  public void testJsonValue() throws Exception {
    StringWriter writer = new StringWriter();
    JsonGenerator generator = Json.createGenerator(writer);
    generator.writeStartObject().write("firstName", "John").write("lastName", "Smith")
        .write("age", 25).write("address", JsonBuilderTest.buildAddress())
        .write("phoneNumber", JsonBuilderTest.buildPhone()).writeEnd();
    generator.close();
    writer.close();

    JsonReader reader = Json.createReader(new StringReader(writer.toString()));
    JsonObject person = reader.readObject();
    JsonObjectTest.testPerson(person);
  }

  @Test
  public void testArrayString() throws Exception {
    StringWriter writer = new StringWriter();
    JsonGenerator generator = Json.createGenerator(writer);
    generator.writeStartArray().write("string").writeEnd();
    generator.close();
    writer.close();

    assertEquals("[\"string\"]", writer.toString());
  }

  @Test
  public void testArrayString2() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    JsonGenerator generator =
        Json.createGeneratorFactory(null).createGenerator(out, StandardCharsets.UTF_16);
    generator.writeStartArray().write("string").writeEnd();
    generator.close();
    out.close();

    Assert.assertArrayEquals("[\"string\"]".getBytes(StandardCharsets.UTF_16), out.toByteArray());
  }

  @Test
  public void testEscapedString() throws Exception {
    StringWriter writer = new StringWriter();
    JsonGenerator generator = Json.createGenerator(writer);
    generator.writeStartArray().write("\u0000").writeEnd();
    generator.close();
    writer.close();

    assertEquals("[\"\\u0000\"]", writer.toString());
  }

  @Test
  public void testEscapedString1() throws Exception {
    StringWriter sw = new StringWriter();
    JsonGenerator generator = Json.createGenerator(sw);
    generator.writeStartArray().write("\u0000\u00ff").writeEnd();
    generator.close();
    sw.close();

    JsonReader jr = Json.createReader(new StringReader(sw.toString()));

    JsonArray array = jr.readArray();
    String got = array.getString(0);
    jr.close();

    assertEquals("\u0000\u00ff", got);
  }

  @Test
  public void testGeneratorEquals() throws Exception {
    StringWriter sw = new StringWriter();
    JsonGenerator generator = Json.createGenerator(sw);
    generator.writeStartArray().write(JsonValue.TRUE).write(JsonValue.FALSE).write(JsonValue.NULL)
        .write(Integer.MAX_VALUE).write(Long.MAX_VALUE).write(Double.MAX_VALUE)
        .write(Integer.MIN_VALUE).write(Long.MIN_VALUE).write(Double.MIN_VALUE).writeEnd();
    generator.close();

    JsonReader reader = Json.createReader(new StringReader(sw.toString()));
    JsonArray expected = reader.readArray();
    reader.close();

    JsonArray actual = Json.createArrayBuilder().add(JsonValue.TRUE).add(JsonValue.FALSE)
        .add(JsonValue.NULL).add(Integer.MAX_VALUE).add(Long.MAX_VALUE).add(Double.MAX_VALUE)
        .add(Integer.MIN_VALUE).add(Long.MIN_VALUE).add(Double.MIN_VALUE).build();

    assertEquals(expected, actual);
  }

  @Test
  public void testPrettyObjectWriter() throws Exception {
    StringWriter writer = new StringWriter();
    Map<String, Object> config = new HashMap<String, Object>();
    config.put(JsonGenerator.PRETTY_PRINTING, true);
    JsonGenerator generator = Json.createGeneratorFactory(config).createGenerator(writer);
    testObject(generator);
    generator.close();
    writer.close();

    JsonReader reader = Json.createReader(new StringReader(writer.toString()));
    JsonObject person = reader.readObject();
    JsonObjectTest.testPerson(person);
  }

  @Test
  public void testPrettyObjectStream() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Map<String, Object> config = new HashMap<String, Object>();
    config.put(JsonGenerator.PRETTY_PRINTING, true);
    JsonGenerator generator = Json.createGeneratorFactory(config).createGenerator(out);
    testObject(generator);
    generator.close();
    out.close();

    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    JsonReader reader = Json.createReader(in);
    JsonObject person = reader.readObject();
    JsonObjectTest.testPerson(person);
    reader.close();
    in.close();
  }

  @Test
  public void testGenerationException1() throws Exception {
    StringWriter writer = new StringWriter();
    JsonGenerator generator = Json.createGenerator(writer);
    generator.writeStartObject();
    try {
      generator.writeStartObject();
      fail("Expected JsonGenerationException, writeStartObject() cannot be called more than once");
    } catch (JsonGenerationException je) {
      // Expected exception
    }
  }

  @Test
  public void testGenerationException2() throws Exception {
    StringWriter writer = new StringWriter();
    JsonGenerator generator = Json.createGenerator(writer);
    generator.writeStartObject();
    try {
      generator.writeStartArray();
      fail("Expected JsonGenerationException, writeStartArray() is valid in no context");
    } catch (JsonGenerationException je) {
      // Expected exception
    }
  }

  @Test
  public void testGenerationException3() throws Exception {
    StringWriter writer = new StringWriter();
    JsonGenerator generator = Json.createGenerator(writer);
    try {
      generator.close();
      fail("Expected JsonGenerationException, no JSON is generated");
    } catch (JsonGenerationException je) {
      // Expected exception
    }
  }

  @Test
  public void testGenerationException4() throws Exception {
    StringWriter writer = new StringWriter();
    JsonGenerator generator = Json.createGenerator(writer);
    generator.writeStartArray();
    try {
      generator.close();
      fail("Expected JsonGenerationException, writeEnd() is not called");
    } catch (JsonGenerationException je) {
      // Expected exception
    }
  }

  @Test
  public void testGenerationException5() throws Exception {
    StringWriter writer = new StringWriter();
    JsonGenerator generator = Json.createGenerator(writer);
    generator.writeStartObject();
    try {
      generator.close();
      fail("Expected JsonGenerationException, writeEnd() is not called");
    } catch (JsonGenerationException je) {
      // Expected exception
    }
  }

  @Test
  public void testGenerationException6() throws Exception {
    StringWriter writer = new StringWriter();
    JsonGenerator generator = Json.createGenerator(writer);
    generator.writeStartObject().writeEnd();
    try {
      generator.writeStartObject();
      fail("Expected JsonGenerationException, cannot generate one more JSON text");
    } catch (JsonGenerationException je) {
      // Expected exception
    }
  }

  @Test
  public void testGenerationException7() throws Exception {
    StringWriter writer = new StringWriter();
    JsonGenerator generator = Json.createGenerator(writer);
    generator.writeStartArray().writeEnd();
    try {
      generator.writeStartArray();
      fail("Expected JsonGenerationException, cannot generate one more JSON text");
    } catch (JsonGenerationException je) {
      // Expected exception
    }
  }

  @Test
  public void testGeneratorArrayDouble() throws Exception {
    StringWriter writer = new StringWriter();
    JsonGenerator generator = Json.createGenerator(writer);
    generator.writeStartArray();
    try {
      generator.write(Double.NaN);
      fail("JsonGenerator.write(Double.NaN) should produce NumberFormatException");
    } catch (NumberFormatException ne) {
      // expected
    }
    try {
      generator.write(Double.POSITIVE_INFINITY);
      fail("JsonGenerator.write(Double.POSITIVE_INIFINITY) should produce NumberFormatException");
    } catch (NumberFormatException ne) {
      // expected
    }
    try {
      generator.write(Double.NEGATIVE_INFINITY);
      fail("JsonGenerator.write(Double.NEGATIVE_INIFINITY) should produce NumberFormatException");
    } catch (NumberFormatException ne) {
      // expected
    }
    generator.writeEnd();
    generator.close();
  }

  @Test
  public void testGeneratorObjectDouble() throws Exception {
    StringWriter writer = new StringWriter();
    JsonGenerator generator = Json.createGenerator(writer);
    generator.writeStartObject();
    try {
      generator.write("foo", Double.NaN);
      fail("JsonGenerator.write(String, Double.NaN) should produce NumberFormatException");
    } catch (NumberFormatException ne) {
      // expected
    }
    try {
      generator.write("foo", Double.POSITIVE_INFINITY);
      fail("JsonGenerator.write(String, Double.POSITIVE_INIFINITY) "
          + "should produce NumberFormatException");
    } catch (NumberFormatException ne) {
      // expected
    }
    try {
      generator.write("foo", Double.NEGATIVE_INFINITY);
      fail("JsonGenerator.write(String, Double.NEGATIVE_INIFINITY) should "
          + "produce NumberFormatException");
    } catch (NumberFormatException ne) {
      // expected
    }
    generator.writeEnd();
    generator.close();
  }

  @Test
  public void testIntGenerator() throws Exception {
    Random rand = new Random(System.currentTimeMillis());
    JsonGeneratorFactory gf = Json.createGeneratorFactory(null);
    JsonReaderFactory rf = Json.createReaderFactory(null);
    JsonBuilderFactory bf = Json.createBuilderFactory(null);
    for (int i = 0; i < 100000; i++) {
      int num = rand.nextInt();
      StringWriter sw = new StringWriter();
      JsonGenerator generator = gf.createGenerator(sw);
      generator.writeStartArray().write(num).writeEnd().close();

      JsonReader reader = rf.createReader(new StringReader(sw.toString()));
      JsonArray got = reader.readArray();
      reader.close();

      JsonArray expected = bf.createArrayBuilder().add(num).build();

      assertEquals(expected, got);
    }
  }

  @Test
  public void testGeneratorBuf() throws Exception {
    JsonGeneratorFactory gf = Json.createGeneratorFactory(null);
    JsonReaderFactory rf = Json.createReaderFactory(null);
    JsonBuilderFactory bf = Json.createBuilderFactory(null);
    StringBuilder sb = new StringBuilder();
    int value = 10;
    for (int i = 0; i < 250; i++) {
      sb.append('a');
      String name = sb.toString();
      StringWriter sw = new StringWriter();
      JsonGenerator generator = gf.createGenerator(sw);
      generator.writeStartObject().write(name, value).writeEnd().close();

      JsonReader reader = rf.createReader(new StringReader(sw.toString()));
      JsonObject got = reader.readObject();
      reader.close();

      JsonObject expected = bf.createObjectBuilder().add(name, value).build();

      assertEquals(expected, got);
    }
  }

  @Test
  public void testBufferPoolFeature() {

    JsonGeneratorFactory factory = Json.createGeneratorFactory(null);
    JsonGenerator generator = factory.createGenerator(new StringWriter());
    generator.writeStartArray();
    generator.writeEnd();
    generator.close();
  }

  @Test
  public void testBufferSizes() {
    JsonReaderFactory rf = Json.createReaderFactory(null);
    JsonBuilderFactory bf = Json.createBuilderFactory(null);
    for (int size = 10; size < 10; size++) {
      JsonGeneratorFactory gf = Json.createGeneratorFactory(null);

      StringBuilder sb = new StringBuilder();
      int value = 10;
      for (int i = 0; i < 15; i++) {
        sb.append('a');
        String name = sb.toString();
        StringWriter sw = new StringWriter();
        JsonGenerator generator = gf.createGenerator(sw);
        generator.writeStartObject().write(name, value).writeEnd().close();

        JsonReader reader = rf.createReader(new StringReader(sw.toString()));
        JsonObject got = reader.readObject();
        reader.close();

        JsonObject expected = bf.createObjectBuilder().add(name, value).build();

        assertEquals(expected, got);
      }

    }
  }

  @Test
  public void testString() throws Exception {
    escapedString("");
    escapedString("abc");
    escapedString("abc\f");
    escapedString("abc\na");
    escapedString("abc\tabc");
    escapedString("abc\n\tabc");
    escapedString("abc\n\tabc\r");
    escapedString("\n\tabc\r");
    escapedString("\bab\tb\rc\\\"\ftesting1234");
    escapedString("\f\babcdef\tb\rc\\\"\ftesting1234");
  }

  void escapedString(String expected) throws Exception {
    StringWriter sw = new StringWriter();
    JsonGenerator generator = Json.createGenerator(sw);
    generator.writeStartArray().write(expected).writeEnd();
    generator.close();
    sw.close();

    JsonReader jr = Json.createReader(new StringReader(sw.toString()));
    JsonArray array = jr.readArray();
    String got = array.getString(0);
    jr.close();

    assertEquals(expected, got);
  }

  @Test
  public void testFlush() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JsonGenerator gen = Json.createGenerator(baos);
    gen.writeStartObject().writeEnd();
    gen.flush();

    assertEquals("{}", baos.toString("UTF-8"));
  }

  @Test
  public void testBigArray() throws Exception {
    StringBuilder str = new StringBuilder();
    for (int i = 0; i < 1024; i++) {
      str.append("hello world ");
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JsonGenerator gen = Json.createGenerator(baos);
    gen.writeStartObject();
    gen.write("hello", str.toString());
    gen.writeEnd().close();

    JsonObject oo = Json.createReader(new ByteArrayInputStream(baos.toByteArray())).readObject();
    Assert.assertEquals(1, oo.size());
    Assert.assertEquals(str.toString(), oo.getString("hello"));

    baos = new ByteArrayOutputStream();
    gen = Json.createGenerator(baos);
    gen.writeStartArray();
    for (int i = 0; i < 1024; i++) {
      gen.write(true);
    }
    gen.writeEnd().close();
  }

  @Test
  public void testClose() throws Exception {
    try {
      JsonGenerator gen = Json.createGenerator((Writer) null);
      gen.close();
      Assert.fail();
    } catch (JsonGenerationException ex) {
      // ok
    }
  }

  @Test
  public void testGenerator() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JsonGenerator gen = Json.createGenerator(baos);
    gen.writeStartArray();
    gen.write(new BigDecimal(12)).write(BigInteger.TEN).writeNull().write(true).write(false);
    gen.writeStartObject().write("0", new BigDecimal(12)).write("1", BigInteger.TEN)
        .write("2", 13L).write("3", 14.4).write("4", false).write("6", true).writeNull("5")
        .writeEnd();
    gen.writeEnd().close();
    JsonArray oo = Json.createReader(new ByteArrayInputStream(baos.toByteArray())).readArray();
    Assert.assertEquals(6, oo.size());
    Assert.assertEquals(new BigDecimal(12), oo.getJsonNumber(0).bigDecimalValue());
    Assert.assertEquals(BigInteger.TEN, oo.getJsonNumber(1).bigIntegerValue());
    Assert.assertEquals(JsonValue.ValueType.NULL, oo.get(2).getValueType());
    Assert.assertTrue(oo.isNull(2));
    Assert.assertFalse(oo.isNull(3));
    Assert.assertTrue(oo.getBoolean(3));
    Assert.assertFalse(oo.getBoolean(4));
    try {
      oo.getBoolean(5);
    } catch (ClassCastException ex) {
      // ok
    }
    JsonObject obj = oo.getJsonObject(5);
    Assert.assertEquals(7, obj.size());
    Assert.assertEquals(new BigDecimal(12), obj.getJsonNumber("0").bigDecimalValue());
    Assert.assertEquals(BigInteger.TEN, obj.getJsonNumber("1").bigIntegerValue());
    Assert.assertEquals(13L, obj.getJsonNumber("2").longValue());
    Assert.assertEquals(14.4, obj.getJsonNumber("3").doubleValue(), 1);
    Assert.assertFalse(obj.getBoolean("4"));
    Assert.assertTrue(obj.getBoolean("6"));
    try {
      obj.getBoolean("5");
    } catch (ClassCastException ex) {
      // ok
    }
    Assert.assertEquals(JsonValue.NULL, obj.get("5"));
    Assert.assertTrue(obj.isNull("5"));
    Assert.assertFalse(obj.isNull("4"));
  }

  @Test
  public void testBadGenerator() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JsonGenerator gen = Json.createGenerator(baos);
    gen.writeStartObject().writeEnd();
    try {
      gen.writeStartArray();
      Assert.fail();
    } catch (JsonGenerationException ex) {
      // ok
    }

    baos = new ByteArrayOutputStream();
    gen = Json.createGenerator(baos);
    gen.writeStartObject().writeEnd();
    try {
      gen.writeStartObject();
      Assert.fail();
    } catch (JsonGenerationException ex) {
      // ok
    }

    baos = new ByteArrayOutputStream();
    gen = Json.createGenerator(baos);
    try {
      gen.writeEnd();
      Assert.fail();
    } catch (JsonGenerationException ex) {
      // ok
    }

    baos = new ByteArrayOutputStream();
    gen = Json.createGenerator(baos);
    try {
      gen.write("hello", 1);
      Assert.fail();
    } catch (JsonGenerationException ex) {
      // ok
    }

    baos = new ByteArrayOutputStream();
    gen = Json.createGenerator(baos);
    try {
      gen.writeStartObject().writeEnd();
      gen.write("hello", 1);
      Assert.fail();
    } catch (JsonGenerationException ex) {
      // ok
    }

    baos = new ByteArrayOutputStream();
    gen = Json.createGenerator(baos);
    try {
      gen.writeStartArray();
      gen.write("hello", 1);
      Assert.fail();
    } catch (JsonGenerationException ex) {
      // ok
    }
  }

  @Test
  public void testBadGeneratorIo() throws Exception {
    OutputStream baos = new OutputStream() {

      @Override
      public void write(int data) throws IOException {}

      @Override
      public void flush() throws IOException {
        throw new IOException();
      }
    };
    JsonGenerator gen = Json.createGenerator(baos);
    try {
      gen.writeStartObject().writeEnd();
      gen.flush();
      Assert.fail();
    } catch (JsonException ex) {
      // ok
    }

    baos = new OutputStream() {
      @Override
      public void write(int data) throws IOException {
        throw new IOException();
      }
    };
    gen = Json.createGenerator(baos);
    try {
      gen.writeStartObject();
      gen.close();
      Assert.fail();
    } catch (JsonGenerationException ex) {
      // ok
    }

  }

}
