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

import net.gcolin.common.lang.CharIterator;
import net.gcolin.common.lang.Header;
import net.gcolin.common.lang.Headers;
import net.gcolin.common.lang.Strings;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class StringsTest {

  @Test
  public void testisIp() {
    Assert.assertFalse(Strings.isIp("168.0.1"));
    Assert.assertTrue(Strings.isIp("192" + ".168.0.1"));
    Assert.assertTrue(Strings.isIp("2001:0db8:0000" + "85a3:0000:0000:ac1f:8001"));
  }

  @Test
  public void testUrlEncode() throws ScriptException {

    for (String phrase : new String[] {"hello%𐍈€¢$", "1#\"'@()?¿{}*[]+-%=rRñÑâÂ齉한سؤالθЩ𐍈"}) {
      String encoded = Strings.encodeUrl(phrase);

      ScriptEngineManager factory = new ScriptEngineManager();
      ScriptEngine engine = factory.getEngineByName("javascript");

      String jsencoded =
          (String) engine.eval("encodeURIComponent('" + phrase.replace("'", "\\'") + "')");
      String decoded = Strings.decodeUrl(encoded);
      String jsdecoded = Strings.decodeUrl(jsencoded);

      Assert.assertEquals(phrase, decoded);
      Assert.assertEquals(phrase, jsdecoded);
    }

    Assert.assertEquals("a\r\n", Strings.decodeUrl("a%0D%0A"));

    for (int i = 1; i < 256; i++) {
      String str = Strings.decodeUrl(Strings.encodeUrl(String.valueOf((char) i)));
      Assert.assertEquals(1, str.length());
      Assert.assertEquals(i, (int) str.charAt(0));
    }

    String str = "D%8f%ce%82%cd%a3%fe%fc%f2p%2f%8cM%26%1b%10%98%b6%e3%d2";
    Strings.decodeUrl(str);

    try {
      Strings.decodeUrl("%");
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      // ok
    }

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
  public void getHeaderParametersTest() {
    Map<String, String> headers = Headers.getParameters("hello");
    Assert.assertEquals(0, headers.size());

    headers = Headers.getParameters("hello;name=value");
    Assert.assertEquals(1, headers.size());
    Assert.assertEquals("value", headers.get("name"));

    headers = Headers.getParameters("hello;name=\"value\"");
    Assert.assertEquals(1, headers.size());
    Assert.assertEquals("value", headers.get("name"));
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
  public void testHeaders() {
    List<Header> list = Headers.parse("fr,en,es");
    Assert.assertEquals(3, list.size());
    Assert.assertEquals("fr", list.get(0).getValue());
    Assert.assertEquals("en", list.get(1).getValue());
    Assert.assertEquals("es", list.get(2).getValue());

    list = Headers.parse("en;q=0.9,fr,es;q=0.8;p=q");
    Assert.assertEquals(3, list.size());
    Assert.assertEquals("fr", list.get(0).getValue());
    Assert.assertEquals("en", list.get(1).getValue());
    Assert.assertEquals("es", list.get(2).getValue());

    list = Headers.parse(" fr , en  ,  es ");
    Assert.assertEquals(3, list.size());
    Assert.assertEquals("fr", list.get(0).getValue());
    Assert.assertEquals("en", list.get(1).getValue());
    Assert.assertEquals("es", list.get(2).getValue());

    list = Headers.parse("fr,en;q=0.9,es;q=0.7");
    Assert.assertEquals(3, list.size());
    Assert.assertEquals("fr", list.get(0).getValue());
    Assert.assertEquals("en", list.get(1).getValue());
    Assert.assertEquals("es", list.get(2).getValue());

    Assert.assertEquals(1.0f, list.get(0).getSort(), 0.1f);
    Assert.assertEquals(0.9f, list.get(1).getSort(), 0.1f);
    Assert.assertEquals(0.7f, list.get(2).getSort(), 0.1f);

    list = Headers.parse("fr;w=hello,en;p=world;a b=pop");
    Assert.assertEquals(2, list.size());
    Assert.assertEquals("hello", list.get(0).getParameters().get("w"));
    Assert.assertEquals("world", list.get(1).getParameters().get("p"));
    Assert.assertEquals("pop", list.get(1).getParameters().get("a b"));
  }

  @Test
  public void getNbIndexOfTest() {
    Assert.assertEquals(0, Strings.getNbIndexOf("hello", 'x'));
    Assert.assertEquals(1, Strings.getNbIndexOf("hello", 'h'));
    Assert.assertEquals(1, Strings.getNbIndexOf("hello", 'e'));
    Assert.assertEquals(2, Strings.getNbIndexOf("hello", 'l'));
  }

}
