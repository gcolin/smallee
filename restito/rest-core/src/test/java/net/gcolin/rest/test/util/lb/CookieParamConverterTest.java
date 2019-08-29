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

package net.gcolin.rest.test.util.lb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import net.gcolin.rest.util.lb.CookieParamConverter;
import net.gcolin.rest.util.lb.NewCookieParamConverter;

import org.junit.Test;

import javax.ws.rs.core.Cookie;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class CookieParamConverterTest {

  private String cookie = "name=value; Path=/; Domain=hello.local; Version=2";

  @Test
  public void fromStringTest() {
    Cookie cc = new CookieParamConverter().fromString(cookie);
    assertEquals("name", cc.getName());
    assertEquals("value", cc.getValue());
    assertEquals("/", cc.getPath());
    assertEquals("hello.local", cc.getDomain());
    assertEquals(2, cc.getVersion());

    assertNull(new NewCookieParamConverter().fromString(""));

    cc = new NewCookieParamConverter().fromString("a=hello");
    assertEquals("a", cc.getName());
    assertEquals("hello", cc.getValue());

    try {
      new NewCookieParamConverter().fromString("hello");
    } catch (IllegalArgumentException ex) {
      // ok
    }

    new NewCookieParamConverter().fromString("a=hello; p=2");
  }

  @Test
  public void toStringTest() {
    assertEquals(cookie,
        new CookieParamConverter().toString(new Cookie("name", "value", "/", "hello.local", 2)));
  }

}
