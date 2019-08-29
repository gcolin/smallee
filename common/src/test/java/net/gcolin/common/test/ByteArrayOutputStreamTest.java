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

import net.gcolin.common.Time;
import net.gcolin.common.io.Io;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ByteArrayOutputStreamTest {

  @Test
  public void testWrite1() throws IOException {
    try (ByteArrayOutputStream javaba = new ByteArrayOutputStream()) {
      try (net.gcolin.common.io.ByteArrayOutputStream ba =
          new net.gcolin.common.io.ByteArrayOutputStream()) {
        Assert.assertTrue(ba.isEmpty());
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("lipsum.txt")) {
          int read;
          while ((read = in.read()) != -1) {
            javaba.write(read);
            ba.write(read);
          }
        }
        Assert.assertFalse(ba.isEmpty());
        Assert.assertArrayEquals(javaba.toByteArray(), ba.toByteArray());
      }
    }
  }

  @Test
  public void testWriteTo() throws IOException {
    testWrite0(small());
    testWrite0(big());
  }

  private void testWrite0(byte[] data) throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    try (net.gcolin.common.io.ByteArrayOutputStream ba =
        new net.gcolin.common.io.ByteArrayOutputStream()) {
      ba.write(data);
      ba.writeTo(bout);
    }
    Assert.assertArrayEquals(data, bout.toByteArray());
  }

  @Test
  public void testReset() throws IOException {
    testReset0(small());
    testReset0(big());
  }

  private byte[] small() {
    return "hello  !".getBytes(StandardCharsets.UTF_8);
  }

  private byte[] big() throws IOException {
    byte[] bt = small();
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    for (int i = 0; i < Io.BUFFER_SIZE; i++) {
      bout.write(bt);
    }
    return bout.toByteArray();
  }

  private void testReset0(byte[] bt) throws IOException {
    net.gcolin.common.io.ByteArrayOutputStream ba =
        new net.gcolin.common.io.ByteArrayOutputStream();
    ba.write(bt);
    Assert.assertArrayEquals(bt, ba.toByteArray());
    Assert.assertFalse(ba.isEmpty());
    ba.reset();
    Assert.assertTrue(ba.isEmpty());
    ba.write(bt);
    Assert.assertArrayEquals(bt, ba.toByteArray());
    Assert.assertFalse(ba.isEmpty());
    ba.release();
    ba.close();
  }

  @Test
  @Ignore
  public void testSpeed1() throws IOException {
    byte[] inb;
    try (ByteArrayOutputStream javaba = new ByteArrayOutputStream()) {
      try (net.gcolin.common.io.ByteArrayOutputStream ba =
          new net.gcolin.common.io.ByteArrayOutputStream()) {
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("lipsum.txt")) {
          int read;
          while ((read = in.read()) != -1) {
            javaba.write(read);
            ba.write(read);
          }
        }
        inb = javaba.toByteArray();
        Assert.assertNotNull(ba.toByteArray());
      }
    }

    int loop = 100;
    Time.tick();
    for (int i = 0; i < loop; i++) {
      try (ByteArrayOutputStream javaba = new ByteArrayOutputStream()) {
        try (InputStream in = new ByteArrayInputStream(inb)) {
          int read;
          while ((read = in.read()) != -1) {
            javaba.write(read);
          }
        }
        javaba.toByteArray();
      }
    }
    Time.tock("ByteArrayOutputStream jdk");

    Time.tick();
    for (int i = 0; i < loop; i++) {
      try (net.gcolin.common.io.ByteArrayOutputStream ba =
          new net.gcolin.common.io.ByteArrayOutputStream()) {
        try (InputStream in = new ByteArrayInputStream(inb)) {
          int read;
          while ((read = in.read()) != -1) {
            ba.write(read);
          }
        }
        ba.toByteArray();
      }
    }
    Time.tock("ByteArrayOutputStream fast");

  }

  @Test
  @Ignore
  public void testSpeed2() throws IOException {
    byte[] inb = null;
    try (ByteArrayOutputStream javaba = new ByteArrayOutputStream()) {
      try (net.gcolin.common.io.ByteArrayOutputStream ba =
          new net.gcolin.common.io.ByteArrayOutputStream()) {
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("lipsum.txt")) {
          int read;
          while ((read = in.read()) != -1) {
            javaba.write(read);
            ba.write(read);
          }
        }
        inb = javaba.toByteArray();
        Assert.assertNotNull(ba.toByteArray());
      }
    }

    int loop = 100;
    byte[] buffer = new byte[1024];
    Time.tick();
    for (int i = 0; i < loop; i++) {
      try (ByteArrayOutputStream javaba = new ByteArrayOutputStream()) {
        try (InputStream in = new ByteArrayInputStream(inb)) {
          int ch;
          while ((ch = in.read(buffer)) != -1) {
            javaba.write(buffer, 0, ch);
          }
        }
        javaba.toByteArray();
      }
    }
    Time.tock("ByteArrayOutputStream jdk buffer");

    Time.tick();
    for (int i = 0; i < loop; i++) {
      try (net.gcolin.common.io.ByteArrayOutputStream ba =
          new net.gcolin.common.io.ByteArrayOutputStream()) {
        try (InputStream in = new ByteArrayInputStream(inb)) {
          int ch;
          while ((ch = in.read(buffer)) != -1) {
            ba.write(buffer, 0, ch);
          }
        }
        Assert.assertNotNull(ba.toByteArray());
      }
    }
    Time.tock("ByteArrayOutputStream fast buffer");

  }

  @Test
  public void testWrite2() throws IOException {
    try (ByteArrayOutputStream javaba = new ByteArrayOutputStream()) {
      try (net.gcolin.common.io.ByteArrayOutputStream ba =
          new net.gcolin.common.io.ByteArrayOutputStream()) {
        byte[] buffer = new byte[159];
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("lipsum.txt")) {
          int ch;
          while ((ch = in.read(buffer)) != -1) {
            javaba.write(buffer, 0, ch);
            ba.write(buffer, 0, ch);
          }
        }

        Assert.assertArrayEquals(javaba.toByteArray(), ba.toByteArray());
      }
    }
  }

  @Test
  public void testWrite3() throws IOException {
    try (ByteArrayOutputStream javaba = new ByteArrayOutputStream()) {
      try (net.gcolin.common.io.ByteArrayOutputStream ba =
          new net.gcolin.common.io.ByteArrayOutputStream()) {
        byte[] buffer = new byte[6000];
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("lipsum.txt")) {
          int ch;
          while ((ch = in.read(buffer)) != -1) {
            javaba.write(buffer, 0, ch);
            ba.write(buffer, 0, ch);
          }
        }

        Assert.assertArrayEquals(javaba.toByteArray(), ba.toByteArray());
      }
    }
  }

}
