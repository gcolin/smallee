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

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.json.stream.JsonParserFactory;

/**
 * JsonParser Tests.
 *
 * @author Jitendra Kotamraju
 */
public class JsonParserTest {
  static final Charset UTF_8 = Charset.forName("UTF-8");
  static final Charset UTF_16BE = Charset.forName("UTF-16BE");
  static final Charset UTF_16LE = Charset.forName("UTF-16LE");
  static final Charset UTF_16 = Charset.forName("UTF-16");
  static final Charset UTF_32LE = Charset.forName("UTF-32LE");
  static final Charset UTF_32BE = Charset.forName("UTF-32BE");

  @Test
  public void testReader() {
    JsonParser reader = Json
        .createParser(new StringReader("{ \"a\" : \"b\", \"c\" : null, \"d\" : [null, \"abc\"] }"));
    reader.close();
  }

  @Test
  public void testEmptyArrayReader() {
    JsonParser parser = Json.createParser(new StringReader("[]"));
    testEmptyArray(parser);
    parser.close();
  }
  
  @Test
  public void testIntGetString() {
      JsonParserFactory f = Json.createParserFactory(null);
      JsonObject obj = Json.createObjectBuilder().add("a", 5).build();
      try (JsonParser parser = f.createParser(obj)) {
          parser.next();
          parser.next();
          parser.next();
          assertEquals("Fails for int=5", "5", parser.getString());
      }
  }

  @Test
  public void testEmptyArrayStream() {
    JsonParser parser = Json.createParser(new ByteArrayInputStream(new byte[] {'[', ']'}));
    testEmptyArray(parser);
    parser.close();
  }

  @Test
  public void testEmptyArrayStreamUtf8() {
    ByteArrayInputStream bin = new ByteArrayInputStream("[]".getBytes(UTF_8));
    JsonParser parser = Json.createParser(bin);
    testEmptyArray(parser);
    parser.close();
  }

  @Test
  public void testEmptyArrayStreamUtf16Le() {
    ByteArrayInputStream bin = new ByteArrayInputStream("[]".getBytes(UTF_16LE));
    JsonParser parser = Json.createParser(bin);
    testEmptyArray(parser);
    parser.close();
  }

  @Test
  public void testEmptyArrayStreamUtf16Be() {
    ByteArrayInputStream bin = new ByteArrayInputStream("[]".getBytes(UTF_16BE));
    JsonParser parser = Json.createParser(bin);
    testEmptyArray(parser);
    parser.close();
  }

  @Test
  public void testEmptyArrayStreamUtf32Le() {
    ByteArrayInputStream bin = new ByteArrayInputStream("[]".getBytes(UTF_32LE));
    JsonParser parser = Json.createParser(bin);
    testEmptyArray(parser);
    parser.close();
  }

  @Test
  public void testEmptyArrayStreamUtf32Be() {
    ByteArrayInputStream bin = new ByteArrayInputStream("[]".getBytes(UTF_32BE));
    JsonParser parser = Json.createParser(bin);
    testEmptyArray(parser);
    parser.close();
  }

  @Test
  public void testEmptyArrayStreamUtf16() {
    ByteArrayInputStream bin = new ByteArrayInputStream("[]".getBytes(UTF_16));
    JsonParser parser = Json.createParser(bin);
    testEmptyArray(parser);
    parser.close();
  }

  @Test
  public void testEmptyArrayStreamWithConfig() {
    Map<String, ?> config = new HashMap<String, Object>();
    JsonParser parser = Json.createParserFactory(config)
        .createParser(new ByteArrayInputStream(new byte[] {'[', ']'}));
    testEmptyArray(parser);
    parser.close();
  }

  @Test
  public void testEmptyArrayStructure() {
    JsonParser parser =
        Json.createParserFactory(null).createParser(Json.createArrayBuilder().build());
    testEmptyArray(parser);
    parser.close();
  }

  @Test
  public void testEmptyArrayStructureWithConfig() {
    Map<String, ?> config = new HashMap<String, Object>();
    JsonParser parser =
        Json.createParserFactory(config).createParser(Json.createArrayBuilder().build());
    testEmptyArray(parser);
    parser.close();
  }

  static void testEmptyArray(JsonParser parser) {
    while (parser.hasNext()) {
      parser.next();
    }
  }

