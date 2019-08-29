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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * @author Gaël COLIN
 */
public class TestStandardJavaSeTypes extends AbstractMultiCharsetTest {

  public static class Obj {
    BigInteger bi;
    BigDecimal bd;
    URL url;
    URI uri;
    Optional<String> opt;
    OptionalInt optInt;
    OptionalLong optLong;
    OptionalDouble optDouble;
  }

  @Test
  public void testBigInteger() {
    BigInteger bi = new BigInteger("123456789123465789");
    Obj obj = new Obj();
    obj.bi = bi;
    Obj o2 = test0(Obj.class, obj, "{\"bi\":123456789123465789}");
    Assert.assertEquals(bi, o2.bi);
  }

  @Test
  public void testBigDecimal() {
    BigDecimal bd = new BigDecimal("12345678912346578.9");
    Obj obj = new Obj();
    obj.bd = bd;
    Obj o2 = test0(Obj.class, obj, "{\"bd\":12345678912346578.9}");
    Assert.assertEquals(bd, o2.bd);
  }

  @Test
  public void testUrl() throws MalformedURLException {
    URL u = new URL("http://example.com");
    Obj obj = new Obj();
    obj.url = u;
    Obj o2 = test0(Obj.class, obj, "{\"url\":\"http://example.com\"}");
    Assert.assertEquals(u, o2.url);
  }

  @Test
  public void testUri() throws URISyntaxException {
    URI u = new URI("http://example.com");
    Obj obj = new Obj();
    obj.uri = u;
    Obj o2 = test0(Obj.class, obj, "{\"uri\":\"http://example.com\"}");
    Assert.assertEquals(u, o2.uri);
  }

  @Test
  public void testOptionalNull() {
    Obj o2 = test0(Obj.class, new Obj(), "{}");
    Assert.assertFalse(o2.opt.isPresent());
    Assert.assertFalse(o2.optDouble.isPresent());
    Assert.assertFalse(o2.optInt.isPresent());
    Assert.assertFalse(o2.optLong.isPresent());
  }

  @Test
  public void testOptional() {
    Obj obj = new Obj();
    obj.opt = Optional.of("hello");
    Obj o2 = test0(Obj.class, obj, "{\"opt\":\"hello\"}");
    Assert.assertTrue(o2.opt.isPresent());
    Assert.assertEquals("hello", o2.opt.get());
  }

  @Test
  public void testOptionalInt() {
    Obj obj = new Obj();
    obj.optInt = OptionalInt.of(123);
    Obj o2 = test0(Obj.class, obj, "{\"optInt\":123}");
    Assert.assertTrue(o2.optInt.isPresent());
    Assert.assertEquals(123, o2.optInt.getAsInt());
  }

  @Test
  public void testOptionalLong() {
    Obj obj = new Obj();
    obj.optLong = OptionalLong.of(123456789L);
    Obj o2 = test0(Obj.class, obj, "{\"optLong\":123456789}");
    Assert.assertTrue(o2.optLong.isPresent());
    Assert.assertEquals(123456789L, o2.optLong.getAsLong());
  }

  @Test
  public void testOptionalDouble() {
    Obj obj = new Obj();
    obj.optDouble = OptionalDouble.of(1.9);
    Obj o2 = test0(Obj.class, obj, "{\"optDouble\":1.9}");
    Assert.assertTrue(o2.optDouble.isPresent());
    Assert.assertEquals(1.9, o2.optDouble.getAsDouble(), 0.1);
  }

}
