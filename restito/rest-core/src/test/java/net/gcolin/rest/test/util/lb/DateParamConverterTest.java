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

import net.gcolin.rest.util.lb.DateParamConverter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class DateParamConverterTest {

  String dateString = "2015-10-26T05:17:07.000Z";
  Date date;

  /**
   * Initialize test.
   */
  @Before
  public void before() {
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    cal.set(2015, 9, 26, 5, 17, 7);
    cal.set(Calendar.MILLISECOND, 0);
    date = cal.getTime();
  }

  @Test
  public void fromStringTest() {
    Assert.assertNull(new DateParamConverter().fromString(""));
    Assert.assertEquals(date, new DateParamConverter().fromString(dateString));
  }

  @Test
  public void toStringTest() {
    Assert.assertEquals(dateString, new DateParamConverter().toString(date));
  }

}
