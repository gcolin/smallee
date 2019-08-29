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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

public class StringWriterTest {

  @Test
  public void testWrite1() throws IOException {
    try (StringWriter javaba = new StringWriter()) {
      try (net.gcolin.common.io.StringWriter ba = new net.gcolin.common.io.StringWriter()) {
        Assert.assertTrue(ba.isEmpty());
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("lipsum.txt")) {
          int read;
          while ((read = in.read()) != -1) {
            javaba.write(read);
            ba.write(read);
          }
        }
        Assert.assertFalse(ba.isEmpty());
        Assert.assertEquals(javaba.toString(), ba.toString());
      }
    }
  }

  @Test
  public void testWriteTo() throws IOException {
    testWrite0(small());
    testWrite0(big());
  }

  private void testWrite0(String str) throws IOException {
    StringWriter bout = new StringWriter();
    try (net.gcolin.common.io.StringWriter ba = new net.gcolin.common.io.StringWriter()) {
      ba.write(str);
      ba.writeTo(bout);
    }
    Assert.assertEquals(str, bout.toString());
  }

  private String small() {
    return "hello  !";
  }

  private String big() throws IOException {
    String str = small();
    StringWriter bout = new StringWriter();
    for (int i = 0; i < Io.BUFFER_SIZE; i++) {
      bout.write(str);
    }
    return bout.toString();
  }

  @Test
  @Ignore
  public void testSpeed1() throws IOException {
    String inb = null;
    try (StringWriter javaba = new StringWriter()) {
      try (net.gcolin.common.io.StringWriter ba = new net.gcolin.common.io.StringWriter()) {
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("lipsum.txt")) {
          int read;
          while ((read = in.read()) != -1) {
            javaba.write(read);
            ba.write(read);
          }
        }
        inb = javaba.toString();
        ba.toString();
      }
    }

    int loop = 100;
    Time.tick();
    for (int i = 0; i < loop; i++) {
      try (StringWriter javaba = new StringWriter()) {
        try (StringReader in = new StringReader(inb)) {
          int read;
          while ((read = in.read()) != -1) {
            javaba.write(read);
          }
        }
        javaba.toString();
      }
    }
    Time.tock("StringWriter jdk");

    Time.tick();
    for (int i = 0; i < loop; i++) {
      try (net.gcolin.common.io.StringWriter ba = new net.gcolin.common.io.StringWriter()) {
        try (StringReader in = new StringReader(inb)) {
          int read;
          while ((read = in.read()) != -1) {
            ba.write(read);
          }
        }
        ba.toString();
      }
    }
    Time.tock("StringWriter fast");

  }

  @Test
  @Ignore
  public void testSpeed2() throws IOException {
    String inb = null;
    try (StringWriter javaba = new StringWriter()) {
      try (net.gcolin.common.io.StringWriter ba = new net.gcolin.common.io.StringWriter()) {
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("lipsum.txt")) {
          int read;
          while ((read = in.read()) != -1) {
            javaba.write(read);
            ba.write(read);
          }
        }
        inb = javaba.toString();
        ba.toString();
      }
    }

    int loop = 100;
    char[] buffer = new char[1024];
    Time.tick();
    for (int i = 0; i < loop; i++) {
      try (StringWriter javaba = new StringWriter()) {
        try (Reader in = new StringReader(inb)) {
          int ch;
          while ((ch = in.read(buffer)) != -1) {
            javaba.write(buffer, 0, ch);
          }
        }
        javaba.toString();
      }
    }
    Time.tock("StringWriter jdk buffer");

    Time.tick();
    for (int i = 0; i < loop; i++) {
      try (net.gcolin.common.io.StringWriter ba = new net.gcolin.common.io.StringWriter()) {
        try (Reader in = new StringReader(inb)) {
          int ch;
          while ((ch = in.read(buffer)) != -1) {
            ba.write(buffer, 0, ch);
          }
        }
        ba.toString();
      }
    }
    Time.tock("StringWriter fast buffer");

  }

  @Test
  public void testWrite2() throws IOException {
    try (StringWriter javaba = new StringWriter()) {
      try (net.gcolin.common.io.StringWriter ba = new net.gcolin.common.io.StringWriter()) {
        char[] buffer = new char[159];
        try (Reader in =
            Io.reader(this.getClass().getClassLoader().getResourceAsStream("lipsum.txt"))) {
          int ch;
          while ((ch = in.read(buffer)) != -1) {
            javaba.write(buffer, 0, ch);
            ba.write(buffer, 0, ch);
          }
        }

        Assert.assertEquals(javaba.toString(), ba.toString());
      }
    }
  }

  @Test
  public void testWrite3() throws IOException {
    try (StringWriter javaba = new StringWriter()) {
      try (net.gcolin.common.io.StringWriter ba = new net.gcolin.common.io.StringWriter()) {
        char[] buffer = new char[6000];
        try (Reader in =
            Io.reader(this.getClass().getClassLoader().getResourceAsStream("lipsum.txt"))) {
          int ch;
          while ((ch = in.read(buffer)) != -1) {
            javaba.write(buffer, 0, ch);
            ba.write(buffer, 0, ch);
          }
        }

        Assert.assertEquals(javaba.toString(), ba.toString());
      }
    }
  }

}
