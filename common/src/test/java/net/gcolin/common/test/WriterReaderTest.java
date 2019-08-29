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

import net.gcolin.common.io.AsciiEncoder;
import net.gcolin.common.io.Decoder;
import net.gcolin.common.io.Encoder;
import net.gcolin.common.io.FastInputStreamReader;
import net.gcolin.common.io.FastOutputStreamWriter;
import net.gcolin.common.io.Io;
import net.gcolin.common.io.Utf16BeDecoder;
import net.gcolin.common.io.Utf16LeDecoder;
import net.gcolin.common.io.Utf32BeDecoder;
import net.gcolin.common.io.Utf32LeDecoder;
import net.gcolin.common.io.Utf8Decoder;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class WriterReaderTest {

  private String sequence = "hello κόσμε𢟒";

  @Test
  public void testBadCharset() throws IOException {
    try {
      Decoder.createDecoder(null, "kor", null);
      Assert.fail();
    } catch (UnsupportedOperationException ex) {
      // ok
    }

    try {
      Encoder.createEncoder(null, "kor");
      Assert.fail();
    } catch (UnsupportedOperationException ex) {
      // ok
    }

    try {
      Encoder encoder = Encoder.createEncoder(null, StandardCharsets.US_ASCII.name());
      encoder.writeBom();
      Assert.fail();
    } catch (UnsupportedOperationException ex) {
      // ok
    }
  }

  @Test
  public void iso88591Test() throws IOException {
    Charset charset = StandardCharsets.ISO_8859_1;
    test(charset, "hello world");

    try {
      test(charset, sequence);
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      // ok
    }
  }

  @Test
  public void utf8Test() throws IOException {
    Charset charset = StandardCharsets.UTF_8;
    test(charset, sequence);
  }

  @Test
  public void asciiTest() throws IOException {
    Charset charset = StandardCharsets.US_ASCII;
    test(charset, "hello world");

    AsciiEncoder encoder = new AsciiEncoder(new ByteArrayOutputStream());
    try {
      encoder.write('ό');
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      // ok
    }
  }

  @Test
  public void utf16leTest() throws IOException {
    Charset charset = StandardCharsets.UTF_16LE;
    test(charset, sequence);
  }

  @Test
  public void utf16beTest() throws IOException {
    Charset charset = StandardCharsets.UTF_16BE;
    test(charset, sequence);
  }

  @Test
  public void utf16Test() throws IOException {
    Charset charset = StandardCharsets.UTF_16;
    test(charset, sequence);
  }

  @Test
  public void utf32Test() throws IOException {
    Charset charset = Charset.forName("UTF_32");
    test(charset, sequence);
  }

  @Test
  public void utf32leTest() throws IOException {
    Charset charset = Charset.forName("UTF_32LE");
    test(charset, sequence);
  }

  @Test
  public void utf32beTest() throws IOException {
    Charset charset = Charset.forName("UTF_32BE");
    test(charset, sequence);
  }

  @Test
  public void utf8Bom() throws IOException {
    testbom(StandardCharsets.UTF_8, Utf8Decoder.class);
  }

  @Test
  public void utf16beBom() throws IOException {
    testbom(StandardCharsets.UTF_16BE, Utf16BeDecoder.class);
  }

  @Test
  public void utf16leBom() throws IOException {
    testbom(StandardCharsets.UTF_16LE, Utf16LeDecoder.class);
  }

  @Test
  public void utf32leBom() throws IOException {
    testbom(Charset.forName("UTF_32LE"), Utf32LeDecoder.class);
  }

  @Test
  public void utf32beBom() throws IOException {
    testbom(Charset.forName("UTF_32BE"), Utf32BeDecoder.class);
  }

  @Test
  public void detect32beBom() throws IOException {
    detectbom(Charset.forName("UTF_32BE"), Utf32BeDecoder.class);
  }

  @Test
  public void detect32leBom() throws IOException {
    detectbom(Charset.forName("UTF_32LE"), Utf32LeDecoder.class);
  }

  @Test
  public void detect16bleBom() throws IOException {
    detectbom(Charset.forName("UTF_16BE"), Utf16BeDecoder.class);
  }

  @Test
  public void detect16leBom() throws IOException {
    detectbom(Charset.forName("UTF_16LE"), Utf16LeDecoder.class);
  }

  @Test
  public void detect8Bom() throws IOException {
    detectbom(Charset.forName("UTF-8"), Utf8Decoder.class);
  }

  private void detectbom(Charset charset, Class<?> expected) throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    FastOutputStreamWriter fw = (FastOutputStreamWriter) Io.writer(bout, charset.name());
    fw.write("hello");
    fw.close();
    FastInputStreamReader fr =
        (FastInputStreamReader) Io.reader(new ByteArrayInputStream(bout.toByteArray()));
    char[] ch = new char[5];
    Assert.assertEquals(5, fr.read(ch));
    Assert.assertEquals("hello", new String(ch));
    Assert.assertEquals(-1, fr.read());
    Assert.assertTrue(expected.isAssignableFrom(fr.getDecoder().getClass()));
  }

  private void testbom(Charset charset, Class<?> expected) throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    FastOutputStreamWriter fw = (FastOutputStreamWriter) Io.writer(bout, charset.name());
    fw.getEncoder().writeBom();
    fw.write("he");
    fw.write('l');
    fw.write("lo".toCharArray());
    fw.close();
    FastInputStreamReader fr =
        (FastInputStreamReader) Io.reader(new ByteArrayInputStream(bout.toByteArray()));
    char[] ch = new char[5];
    Assert.assertEquals(5, fr.read(ch));
    Assert.assertTrue(expected.isAssignableFrom(fr.getDecoder().getClass()));
    Assert.assertEquals("hello", new String(ch));
    Assert.assertEquals(-1, fr.read());
  }

  private void test(Charset charset, String val) throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();

    Writer writer = Io.writer(bout, charset.name());
    writer.write(val);

    writer.close();

    char[] chjava = new char[50];
    char[] ch = new char[50];
    int count;

    Reader readerjava =
        new InputStreamReader(new ByteArrayInputStream(bout.toByteArray()), charset);
    count = readerjava.read(chjava);
    String strjava = new String(chjava, 0, count);

    Reader reader = new InputStreamReader(new ByteArrayInputStream(bout.toByteArray()), charset);
    count = reader.read(ch);
    String str = new String(ch, 0, count);

    Assert.assertEquals(strjava, str);
  }

}
