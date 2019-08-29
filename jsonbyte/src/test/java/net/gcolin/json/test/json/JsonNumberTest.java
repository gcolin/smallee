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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonWriter;

import org.junit.Test;

/**
 * JSON number tests.
 * 
 * @author Jitendra Kotamraju
 */
public class JsonNumberTest {

  @Test
  public void testFloating() throws Exception {
    JsonArray array1 = Json.createArrayBuilder().add(10.4).build();
    JsonReader reader = Json.createReader(new StringReader("[10.4]"));
    JsonArray array2 = reader.readArray();

    assertEquals(array1.get(0), array2.get(0));
    assertEquals(array1, array2);
  }

  @Test
  public void testBigDecimal() throws Exception {
    JsonArray array1 = Json.createArrayBuilder().add(new BigDecimal("10.4")).build();
    JsonReader reader = Json.createReader(new StringReader("[10.4]"));
    JsonArray array2 = reader.readArray();

    assertEquals(array1.get(0), array2.get(0));
    assertEquals(array1, array2);
  }

  @Test
  public void testIntNumberType() throws Exception {
    JsonArray array1 = Json.createArrayBuilder().add(Integer.MIN_VALUE).add(Integer.MAX_VALUE)
        .add(Integer.MIN_VALUE + 1).add(Integer.MAX_VALUE - 1).add(12).add(12L)
        .add(BigInteger.ZERO).build();
    testNumberType(array1, true);

    StringReader sr = new StringReader("[" + "-2147483648, " + "2147483647, " + "-2147483647, "
        + "2147483646, " + "12, " + "12, " + "0 " + "]");
    JsonReader reader = Json.createReader(sr);
    JsonArray array2 = reader.readArray();
    reader.close();
    testNumberType(array2, true);

    assertEquals(array1, array2);
  }

  private void testNumberType(JsonArray array, boolean integral) {
    for (JsonValue value : array) {
      assertEquals(integral, ((JsonNumber) value).isIntegral());
    }
  }

  @Test
  public void testLongNumberType() throws Exception {
    JsonArray array1 = Json.createArrayBuilder().add(Long.MIN_VALUE).add(Long.MAX_VALUE)
        .add(Long.MIN_VALUE + 1).add(Long.MAX_VALUE - 1).add((long) Integer.MIN_VALUE - 1)
        .add((long) Integer.MAX_VALUE + 1).build();
    testNumberType(array1, true);

    StringReader sr = new StringReader(
        "[" + "-9223372036854775808, " + "9223372036854775807, " + "-9223372036854775807, "
            + "9223372036854775806, " + "-2147483649, " + "2147483648 " + "]");
    JsonReader reader = Json.createReader(sr);
    JsonArray array2 = reader.readArray();
    reader.close();
    testNumberType(array2, true);

    assertEquals(array1, array2);
  }

  @Test
  public void testBigDecimalNumberType() throws Exception {
    JsonArray array1 = Json.createArrayBuilder().add(12d).add(12.0d).add(12.1d)
        .add(Double.MIN_VALUE).add(Double.MAX_VALUE).build();
    testNumberType(array1, false);

    StringReader sr = new StringReader(
        "[" + "12.0, " + "12.0, " + "12.1, " + "4.9E-324, " + "1.7976931348623157E+308 " + "]");
    JsonReader reader = Json.createReader(sr);
    JsonArray array2 = reader.readArray();
    reader.close();
    testNumberType(array2, false);

    assertEquals(array1, array2);

    JsonArray array3 = Json.createArrayBuilder().add(new BigDecimal("20.0")).build();

    testNumber0(array3, "20.0");
  }

  @Test
  public void testMinMax() throws Exception {
    JsonArray expected =
        Json.createArrayBuilder().add(Integer.MIN_VALUE).add(Integer.MAX_VALUE).add(Long.MIN_VALUE)
            .add(Long.MAX_VALUE).add(Double.MIN_VALUE).add(Double.MAX_VALUE).build();

    StringWriter sw = new StringWriter();
    JsonWriter writer = Json.createWriter(sw);
    writer.writeArray(expected);
    writer.close();

    JsonReader reader = Json.createReader(new StringReader(sw.toString()));
    JsonArray actual = reader.readArray();
    reader.close();

    assertEquals(expected, actual);
    assertEquals(expected.hashCode(), actual.hashCode());
    assertTrue(actual.get(0).equals(actual.get(0)));
  }

  @Test
  public void testLeadingZeroes() {
    JsonArray array = Json.createArrayBuilder().add(0012.1d).build();

    StringWriter sw = new StringWriter();
    JsonWriter jw = Json.createWriter(sw);
    jw.write(array);
    jw.close();

    assertEquals("[12.1]", sw.toString());
  }

  @Test
  public void testBigInteger() {
    JsonArray array3 = Json.createArrayBuilder().add(new BigInteger("20")).build();

    testNumber0(array3, "20");
  }

  private void testNumber0(JsonArray array3, String val) {
    JsonNumber nb = array3.getJsonNumber(0);
    assertEquals(20, nb.intValueExact());
    assertEquals(20, nb.intValue());
    assertEquals(20L, nb.longValue());
    assertEquals(20L, nb.longValueExact());
    assertEquals(new BigInteger("20"), nb.bigIntegerValue());
    assertEquals(new BigInteger("20"), nb.bigIntegerValueExact());
    assertEquals(20.0, nb.doubleValue(), 1.0);
    assertEquals(val, nb.toString());
  }

  @Test
  public void testLong() {
    JsonArray array3 = Json.createArrayBuilder().add(20L).build();

    testNumber0(array3, "20");
  }

  @Test
  public void testInt() {
    JsonArray array3 = Json.createArrayBuilder().add(20).build();

    testNumber0(array3, "20");
  }

  @Test
  public void testDouble() {
    JsonArray array3 = Json.createArrayBuilder().add(20.0).build();

    testNumber0(array3, "20.0");
  }

  @Test
  public void testBigIntegerExact() {
    try {
      JsonArray array = Json.createArrayBuilder().add(12345.12345).build();
      array.getJsonNumber(0).bigIntegerValueExact();
      fail("Expected Arithmetic exception");
    } catch (ArithmeticException expected) {
      // no-op
    }
  }

}
