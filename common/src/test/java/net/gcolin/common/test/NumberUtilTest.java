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

package net.gcolin.common.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import net.gcolin.common.lang.NumberUtil;

import org.junit.Test;

public class NumberUtilTest {

  @Test
  public void testExtractDouble() {
    assertEquals(12.0, NumberUtil.extractDouble("12").doubleValue(), 0.5);
    assertEquals(12.0, NumberUtil.extractDouble("12 soles").doubleValue(), 0.5);
    assertEquals(12.0, NumberUtil.extractDouble("12.0").doubleValue(), 0.5);
    assertEquals(12.0, NumberUtil.extractDouble("aprox 12.0 soles").doubleValue(), 0.5);
    assertNull(NumberUtil.extractDouble(null));
  }

  @Test
  public void testParseDouble() {
    assertEquals(12.0, NumberUtil.parseDouble("12").doubleValue(), 0.5);
    assertNull(NumberUtil.parseDouble("12 soles"));
    assertEquals(12.0, NumberUtil.parseDouble("12.0").doubleValue(), 0.5);
    assertNull(NumberUtil.parseDouble("aprox 12.0 soles"));
    assertNull(NumberUtil.parseDouble(null));

    assertEquals(12.0, NumberUtil.parseDouble("12", 0), 0.5);
    assertEquals(0.0, NumberUtil.parseDouble("12 soles", 0), 0.5);
    assertEquals(12.0, NumberUtil.parseDouble("12.0", 0), 0.5);
    assertEquals(0.0, NumberUtil.parseDouble("aprox 12.0 soles", 0), 0.5);
    assertEquals(0.0, NumberUtil.parseDouble(null, 0), 0.5);
  }

  @Test
  public void testParseFloat() {
    assertEquals(12.0f, NumberUtil.parseFloat("12").doubleValue(), 0.5f);
    assertNull(NumberUtil.parseFloat("12 soles"));
    assertEquals(12.0f, NumberUtil.parseFloat("12.0").doubleValue(), 0.5f);
    assertNull(NumberUtil.parseFloat("aprox 12.0 soles"));
    assertNull(NumberUtil.parseFloat(null));

    assertEquals(12.0f, NumberUtil.parseFloat("12", 0), 0.5f);
    assertEquals(0.0f, NumberUtil.parseFloat("12 soles", 0), 0.5f);
    assertEquals(12.0f, NumberUtil.parseFloat("12.0", 0), 0.5f);
    assertEquals(0.0f, NumberUtil.parseFloat("aprox 12.0 soles", 0), 0.5f);
    assertEquals(0.0f, NumberUtil.parseFloat(null, 0), 0.5f);
  }

  @Test
  public void testParseInt() {
    assertEquals(12, NumberUtil.parseInt("12").intValue());
    assertNull(NumberUtil.parseInt("12 soles"));
    assertNull(NumberUtil.parseInt("12.0"));
    assertNull(NumberUtil.parseInt("aprox 12.0 soles"));
    assertNull(NumberUtil.parseInt(null));

    assertEquals(12, NumberUtil.parseInt("12", 0));
    assertEquals(0, NumberUtil.parseInt("12 soles", 0));
    assertEquals(0, NumberUtil.parseInt("12.0", 0));
    assertEquals(0, NumberUtil.parseInt("aprox 12.0 soles", 0));
    assertEquals(0, NumberUtil.parseInt(null, 0));
  }

  @Test
  public void testParseShort() {
    assertEquals((short) 12, NumberUtil.parseShort("12").shortValue());
    assertNull(NumberUtil.parseShort("12 soles"));
    assertNull(NumberUtil.parseShort("12.0"));
    assertNull(NumberUtil.parseShort("aprox 12.0 soles"));
    assertNull(NumberUtil.parseShort(null));

    assertEquals((short) 12, NumberUtil.parseShort("12", (short) 0));
    assertEquals((short) 0, NumberUtil.parseShort("12 soles", (short) 0));
    assertEquals((short) 0, NumberUtil.parseShort("12.0", (short) 0));
    assertEquals((short) 0, NumberUtil.parseShort("aprox 12.0 soles", (short) 0));
    assertEquals((short) 0, NumberUtil.parseShort(null, (short) 0));
  }

  @Test
  public void testParseLong() {
    assertEquals(12L, NumberUtil.parseLong("12").longValue());
    assertNull(NumberUtil.parseLong("12 soles"));
    assertNull(NumberUtil.parseLong("12.0"));
    assertNull(NumberUtil.parseLong("aprox 12.0 soles"));
    assertNull(NumberUtil.parseLong(null));

    assertEquals(12L, NumberUtil.parseLong("12", 0));
    assertEquals(0L, NumberUtil.parseLong("12 soles", 0));
    assertEquals(0L, NumberUtil.parseLong("12.0", 0));
    assertEquals(0L, NumberUtil.parseLong("aprox 12.0 soles", 0));
    assertEquals(0L, NumberUtil.parseLong(null, 0));
  }

}
