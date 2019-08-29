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

import org.junit.Assert;
import org.junit.Test;

/**
 * Basix type tests.
 * 
 * @author Gaël COLIN
 */
public class TestBasicType extends AbstractMultiCharsetTest {

  public static class Obj {
    String str;
    Character ch;
    Byte bt;
    Short sh;
    Integer in;
    Long ln;
    Float fl;
    Double db;
    Boolean bl;
  }

  @Test
  public void testString() {
    Obj obj = new Obj();
    obj.str = "hello";
    Obj o2 = test0(Obj.class, obj, "{\"str\":\"hello\"}");
    Assert.assertEquals("hello", o2.str);
  }

  @Test
  public void testCharacter() {
    Obj obj = new Obj();
    obj.ch = 'a';
    Obj o2 = test0(Obj.class, obj, "{\"ch\":97}");
    Assert.assertEquals('a', o2.ch.charValue());
  }

  @Test
  public void testByte() {
    Obj obj = new Obj();
    obj.bt = 100;
    Obj o2 = test0(Obj.class, obj, "{\"bt\":100}");
    Assert.assertEquals(100, o2.bt.byteValue());
  }

  @Test
  public void testShort() {
    Obj obj = new Obj();
    obj.sh = 15;
    Obj o2 = test0(Obj.class, obj, "{\"sh\":15}");
    Assert.assertEquals(15, o2.sh.shortValue());
  }

  @Test
  public void testInteger() {
    Obj obj = new Obj();
    obj.in = 123456;
    Obj o2 = test0(Obj.class, obj, "{\"in\":123456}");
    Assert.assertEquals(123456, o2.in.intValue());
  }

  @Test
  public void testIntegerNegative() {
    Obj obj = new Obj();
    obj.in = -123456;
    Obj o2 = test0(Obj.class, obj, "{\"in\":-123456}");
    Assert.assertEquals(-123456, o2.in.intValue());
  }

  @Test
  public void testLong() {
    Obj obj = new Obj();
    obj.ln = 12345678910L;
    Obj o2 = test0(Obj.class, obj, "{\"ln\":12345678910}");
    Assert.assertEquals(12345678910L, o2.ln.longValue());
  }

  @Test
  public void testFloat() {
    Obj obj = new Obj();
    obj.fl = 1.2f;
    Obj o2 = test0(Obj.class, obj, null);
    Assert.assertEquals(1.2f, o2.fl.floatValue(), 0.1f);
  }

  @Test
  public void testDouble() {
    Obj obj = new Obj();
    obj.db = 1.3;
    Obj o2 = test0(Obj.class, obj, "{\"db\":1.3}");
    Assert.assertEquals(1.3, o2.db.doubleValue(), 0.1f);
  }

  @Test
  public void testBooleanTrue() {
    Obj obj = new Obj();
    obj.bl = true;
    Obj o2 = test0(Obj.class, obj, "{\"bl\":true}");
    Assert.assertTrue(o2.bl);
  }

  @Test
  public void testBooleanFalse() {
    Obj obj = new Obj();
    obj.bl = false;
    Obj o2 = test0(Obj.class, obj, "{\"bl\":false}");
    Assert.assertFalse(o2.bl);
  }

}
