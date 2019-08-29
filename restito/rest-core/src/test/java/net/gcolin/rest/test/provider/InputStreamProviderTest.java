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
import net.gcolin.rest.provider.InputStreamProvider;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class InputStreamProviderTest {

  @Test
  public void writeToTest() throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    new InputStreamProvider().writeTo(
        new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)), InputStream.class,
        InputStream.class, null, null, null, bout);
    Assert.assertEquals("hello", new String(bout.toByteArray(), StandardCharsets.UTF_8));
  }

  @Test
  public void readFromTest() throws IOException {
    InputStream in = new InputStreamProvider().readFrom(InputStream.class, InputStream.class, null,
        null, null, new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)));
    Assert.assertEquals("hello", Io.toString(in));
  }

}
