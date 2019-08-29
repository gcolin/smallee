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
import static org.junit.Assert.assertTrue;

import net.gcolin.rest.util.lb.NewCookieParamConverter;

import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

import javax.ws.rs.core.NewCookie;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class NewCookieParamConverterTest {

  private String cookie = "name=value; Comment=\"hello\"; Path=/; Domain=hello.local; Version=2; "
      + "Max-Age=10000; Secure=true; HttpOnly=true; Expires=Tue, 10 Nov 2015 16:09:15 GMT";

  @Test
  public void fromStringTest() {
    NewCookie cc = new NewCookieParamConverter().fromString(cookie);
    assertEquals("name", cc.getName());
    assertEquals("value", cc.getValue());
    assertEquals("hello", cc.getComment());
    assertEquals("/", cc.getPath());
    assertEquals("hello.local", cc.getDomain());
    assertEquals(2, cc.getVersion());
    assertEquals(10000, cc.getMaxAge());
    assertTrue(cc.isSecure());
    assertTrue(cc.isHttpOnly());
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    cal.set(2015, 10, 10, 16, 9, 15);
    cal.set(Calendar.MILLISECOND, 0);
    assertEquals(cal.getTime(), cc.getExpiry());

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
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    cal.set(2015, 10, 10, 16, 9, 15);
    cal.set(Calendar.MILLISECOND, 0);
    assertEquals(cookie, new NewCookieParamConverter().toString(new NewCookie("name", "value", "/",
        "hello.local", 2, "hello", 10000, cal.getTime(), true, true)));
  }

}