  @Test
  public void testEmptyArrayReaderIterator() {
    JsonParser parser = Json.createParser(new StringReader("[]"));
    testEmptyArrayIterator(parser);
    parser.close();
  }

  @Test
  public void testEmptyArrayStructureIterator() {
    JsonParser parser =
        Json.createParserFactory(null).createParser(Json.createArrayBuilder().build());
    testEmptyArrayIterator(parser);
    parser.close();
  }

  static void testEmptyArrayIterator(JsonParser parser) {
    assertEquals(true, parser.hasNext());
    assertEquals(true, parser.hasNext());
    assertEquals(Event.START_ARRAY, parser.next());

    assertEquals(true, parser.hasNext());
    assertEquals(true, parser.hasNext());
    assertEquals(Event.END_ARRAY, parser.next());

    assertEquals(false, parser.hasNext());
    assertEquals(false, parser.hasNext());
    try {
      parser.next();
      fail("Should have thrown a NoSuchElementException");
    } catch (NoSuchElementException ne) {
      // ok
    }
  }

  @Test
  public void testEmptyArrayIterator2Reader() {
    JsonParser parser = Json.createParser(new StringReader("[]"));
    testEmptyArrayIterator2(parser);
    parser.close();
  }

  @Test
  public void testEmptyArrayIterator2Structure() {
    JsonParser parser =
        Json.createParserFactory(null).createParser(Json.createArrayBuilder().build());
    testEmptyArrayIterator2(parser);
    parser.close();
  }

  static void testEmptyArrayIterator2(JsonParser parser) {
    assertEquals(Event.START_ARRAY, parser.next());
    assertEquals(Event.END_ARRAY, parser.next());
    try {
      parser.next();
      fail("Should have thrown a NoSuchElementException");
    } catch (NoSuchElementException ne) {
      // ok
    }
  }

  @Test
  public void testEmptyArrayIterator3Reader() {
    JsonParser parser = Json.createParser(new StringReader("[]"));
    testEmptyArrayIterator3(parser);
    parser.close();
  }

  @Test
  public void testEmptyArrayIterator3Structure() {
    JsonParser parser =
        Json.createParserFactory(null).createParser(Json.createArrayBuilder().build());
    testEmptyArrayIterator3(parser);
    parser.close();
  }

  static void testEmptyArrayIterator3(JsonParser parser) {
    assertEquals(Event.START_ARRAY, parser.next());
    assertEquals(Event.END_ARRAY, parser.next());
    assertEquals(false, parser.hasNext());
    try {
      parser.next();
      fail("Should have thrown a NoSuchElementException");
    } catch (NoSuchElementException ne) {
      // ok
    }
  }

  @Test
  // Tests empty object
  public void testEmptyObjectReader() {
    JsonParser parser = Json.createParser(new StringReader("{}"));
    testEmptyObject(parser);
    parser.close();
  }

  @Test
  public void testEmptyObjectStream() {
    JsonParser parser = Json.createParser(new ByteArrayInputStream(new byte[] {'{', '}'}));
    testEmptyObject(parser);
    parser.close();
  }

  @Test
  public void testEmptyObjectStructure() {
    JsonParser parser =
        Json.createParserFactory(null).createParser(Json.createObjectBuilder().build());
    testEmptyObject(parser);
    parser.close();
  }

  @Test
  public void testEmptyObjectStructureWithConfig() {
    Map<String, ?> config = new HashMap<String, Object>();
    JsonParser parser =
        Json.createParserFactory(config).createParser(Json.createObjectBuilder().build());
    testEmptyObject(parser);
    parser.close();
  }

  static void testEmptyObject(JsonParser parser) {
    while (parser.hasNext()) {
      parser.next();
    }
  }

  @Test
  public void testEmptyObjectIteratorReader() {
    JsonParser parser = Json.createParser(new StringReader("{}"));
    testEmptyObjectIterator(parser);
    parser.close();
  }

  @Test
  public void testEmptyObjectIteratorStructure() {
    JsonParser parser =
        Json.createParserFactory(null).createParser(Json.createObjectBuilder().build());
    testEmptyObjectIterator(parser);
    parser.close();
  }

  static void testEmptyObjectIterator(JsonParser parser) {
    assertEquals(true, parser.hasNext());
    assertEquals(true, parser.hasNext());
    assertEquals(Event.START_OBJECT, parser.next());

    assertEquals(true, parser.hasNext());
    assertEquals(true, parser.hasNext());
    assertEquals(Event.END_OBJECT, parser.next());

    assertEquals(false, parser.hasNext());
    assertEquals(false, parser.hasNext());
    try {
      parser.next();
      fail("Should have thrown a NoSuchElementException");
    } catch (NoSuchElementException ne) {
      // ok
    }
  }

