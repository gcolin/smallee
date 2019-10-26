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

import java.util.NoSuchElementException;
import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Test;

import net.gcolin.common.lang.CharIterator;
import net.gcolin.common.lang.Strings;

public class StringsTest {

  @Test
  public void testisIp() {
    Assert.assertFalse(Strings.isIp("168.0.1"));
    Assert.assertTrue(Strings.isIp("192" + ".168.0.1"));
    Assert.assertTrue(Strings.isIp("2001:0db8:0000" + "85a3:0000:0000:ac1f:8001"));
  }

  @Test
  public void testXmlEncode() {
    Assert.assertEquals("&lt;&gt;&amp;\"h", Strings.encodeXml("<>&\"h", false));
    Assert.assertEquals("&lt;&gt;&amp;&quot;h", Strings.encodeXml("<>&\"h", true));

    for (int i = 34; i < 256; i++) {
      String str = Strings.decodeXml(Strings.encodeXml(String.valueOf((char) i), true));
      Assert.assertEquals(1, str.length());
      Assert.assertEquals(i, (int) str.charAt(0));
    }
  }

  @Test
  public void testJsonEncode() {
    Assert.assertEquals("\\\"\\/\\\\h", Strings.encodeJson("\"/\\h"));
    Assert.assertTrue("hello" == Strings.encodeJson("hello"));
  }

  @Test
  public void testUnquoteAndTrim() {
    Assert.assertEquals("", Strings.unquoteAndTrim("  "));
    Assert.assertEquals("hello", Strings.unquoteAndTrim("hello"));
    Assert.assertEquals("hello", Strings.unquoteAndTrim("\"hello\""));
    Assert.assertEquals("hello", Strings.unquoteAndTrim("  hello "));
    Assert.assertEquals("hello", Strings.unquoteAndTrim(" \"hello \""));
    Assert.assertEquals("hello", Strings.unquoteAndTrim("\" hello\" "));
  }

  @Test
  public void uncapitalizetest() {
    Assert.assertEquals(null, Strings.uncapitalize(null));
    Assert.assertEquals("", Strings.uncapitalize(""));
    Assert.assertEquals("hello", Strings.uncapitalize("hello"));
    Assert.assertEquals("hello", Strings.uncapitalize("Hello"));
  }

  @Test
  public void capitalizetest() {
    Assert.assertEquals(null, Strings.capitalize(null));
    Assert.assertEquals("", Strings.capitalize(""));
    Assert.assertEquals("Hello", Strings.capitalize("hello"));
    Assert.assertEquals("Hello", Strings.capitalize("Hello"));
  }

  @Test
  public void isBlankTest() {
    Assert.assertTrue(Strings.isBlank(null));
    Assert.assertTrue(Strings.isBlank(""));
    Assert.assertTrue(Strings.isBlank("   "));
    Assert.assertTrue(Strings.isBlank("\t\n\r"));
    Assert.assertFalse(Strings.isBlank("f"));
    Assert.assertFalse(Strings.isBlank("  f"));
  }

  @Test
  public void msgTest() {
    ResourceBundle rb = ResourceBundle.getBundle("message");
    Assert.assertEquals("world", Strings.msg(rb, "hello"));
    Assert.assertEquals("!!!world!!!", Strings.msg(rb, "world"));
  }

  @Test
  public void toStringTest() {
    Assert.assertEquals("", Strings.toString(null));
    Assert.assertEquals("hello", Strings.toString("hello"));
  }

  @Test
  public void substringTrimedTest() {
    Assert.assertEquals("a big fish", Strings.substringTrimed("a big fish", 0, 10));
    Assert.assertEquals("big fish", Strings.substringTrimed("a big fish", 1, 10));
    Assert.assertEquals("big fish", Strings.substringTrimed("a big fish", 2, 10));
    Assert.assertEquals("big", Strings.substringTrimed("a big fish", 2, 6));
  }

  @Test
  public void isCheckedTest() {
    Assert.assertTrue(Strings.isChecked("true"));
    Assert.assertTrue(Strings.isChecked("TRUE"));
    Assert.assertTrue(Strings.isChecked("True"));

    Assert.assertTrue(Strings.isChecked("on"));
    Assert.assertTrue(Strings.isChecked("On"));

    Assert.assertFalse(Strings.isChecked("false"));
    Assert.assertFalse(Strings.isChecked("off"));
  }

