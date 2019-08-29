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

package net.gcolin.rest.test.provider;

import net.gcolin.common.io.Io;
import net.gcolin.rest.Logs;
import net.gcolin.rest.provider.FileProvider;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.MultivaluedHashMap;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class FileProviderTest {

  File file = new File("src/test/resources/file.txt");

  @Test
  public void isWriteableTest() {
    Assert.assertTrue(new FileProvider().isWriteable(File.class, File.class, null, null));
    Assert.assertFalse(new FileProvider().isWriteable(String.class, String.class, null, null));
  }

  @Test
  public void getSizeTest() {
    Assert.assertEquals(file.length(),
        new FileProvider().getSize(file, File.class, File.class, null, null));
  }

  @Test
  public void writeToTest() throws IOException {
    MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    new FileProvider().writeTo(file, File.class, File.class, null, null, headers, bout);
    Assert.assertEquals(file.getName(), headers.getFirst("fileName"));
    try (InputStream in = new FileInputStream(file)) {
      Assert.assertArrayEquals(Io.toByteArray(in), bout.toByteArray());
    }
  }

  @Test
  public void readFromTest() throws IOException {
    MultivaluedHashMap<String, String> headers = new MultivaluedHashMap<>();
    headers.add("fileName", "hello.txt");
    File newF = null;
    try (InputStream in = new FileInputStream(file)) {
      newF = new FileProvider().readFrom(File.class, File.class, null, null, headers, in);
    }
    Assert.assertTrue(newF.getName().contains("hello.txt"));
    try (InputStream in = new FileInputStream(file)) {
      try (InputStream in2 = new FileInputStream(newF)) {
        Assert.assertArrayEquals(Io.toByteArray(in), Io.toByteArray(in2));
      }
    }
    if (!newF.delete()) {
      Logs.LOG.info("cannot delete " + newF);
    }
  }

}