  @Test
  public void testEmptyObjectIterator2Reader() {
    JsonParser parser = Json.createParser(new StringReader("{}"));
    testEmptyObjectIterator2(parser);
    parser.close();
  }

  @Test
  public void testEmptyObjectIterator2Structure() {
    JsonParser parser =
        Json.createParserFactory(null).createParser(Json.createObjectBuilder().build());
    testEmptyObjectIterator2(parser);
    parser.close();
  }

  static void testEmptyObjectIterator2(JsonParser parser) {
    assertEquals(Event.START_OBJECT, parser.next());
    assertEquals(Event.END_OBJECT, parser.next());
    try {
      parser.next();
      fail("Should have thrown a NoSuchElementException");
    } catch (NoSuchElementException ne) {
      // ok
    }
  }

  @Test
  public void testEmptyObjectIterator3Reader() {
    JsonParser parser = Json.createParser(new StringReader("{}"));
    testEmptyObjectIterator3(parser);
    parser.close();
  }

  @Test
  public void testEmptyObjectIterator3Structure() {
    JsonParser parser =
        Json.createParserFactory(null).createParser(Json.createObjectBuilder().build());
    testEmptyObjectIterator3(parser);
    parser.close();
  }

  static void testEmptyObjectIterator3(JsonParser parser) {
    assertEquals(Event.START_OBJECT, parser.next());
    assertEquals(Event.END_OBJECT, parser.next());
    assertEquals(false, parser.hasNext());
    try {
      parser.next();
      fail("Should have thrown a NoSuchElementException");
    } catch (NoSuchElementException ne) {
      // expected
    }
  }

  @Test
  public void testStructureStructure() {
    JsonParser parser = Json.createParserFactory(null).createParser(
        Json.createArrayBuilder().add(false).add(true).addNull().add(2.1).add(2).build());
    assertEquals(Event.START_ARRAY, parser.next());
    assertEquals(Event.VALUE_FALSE, parser.next());
    assertEquals(Event.VALUE_TRUE, parser.next());
    assertEquals(Event.VALUE_NULL, parser.next());
    assertEquals(Event.VALUE_NUMBER, parser.next());
    assertFalse(parser.isIntegralNumber());
    assertEquals(Event.VALUE_NUMBER, parser.next());
    assertTrue(parser.isIntegralNumber());
    assertEquals(Event.END_ARRAY, parser.next());
    parser.close();
  }

  @Test
  public void testStructureWithInner() {
    JsonParser parser = Json.createParserFactory(null)
        .createParser(Json.createArrayBuilder().add(Json.createObjectBuilder().build()).build());
    assertEquals(Event.START_ARRAY, parser.next());
    assertEquals(Event.START_OBJECT, parser.next());
    assertEquals(Event.END_OBJECT, parser.next());
    assertEquals(Event.END_ARRAY, parser.next());
    parser.close();
  }

  @Test
  public void testWikiIteratorReader() throws Exception {
    JsonParser parser = Json.createParser(wikiReader());
    testWikiIterator(parser);
    parser.close();
  }

  @Test
  public void testWikiIteratorStructure() throws Exception {
    JsonParser parser = Json.createParserFactory(null).createParser(JsonBuilderTest.buildPerson());
    testWikiIterator(parser);
    parser.close();
  }

  static void testWikiIterator(JsonParser parser) throws Exception {
    while (parser.hasNext()) {
      parser.next();
    }
  }

  @Test
  public void testWikiInputStream() throws Exception {
    JsonParser parser = Json.createParser(wikiStream());
    testWiki(parser);
    parser.close();
  }

  @Test
  public void testWikiInputStreamUtf16Le() throws Exception {
    ByteArrayInputStream bin = new ByteArrayInputStream(wikiString().getBytes(UTF_16LE));
    JsonParser parser = Json.createParser(bin);
    testWiki(parser);
    parser.close();
  }

  @Test
  public void testWikiReader() throws Exception {
    JsonParser parser = Json.createParser(wikiReader());
    testWiki(parser);
    parser.close();
  }