  @Test
  public void getLevenshteinDistanceTest() {
    try {
      Strings.getLevenshteinDistance(null, "");
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      // ok
    }
    try {
      Strings.getLevenshteinDistance("", null);
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      // ok
    }
    Assert.assertEquals(0, Strings.getLevenshteinDistance("", ""));
    Assert.assertEquals(1, Strings.getLevenshteinDistance("", "a"));
    Assert.assertEquals(7, Strings.getLevenshteinDistance("aaapppp", ""));
    Assert.assertEquals(1, Strings.getLevenshteinDistance("frog", "fog"));
    Assert.assertEquals(3, Strings.getLevenshteinDistance("fly", "ant"));
    Assert.assertEquals(7, Strings.getLevenshteinDistance("elephant", "hippo"));
    Assert.assertEquals(7, Strings.getLevenshteinDistance("hippo", "elephant"));
    Assert.assertEquals(8, Strings.getLevenshteinDistance("hippo", "zzzzzzzz"));
    Assert.assertEquals(1, Strings.getLevenshteinDistance("hello", "hallo"));
  }

  @Test
  public void isFileNameValidTest() {
    Assert.assertTrue(Strings.isFileNameValid("hello.txt"));
    Assert.assertFalse(Strings.isFileNameValid("/hello.txt"));
    Assert.assertFalse(Strings.isFileNameValid("\nhello.txt"));
    Assert.assertFalse(Strings.isFileNameValid("\rhello.txt"));
    Assert.assertFalse(Strings.isFileNameValid("\thello.txt"));
    Assert.assertFalse(Strings.isFileNameValid("\0hello.txt"));
    Assert.assertFalse(Strings.isFileNameValid("\fhello.txt"));
    Assert.assertFalse(Strings.isFileNameValid("`hello.txt"));
    Assert.assertFalse(Strings.isFileNameValid("?hello.txt"));
    Assert.assertFalse(Strings.isFileNameValid("*hello.txt"));
    Assert.assertFalse(Strings.isFileNameValid("\\hello.txt"));
    Assert.assertFalse(Strings.isFileNameValid("<hello.txt"));
    Assert.assertFalse(Strings.isFileNameValid(">hello.txt"));
    Assert.assertFalse(Strings.isFileNameValid("|hello.txt"));
    Assert.assertFalse(Strings.isFileNameValid("\"hello.txt"));
    Assert.assertFalse(Strings.isFileNameValid(":hello.txt"));
  }

  @Test
  public void isQuotedTest() {
    Assert.assertTrue(Strings.isQuoted("\"hello\""));
    Assert.assertTrue(Strings.isQuoted("\"\""));
    Assert.assertFalse(Strings.isQuoted("\""));
    Assert.assertFalse(Strings.isQuoted(""));
    Assert.assertFalse(Strings.isQuoted(null));
    Assert.assertFalse(Strings.isQuoted("hello"));
  }

  @Test
  public void iteratorTest() {
    iteratorTest0(Strings.iterator("h", 0, 1));
    iteratorTest0(Strings.iterator("h".toCharArray(), 0, 1));
  }

  private void iteratorTest0(CharIterator chit) {
    Assert.assertTrue(chit.hasNext());
    Assert.assertEquals('h', chit.next());
    Assert.assertFalse(chit.hasNext());
    boolean exthrown = false;
    try {
      chit.next();
      Assert.fail();
    } catch (NoSuchElementException ex) {
      exthrown = true;
    }
    Assert.assertTrue(exthrown);
  }

  @Test
  public void getNbIndexOfTest() {
    Assert.assertEquals(0, Strings.getNbIndexOf("hello", 'x'));
    Assert.assertEquals(1, Strings.getNbIndexOf("hello", 'h'));
    Assert.assertEquals(1, Strings.getNbIndexOf("hello", 'e'));
    Assert.assertEquals(2, Strings.getNbIndexOf("hello", 'l'));
  }

}
