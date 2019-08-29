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

import net.gcolin.common.io.FastInputStreamReader;
import net.gcolin.common.io.Io;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class FastInputstreamReaderTest {

  private static String lipsum;
  private static final boolean speedTest = false;

  /**
   * Load lipsum.
   */
  @BeforeClass
  public static void beforeAll() {
    try {
      lipsum = new String(
          Io.toByteArray(
              FastInputstreamReaderTest.class.getClassLoader().getResourceAsStream("lipsum.txt")),
          StandardCharsets.UTF_8);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Test
  public void testClose() throws IOException {
    FastInputStreamReader reader = new FastInputStreamReader(null);
    reader.close();
  }

  @Test
  public void testReadOneByOne() throws IOException {
    FastInputStreamReader fr = new FastInputStreamReader(
        new net.gcolin.common.io.ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)));
    StringBuilder str = new StringBuilder();
    int nb;
    while ((nb = fr.read()) != -1) {
      str.append((char) nb);
    }
    Assert.assertEquals("hello", str.toString());
    fr.close();
  }

  @Test
  public void testAscii() throws IOException {
    test(StandardCharsets.US_ASCII);
  }

  @Test
  public void testUtf8() throws IOException {
    test(StandardCharsets.UTF_8);
  }

  @Test
  public void testIso8859_1() throws IOException {
    test(StandardCharsets.ISO_8859_1);
  }

  @Test
  public void testUtf16Be() throws IOException {
    test(StandardCharsets.UTF_16BE);
  }

  @Test
  public void testUtf16Le() throws IOException {
    test(StandardCharsets.UTF_16LE);
  }

  @Test
  public void testUtf32Be() throws IOException {
    test(Charset.forName("UTF_32"));
  }

  @Test
  public void testUtf32Le() throws IOException {
    test(Charset.forName("UTF_32LE"));
  }

  private void test(Charset charset) throws IOException {
    test(charset, 10, speedTest ? 100000 : 1);
    test(charset, 100, speedTest ? 100000 : 1);
    test(charset, 1000, speedTest ? 100000 : 1);
    test(charset, 10000, speedTest ? 10000 : 1);
    test(charset, 100000, speedTest ? 1000 : 1);
    test(charset, 1000000, speedTest ? 100 : 1);
  }

  private void test(Charset charset, int size, int loop) throws IOException {
    StringBuffer str = new StringBuffer();
    while (str.length() < size) {
      str.append(lipsum.substring(0, Math.min(lipsum.length(), size - str.length())));
    }
    String string = str.toString();
    byte[] src = string.getBytes(charset);
    try (InputStreamReader r = new InputStreamReader(new ByteArrayInputStream(src), charset)) {
      Assert.assertEquals(string, Io.toString(r));
    }
    try (FastInputStreamReader r =
        new FastInputStreamReader(new ByteArrayInputStream(src), charset.name())) {
      Assert.assertEquals(string, Io.toString(r));
    }
    StringBuilder result = new StringBuilder();
    result.append(
        "speed test for " + charset.name() + " with size " + size + " in " + loop + " loop\n");
    long time = System.currentTimeMillis();
    for (int i = 0; i < loop; i++) {
      try (InputStreamReader r = new InputStreamReader(new ByteArrayInputStream(src), charset)) {
        Assert.assertEquals(string, Io.toString(r));
      }
    }
    time = System.currentTimeMillis() - time;
    result.append("InputStreamReader " + time + " ms\n");
    time = System.currentTimeMillis();
    for (int i = 0; i < loop; i++) {
      try (FastInputStreamReader r =
          new FastInputStreamReader(new ByteArrayInputStream(src), charset.name())) {
        Assert.assertEquals(string, Io.toString(r));
      }
    }
    time = System.currentTimeMillis() - time;
    result.append("FastInputStreamReader " + time + " ms\n");
    Logger.getLogger(FastInputstreamReaderTest.class.getName()).info(result.toString());
  }
}
