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

import net.gcolin.rest.provider.CharacterProvider;

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
public class CharacterProviderTest {

  @Test
  public void getSizeTest() {
    Assert.assertEquals(1, new CharacterProvider().getSize(Character.valueOf('a'), Character.class,
        Character.class, null, MediaType.TEXT_PLAIN_TYPE));

    Assert.assertEquals(-1, new CharacterProvider().getSize(null, Character.class, Character.class,
        null, MediaType.TEXT_PLAIN_TYPE));
  }

  @Test
  public void writeToTest() throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    new CharacterProvider().writeTo('h', Character.class, Character.class, null, null, null, bout);
    Assert.assertEquals("h", new String(bout.toByteArray(), StandardCharsets.UTF_8));

    bout = new ByteArrayOutputStream();
    new CharacterProvider().writeTo(null, Character.class, Character.class, null, null, null, bout);
    Assert.assertEquals(0, bout.size());
  }

  @Test
  public void readFromTest() throws IOException {
    ByteArrayInputStream bout = new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8));
    Assert.assertEquals(Character.valueOf('h'),
        new CharacterProvider().readFrom(Character.class, Character.class, null, null, null, bout));
    bout = new ByteArrayInputStream(new byte[0]);
    Assert.assertNull(
        new CharacterProvider().readFrom(Character.class, Character.class, null, null, null, bout));
  }

}