  @Test
  public void testWikiStructure() throws Exception {
    JsonObject oo = JsonBuilderTest.buildPerson();

    JsonParser parser = Json.createParserFactory(null).createParser(oo);
    testWiki(parser);
    parser.close();
  }

  static void testWiki(JsonParser parser) {

    Event event = parser.next();
    assertEquals(Event.START_OBJECT, event);

    testObjectStringValue(parser, "firstName", "John");
    testObjectStringValue(parser, "lastName", "Smith");

    event = parser.next();
    assertEquals(Event.KEY_NAME, event);
    assertEquals("age", parser.getString());

    event = parser.next();
    assertEquals(Event.VALUE_NUMBER, event);
    assertEquals(25, parser.getInt());
    assertEquals(25, parser.getLong());
    assertEquals(25, parser.getBigDecimal().intValue());
    assertTrue(parser.isIntegralNumber());

    event = parser.next();
    assertEquals(Event.KEY_NAME, event);
    assertEquals("address", parser.getString());

    event = parser.next();
    assertEquals(Event.START_OBJECT, event);

    testObjectStringValue(parser, "streetAddress", "21 2nd Street");
    testObjectStringValue(parser, "city", "New York");
    testObjectStringValue(parser, "state", "NY");
    testObjectStringValue(parser, "postalCode", "10021");

    event = parser.next();
    assertEquals(Event.END_OBJECT, event);

    event = parser.next();
    assertEquals(Event.KEY_NAME, event);
    assertEquals("phoneNumber", parser.getString());

    event = parser.next();
    assertEquals(Event.START_ARRAY, event);
    event = parser.next();
    assertEquals(Event.START_OBJECT, event);
    testObjectStringValue(parser, "type", "home");
    testObjectStringValue(parser, "number", "212 555-1234");
    event = parser.next();
    assertEquals(Event.END_OBJECT, event);

    event = parser.next();
    assertEquals(Event.START_OBJECT, event);
    testObjectStringValue(parser, "type", "fax");
    testObjectStringValue(parser, "number", "646 555-4567");
    event = parser.next();
    assertEquals(Event.END_OBJECT, event);
    event = parser.next();
    assertEquals(Event.END_ARRAY, event);

    event = parser.next();
    assertEquals(Event.END_OBJECT, event);
  }

  static void testObjectStringValue(JsonParser parser, String name, String value) {
    Event event = parser.next();
    assertEquals(Event.KEY_NAME, event);
    assertEquals(name, parser.getString());

    event = parser.next();
    assertEquals(Event.VALUE_STRING, event);
    assertEquals(value, parser.getString());
  }

  @Test
  public void testNestedArrayReader() {
    JsonParser parser = Json.createParser(new StringReader("[[],[[]]]"));
    testNestedArray(parser);
    parser.close();
  }

  @Test
  public void testNestedArrayStructure() {
    JsonParser parser = Json.createParserFactory(null)
        .createParser(Json.createArrayBuilder().add(Json.createArrayBuilder())
            .add(Json.createArrayBuilder().add(Json.createArrayBuilder())).build());
    testNestedArray(parser);
    parser.close();
  }

  static void testNestedArray(JsonParser parser) {
    assertEquals(Event.START_ARRAY, parser.next());
    assertEquals(Event.START_ARRAY, parser.next());
    assertEquals(Event.END_ARRAY, parser.next());
    assertEquals(Event.START_ARRAY, parser.next());
    assertEquals(Event.START_ARRAY, parser.next());
    assertEquals(Event.END_ARRAY, parser.next());
    assertEquals(Event.END_ARRAY, parser.next());
    assertEquals(Event.END_ARRAY, parser.next());
    assertEquals(false, parser.hasNext());
    assertEquals(false, parser.hasNext());
  }

  @Test
  public void testExceptionsReader() throws Exception {
    JsonParser parser = Json.createParser(wikiReader());
    testExceptions(parser);
    parser.close();
  }

  @Test
  public void testExceptionsStructure() throws Exception {
    JsonParser parser = Json.createParserFactory(null).createParser(JsonBuilderTest.buildPerson());
    testExceptions(parser);
    parser.close();
  }

