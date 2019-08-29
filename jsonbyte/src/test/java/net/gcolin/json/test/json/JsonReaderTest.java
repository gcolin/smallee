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

import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonValue;

/**
 * JSON reader tests.
 * 
 * @author Jitendra Kotamraju
 * @author Gaël COLIN
 */
public class JsonReaderTest {

  @Test
  public void testCharset() throws Exception {
    try (InputStream in = JsonReaderTest.class.getResourceAsStream("/wiki.json")) {
      JsonReader reader = Json.createReaderFactory(null).createReader(in, StandardCharsets.UTF_8);
      JsonObject value = reader.readObject();
      reader.close();
      JsonObjectTest.testPerson(value);
    }
  }

  @Test
  public void testUnknownFeature() throws Exception {
      Map<String, Object> config = new HashMap<>();
      config.put("foo", true);
      JsonReaderFactory factory = Json.createReaderFactory(config);
      factory.createReader(new StringReader("{}"));
      Map<String, ?> config1 = factory.getConfigInUse();
      if (config1.size() > 0) {
          Assert.fail("Shouldn't have any config in use");
      }
  }
  
  @Test
  public void testObject() throws Exception {
    JsonObject person = readPerson();
    JsonObjectTest.testPerson(person);
  }

  @Test
  public void testEscapedString() throws Exception {
    // u00ff is escaped once, not escaped once
    JsonReader reader = Json.createReader(new StringReader("[\"\\u0000\\u00ff\u00ff\"]"));
    JsonArray array = reader.readArray();
    reader.close();
    String str = array.getString(0);
    Assert.assertEquals("\u0000\u00ff\u00ff", str);
  }

  @Test
  public void testIllegalStateExcepton() throws Exception {
    JsonReader reader = Json.createReader(new StringReader("{}"));
    reader.readObject();
    try {
      reader.readObject();
    } catch (IllegalStateException expected) {
      // no-op
    }
    reader.close();

    reader = Json.createReader(new StringReader("[]"));
    reader.readArray();
    try {
      reader.readArray();
    } catch (IllegalStateException expected) {
      // no-op
    }
    reader.close();

    reader = Json.createReader(new StringReader("{}"));
    reader.read();
    try {
      reader.read();
    } catch (IllegalStateException expected) {
      // no-op
    }
    reader.close();
  }

  static JsonObject readPerson() throws Exception {
    Reader wikiReader = new InputStreamReader(
        JsonReaderTest.class.getResourceAsStream("/wiki.json"), StandardCharsets.UTF_8);
    JsonReader reader = Json.createReader(wikiReader);
    JsonValue value = reader.readObject();
    reader.close();
    return (JsonObject) value;
  }

  @Test
  // JSONP-23 cached empty string is not reset
  public void testEmptyStringUsingStandardBuffer() throws Throwable {
    JsonReaderFactory factory = Json.createReaderFactory(null);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 40; i++) {
      sb.append('a');
      String name = sb.toString();
      String str = "[1, \"\", \"" + name + "\", \"\", \"" + name + "\", \"\", 100]";
      JsonReader reader = factory.createReader(new StringReader(str));
      JsonArray array = reader.readArray();
      Assert.assertEquals(1, array.getInt(0));
      Assert.assertEquals("", array.getString(1));
      Assert.assertEquals(name, array.getString(2));
      Assert.assertEquals("", array.getString(3));
      Assert.assertEquals(name, array.getString(4));
      Assert.assertEquals("", array.getString(5));
      Assert.assertEquals(100, array.getInt(6));
      reader.close();
    }
  }

  @Test
  // JSONP-23 cached empty string is not reset
  public void testEmptyStringUsingBuffers() throws Throwable {
    for (int size = 20; size < 5; size++) {

      JsonReaderFactory factory = Json.createReaderFactory(null);

      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < 10; i++) {
        sb.append('a');
        String name = sb.toString();
        String str = "[1, \"\", \"" + name + "\", \"\", \"" + name + "\", \"\", 100]";

        JsonReader reader = factory.createReader(new StringReader(str));
        JsonArray array = reader.readArray();
        Assert.assertEquals(1, array.getInt(0));
        Assert.assertEquals("", array.getString(1));
        Assert.assertEquals(name, array.getString(2));
        Assert.assertEquals("", array.getString(3));
        Assert.assertEquals(name, array.getString(4));
        Assert.assertEquals("", array.getString(5));
        Assert.assertEquals(100, array.getInt(6));
        reader.close();
      }
    }
  }

}
