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

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ByteArrayInputStreamTest {

  @Test
  public void testRead1() throws IOException {
    byte[] inb;
    try (ByteArrayOutputStream javaba = new ByteArrayOutputStream()) {
      try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("lipsum.txt")) {
        int read;
        while ((read = in.read()) != -1) {
          javaba.write(read);
        }
      }
      inb = javaba.toByteArray();
    }

    ByteArrayInputStream javain = new ByteArrayInputStream(inb);
    net.gcolin.common.io.ByteArrayInputStream in =
        new net.gcolin.common.io.ByteArrayInputStream(inb);

    int read = javain.read();
    while (read != -1) {
      Assert.assertEquals(read, in.read());
      read = javain.read();      
    }

    in.close();

    javain = new ByteArrayInputStream(inb);
    in = new net.gcolin.common.io.ByteArrayInputStream(inb);

    int c1 = 0;
    int c2;
    byte[] buffer = new byte[189];
    byte[] buffer2 = new byte[189];
    while (c1 != -1) {
      c1 = javain.read(buffer);
      c2 = in.read(buffer2);
      Assert.assertEquals(c1, c2);
      Assert.assertArrayEquals(buffer, buffer2);
    }

    in.close();

    javain = new ByteArrayInputStream(inb);
    in = new net.gcolin.common.io.ByteArrayInputStream(inb);

    c1 = 0;
    buffer = new byte[5000];
    buffer2 = new byte[5000];
    while (c1 != -1) {
      c1 = javain.read(buffer);
      c2 = in.read(buffer2);
      Assert.assertEquals(c1, c2);
      Assert.assertArrayEquals(buffer, buffer2);
    }

    in.close();
  }

}