  static void testExceptions(JsonParser parser) {

    Event event = parser.next();
    assertEquals(Event.START_OBJECT, event);

    try {
      parser.getString();
      fail("JsonParser#getString() should have thrown exception in START_OBJECT state");
    } catch (IllegalStateException expected) {
      // no-op
    }

    try {
      parser.isIntegralNumber();
      fail("JsonParser#getNumberType() should have thrown exception in START_OBJECT state");
    } catch (IllegalStateException expected) {
      // no-op
    }

    try {
      parser.getInt();
      fail("JsonParser#getInt() should have thrown exception in START_OBJECT state");
    } catch (IllegalStateException expected) {
      // no-op
    }

    try {
      parser.getLong();
      fail("JsonParser#getLong() should have thrown exception in START_OBJECT state");
    } catch (IllegalStateException expected) {
      // no-op
    }

    try {
      parser.getBigDecimal();
      fail("JsonParser#getBigDecimal() should have thrown exception in START_OBJECT state");
    } catch (IllegalStateException expected) {
      // no-op
    }
  }

  static String wikiString() {
    String str = null;
    try (java.util.Scanner scanner = new java.util.Scanner(wikiReader())) {
      scanner.useDelimiter("\\A");
      str = scanner.hasNext() ? scanner.next() : "";
      scanner.close();
    }
    return str;
  }

  static InputStream wikiStream() {
    return JsonParserTest.class.getResourceAsStream("/wiki.json");
  }

  static Reader wikiReader() {
    return new InputStreamReader(
        JsonParserTest.class.getClassLoader().getResourceAsStream("wiki.json"), UTF_8);
  }

  @Test
  public void testIntNumber() {
    JsonParserFactory factory = Json.createParserFactory(null);

    Random rand = new Random(System.currentTimeMillis());

    for (int i = 0; i < 100000; i++) {
      long num = i % 2 == 0 ? rand.nextInt() : rand.nextLong();
      JsonParser parser = factory.createParser(new StringReader("[" + num + "]"));
      parser.next();
      parser.next();
      assertEquals("Fails for num=" + num, new BigDecimal(num).intValue(), parser.getInt());
      parser.close();
    }

  }

  @Test
  public void testBufferPoolFeature() {
    JsonParserFactory factory = Json.createParserFactory(null);
    JsonParser parser = factory.createParser(new StringReader("[]"));
    parser.next();
    parser.next();
    parser.close();
  }

  @Test
  public void testBufferSizes() {
    Random rand = new Random(System.currentTimeMillis());
    for (int size = 100; size < 10; size++) {
      JsonParserFactory factory = Json.createParserFactory(null);

      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < 10; i++) {
        sb.append('a');
        String name = sb.toString();
        long num = i % 2 == 0 ? rand.nextInt() : rand.nextLong();
        String str = "{\"" + name + "\":[" + num + "]}";
        JsonParser parser = factory.createParser(new StringReader(str));
        parser.next();
        parser.next();
        assertEquals("Fails for " + str, name, parser.getString());
        parser.next();
        parser.next();
        assertEquals("Fails for " + str, new BigDecimal(num).intValue(), parser.getInt());
        parser.close();
      }
    }
  }

  @Test
  // Tests for string starting on buffer boundary (JSONP-15)
  // xxxxxxx"xxxxxxxxx"
  // ^
  // |
  // 4096
  public void testStringUsingStandardBuffer() throws Throwable {
    JsonParserFactory factory = Json.createParserFactory(null);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 40; i++) {
      sb.append('a');
      String name = sb.toString();
      String str = "{\"" + name + "\":\"" + name + "\"}";
      JsonParser parser = factory.createParser(new StringReader(str));
      parser.next();
      parser.next();
      assertEquals("Fails for size=" + i, name, parser.getString());
      parser.next();
      assertEquals("Fails for size=" + i, name, parser.getString());
      parser.close();
    }
  }

  @Test
  // Tests for int starting on buffer boundary
  // xxxxxxx"xxxxxxxxx"
  // ^
  // |
  // 4096
  public void testIntegerUsingStandardBuffer() throws Throwable {
    Random rand = new Random(System.currentTimeMillis());
    JsonParserFactory factory = Json.createParserFactory(null);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 40; i++) {
      sb.append('a');
      String name = sb.toString();
      int num = rand.nextInt();
      String str = "{\"" + name + "\":" + num + "}";
      JsonParser parser = factory.createParser(new StringReader(str));
      parser.next();
      parser.next();
      assertEquals("Fails for size=" + i, name, parser.getString());
      parser.next();
      assertEquals("Fails for size=" + i, num, parser.getInt());
      parser.close();
    }
  }

}
