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

import net.gcolin.rest.provider.BooleanProvider;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.MediaType;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class BooleanProviderTest {

  @Test
  public void getSizeTest() {
    Assert.assertEquals(-1, new BooleanProvider().getSize(true, Boolean.class, Boolean.class, null,
        MediaType.TEXT_PLAIN_TYPE));
  }

  @Test
  public void writeToTest() throws IOException {
    writeToTest0(true);
    writeToTest0(false);
  }

  private void writeToTest0(boolean val) throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    new BooleanProvider().writeTo(val, Boolean.class, Boolean.class, null, null, null, bout);
    Assert.assertEquals(String.valueOf(val),
        new String(bout.toByteArray(), StandardCharsets.UTF_8));
  }

  @Test
  public void readFromTest() throws IOException {
    readFromTest0(true);
    readFromTest0(false);
  }

  private void readFromTest0(Boolean val) throws IOException {
    ByteArrayInputStream bout =
        new ByteArrayInputStream(String.valueOf(val).getBytes(StandardCharsets.UTF_8));
    Assert.assertEquals(val, new BooleanProvider()
        .readFrom(Boolean.class, Boolean.class, null, null, null, bout));
  }

}
