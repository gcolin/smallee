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

import net.gcolin.common.lang.JsonDateFormat;

import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class JsonDateFormatTest {

  @Test
  public void testSimple() throws ParseException {
    JsonDateFormat df = new JsonDateFormat();
    Date now = new Date();
    String strnow = df.format(now);
    Date d2 = df.parse(strnow);
    Assert.assertEquals(now, d2);
  }

  @Test
  public void testSimple2() throws ParseException {
    JsonDateFormat df = new JsonDateFormat();
    Date d2 = df.parse("2012-01-10");
    Calendar cal = Calendar.getInstance();
    cal.setTimeZone(TimeZone.getTimeZone("GMT"));
    cal.setTime(d2);
    Assert.assertEquals(2012, cal.get(Calendar.YEAR));
    Assert.assertEquals(0, cal.get(Calendar.MONTH));
    Assert.assertEquals(10, cal.get(Calendar.DAY_OF_MONTH));
  }

  @Test
  public void testError() throws ParseException {
    try {
      Assert.assertNull(new JsonDateFormat().parse("qwerty"));
      Assert.fail();
    } catch (ParseException ex) {
      Assert.assertEquals(0, ex.getErrorOffset());
    }
  }

}
