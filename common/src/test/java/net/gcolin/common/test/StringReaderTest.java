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

import net.gcolin.common.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;

public class StringReaderTest {

  @Test
  public void testReadSimple() throws IOException {
    Reader reader = new StringReader("hello");
    Assert.assertEquals('h', reader.read());
    Assert.assertEquals('e', reader.read());
    Assert.assertEquals('l', reader.read());
    Assert.assertEquals('l', reader.read());
    Assert.assertEquals('o', reader.read());
    Assert.assertEquals(-1, reader.read());
    Assert.assertEquals(-1, reader.read());
    reader.close();
  }
  
  @Test
  public void testReadArray() throws IOException {
    Reader reader = new StringReader("hello world");
    char[] buf = new char[10];
    Assert.assertEquals(2, reader.read(buf, 0, 2));
    Assert.assertEquals("he", new String(buf, 0, 2));
    Assert.assertEquals(4, reader.read(buf, 0, 4));
    Assert.assertEquals("llo ", new String(buf, 0, 4));
    Assert.assertEquals(5, reader.read(buf));
    Assert.assertEquals("world", new String(buf, 0, 5));
    Assert.assertEquals(-1, reader.read(buf));
    Assert.assertEquals(-1, reader.read(buf));
    reader.close();
  }
  
}
