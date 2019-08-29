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

import net.gcolin.rest.provider.NumberProvider;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class NumberProviderTest {

  @Test
  public void writeToTest() throws IOException {
    writeToTest0(BigDecimal.TEN);
    writeToTest0(15.3);
  }

  private void writeToTest0(Number nb) throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    new NumberProvider().writeTo(nb, Number.class, Number.class, null, null, null, bout);
    Assert.assertEquals(nb.toString(), new String(bout.toByteArray(), StandardCharsets.UTF_8));
  }

  @Test
  public void readFromTest() throws IOException {
    readFromTest0(new BigDecimal(-10));
    readFromTest0(19.3);
  }

  private void readFromTest0(Number nb) throws IOException {
    ByteArrayInputStream bout =
        new ByteArrayInputStream(nb.toString().getBytes(StandardCharsets.UTF_8));
    Assert.assertEquals(new BigDecimal(nb.toString()),
        new NumberProvider().readFrom(Number.class, Number.class, null, null, null, bout));
  }

}
