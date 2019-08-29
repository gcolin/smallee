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

import net.gcolin.common.io.BufferedOutputStream;
import net.gcolin.common.io.Io;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

public class BufferedOutputStreamTest {

  @Test
  public void testWriteByte() throws IOException {
    test0((data, out) -> {
      for (byte b : data) {
        try {
          out.write(b);
        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
      }
    });
  }

  @Test
  public void testWriteByteArray1024() throws IOException {
    test0((data, out) -> {
      for (int i = 0; i < data.length; i += 1024) {
        try {
          out.write(data, i, Math.min(1024, data.length - i));
        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
      }
    });
  }

  @Test
  public void testWriteByteArray() throws IOException {
    test0((data, out) -> {
      try {
        out.write(data);
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    });
  }

  private void test0(BiConsumer<byte[], OutputStream> consumer) throws IOException {
    byte[] hello = "hello world".getBytes(StandardCharsets.UTF_8);
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    for (int i = 0; i < Io.BUFFER_SIZE; i++) {
      bout.write(hello);
    }

    byte[] data = bout.toByteArray();
    bout = new ByteArrayOutputStream();
    BufferedOutputStream out = new BufferedOutputStream(bout);
    consumer.accept(data, out);
    out.flush();
    Assert.assertArrayEquals(data, bout.toByteArray());
  }

}
