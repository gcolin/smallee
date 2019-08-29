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

import net.gcolin.common.io.Io;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class Utf8Test {

  @Test
  public void test() throws IOException {
    char[] cbuf = new char[1000];
    int ch = 0;
    try (Reader reader =
        Io.reader(Utf8Test.class.getClassLoader().getResourceAsStream("specialChar.txt"))) {
      ch = reader.read(cbuf);
    }

    String read = new String(cbuf, 0, ch);
    read = read.substring(read.lastIndexOf("-->") + 3);
    Assert.assertEquals("Presentación Contáctenos 1#\\\"'@()?¿{}*[]+-%=rRñÑâÂ齉한سؤالθЩ𐍈", read);

    String str = "Presentación Contáctenos 1#\\\"'@()?¿{}*[]+-%=rRñÑâÂ齉한سؤالθЩ𐍈";
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    try (Writer writer = Io.writer(bout, "utf8")) {
      writer.write(str);
    }

    Reader reader =
        new InputStreamReader(new ByteArrayInputStream(bout.toByteArray()), StandardCharsets.UTF_8);

    Assert.assertEquals(str, Io.toString(reader));
  }

}
