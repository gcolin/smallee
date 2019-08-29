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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerationException;

/**
 * JSON writer tests.
 * 
 * @author Jitendra Kotamraju
 */
public class JsonWriterTest {

  @Test
  public void testObject() throws Exception {
    StringWriter writer = new StringWriter();
    JsonWriter jsonWriter = Json.createWriter(writer);
    jsonWriter.writeObject(Json.createObjectBuilder().build());
    jsonWriter.close();
    writer.close();

    Assert.assertEquals("{}", writer.toString());
  }

  @Test
  public void testArray() throws Exception {
    StringWriter writer = new StringWriter();
    JsonWriter jsonWriter = Json.createWriter(writer);
    jsonWriter.writeArray(Json.createArrayBuilder().build());
    jsonWriter.close();
    writer.close();

    Assert.assertEquals("[]", writer.toString());
  }

  @Test
  public void testNumber() throws Exception {
    StringWriter writer = new StringWriter();
    JsonWriter jsonWriter = Json.createWriter(writer);
    jsonWriter.writeArray(Json.createArrayBuilder().add(10).build());
    jsonWriter.close();
    writer.close();

    Assert.assertEquals("[10]", writer.toString());
  }

  @Test
  public void testDoubleNumber() throws Exception {
    StringWriter writer = new StringWriter();
    JsonWriter jsonWriter = Json.createWriter(writer);
    jsonWriter.writeArray(Json.createArrayBuilder().add(10.5).build());
    jsonWriter.close();
    writer.close();

    Assert.assertEquals("[10.5]", writer.toString());
  }

  @Test
  public void testArrayString() throws Exception {
    StringWriter writer = new StringWriter();
    JsonWriter jsonWriter = Json.createWriter(writer);
    jsonWriter.writeArray(Json.createArrayBuilder().add("string").build());
    jsonWriter.close();
    writer.close();

    Assert.assertEquals("[\"string\"]", writer.toString());
  }

  @Test
  public void testIllegalStateExcepton() throws Exception {
    JsonObject obj = Json.createObjectBuilder().build();

    JsonWriter writer = Json.createWriter(new StringWriter());
    writer.writeObject(obj);
    try {
      writer.writeObject(obj);
    } catch (IllegalStateException expected) {
      // no-op
    }
    writer.close();

    writer = Json.createWriter(new StringWriter());
    JsonArray array = Json.createArrayBuilder().build();
    writer.writeArray(array);
    try {
      writer.writeArray(array);
    } catch (IllegalStateException expected) {
      // no-op
    }
    writer.close();

    writer = Json.createWriter(new StringWriter());
    writer.write(array);
    try {
      writer.writeArray(array);
    } catch (IllegalStateException expected) {
      // no-op
    }
    writer.close();
  }

  @Test
  public void testNoCloseWriteObjectToStream() throws Exception {
    StringWriter baos = new StringWriter();
    JsonWriter writer = Json.createWriter(baos);
    writer.write(Json.createObjectBuilder().build());
    // not calling writer.close() intentionally
    Assert.assertEquals("{}", baos.toString());
  }

  @Test
  public void testNoCloseWriteObjectToWriter() throws Exception {
    StringWriter sw = new StringWriter();
    JsonWriter writer = Json.createWriter(sw);
    writer.write(Json.createObjectBuilder().build());
    // not calling writer.close() intentionally
    Assert.assertEquals("{}", sw.toString());
  }

  @Test
  public void testNoCloseWriteArrayToStream() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JsonWriter writer = Json.createWriter(baos);
    writer.write(Json.createArrayBuilder().build());
    // not calling writer.close() intentionally
    Assert.assertEquals("[]", baos.toString("UTF-8"));
  }

  @Test
  public void testNoCloseWriteArrayToStream2() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JsonWriter writer = Json.createWriterFactory(null).createWriter(baos, StandardCharsets.UTF_8);
    writer.write(Json.createArrayBuilder().build());
    // not calling writer.close() intentionally
    Assert.assertEquals("[]", baos.toString("UTF-8"));
  }

  @Test
  public void testNoCloseWriteArrayToWriter() throws Exception {
    StringWriter sw = new StringWriter();
    JsonWriter writer = Json.createWriterFactory(null).createWriter(sw);
    writer.write(Json.createArrayBuilder().build());
    // not calling writer.close() intentionally
    Assert.assertEquals("[]", sw.toString());
  }

  @Test
  public void testClose() throws Exception {
    MyByteStream baos = new MyByteStream();
    JsonWriter writer = Json.createWriter(baos);
    writer.write(Json.createObjectBuilder().build());
    writer.close();
    Assert.assertEquals("{}", baos.toString("UTF-8"));
    Assert.assertTrue(baos.isClosed());
  }

  @Test
  public void testNoSource() throws Exception {
    try {
      Json.createWriter((Writer) null);
    } catch (JsonGenerationException ex) {
      // ok
    }
  }

  private static final class MyByteStream extends ByteArrayOutputStream {
    boolean closed;

    boolean isClosed() {
      return closed;
    }

    public void close() throws IOException {
      super.close();
      closed = true;
    }
  }
}
