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

package net.gcolin.json.test.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonReader;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.json.stream.JsonParsingException;

/**
 * @author Gael COLIN
 * @since 1.0
 */
public class JsonParser2Test {

  @Test
  public void location() {
    JsonParser parser = Json.createParser(new StringReader("[0]"));
    Assert.assertNotNull(parser.getLocation());
  }

  @Test
  public void bad() {
    for (String val : new String[] {"[f]", "[fa]", "[fal]", "[fals]", "[t]", "[tr]", "[tru]", "[n]",
        "[nu]", "[nul]", "[1ee]", "[\"\t\"]", "[\"\\\0\"]", "", "[}", "{]"}) {
      try {
        JsonReader re = Json.createReader(new StringReader(val));
        re.read();
        Assert.fail("should fail with : " + val);
      } catch (JsonParsingException ex) {
        Assert.assertNotNull(ex.getLocation());
      }
    }

    for (String val : new String[] {"0"}) {
      try {
        JsonReader re = Json.createReader(new StringReader(val));
        re.read();
        Assert.fail();
      } catch (Exception ex) {
        Assert.assertNotNull(ex);
      }
    }
  }

  @Test
  public void badIo() {
    try {
      JsonReader reader = Json.createReader(new Reader() {

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
          throw new IOException();
        }

        @Override
        public void close() throws IOException {}

      });
      reader.read();
      Assert.fail();
    } catch (JsonException ex) {
      // OK
    }
  }

  @Test
  public void testIntegral() {
    JsonParser parser = Json.createParserFactory(null).createParser(new StringReader("[1,1.1]"));
    assertEquals(Event.START_ARRAY, parser.next());
    assertEquals(Event.VALUE_NUMBER, parser.next());
    assertTrue(parser.isIntegralNumber());
    assertEquals(Event.VALUE_NUMBER, parser.next());
    assertFalse(parser.isIntegralNumber());
    assertEquals(Event.END_ARRAY, parser.next());
    parser.close();
  }

}
