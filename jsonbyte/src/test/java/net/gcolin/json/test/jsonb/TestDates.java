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

package net.gcolin.json.test.jsonb;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * Dates tests.
 * 
 * @author Gaël COLIN
 */
public class TestDates extends AbstractMultiCharsetTest {

  public static class Obj {
    Date date;
    Calendar calendar;
    GregorianCalendar gregorianCalendar;
    TimeZone timezone;
    SimpleTimeZone simpleTimeZone;
    Instant instant;
    Duration duration;
    Period period;
    LocalDate localDate;
    LocalTime localTime;
    LocalDateTime localDateTime;
    ZonedDateTime zonedDateTime;
    ZoneId zoneid;
    ZoneOffset zoneOffset;
    OffsetDateTime offsetDateTime;
    OffsetTime offsetTime;
  }


  @Test
  public void testDate() {
    Obj obj = new Obj();
    obj.date = new Date();
    Obj o2 = test0(Obj.class, obj, "{\"date\":\"" + DateTimeFormatter.ISO_DATE_TIME
        .format(ZonedDateTime.ofInstant(obj.date.toInstant(), ZoneOffset.UTC)) + "\"}");
    Assert.assertEquals(obj.date, o2.date);
  }

  @Test
  public void testCalendar() {
    Obj obj = new Obj();
    obj.calendar = Calendar.getInstance();
    Obj o2 = test0(Obj.class, obj, "{\"calendar\":\"" + DateTimeFormatter.ISO_DATE_TIME
        .format(ZonedDateTime.ofInstant(obj.calendar.toInstant(), ZoneOffset.UTC)) + "\"}");
    Assert.assertEquals(obj.calendar, o2.calendar);
  }

  @Test
  public void testGregorianCalendar() {
    Obj obj = new Obj();
    obj.gregorianCalendar = new GregorianCalendar();
    Obj o2 = test0(Obj.class, obj,
        "{\"gregorianCalendar\":\""
            + DateTimeFormatter.ISO_DATE_TIME
                .format(ZonedDateTime.ofInstant(obj.gregorianCalendar.toInstant(), ZoneOffset.UTC))
            + "\"}");
    Assert.assertEquals(obj.gregorianCalendar, o2.gregorianCalendar);
  }

  @Test
  public void testTimeZone() {
    Obj obj = new Obj();
    obj.timezone = TimeZone.getTimeZone("UTC");
    Obj o2 = test0(Obj.class, obj, "{\"timezone\":\"UTC\"}");
    Assert.assertEquals(obj.timezone, o2.timezone);
  }

  @Test
  public void testSimpleTimeZone() {
    Obj obj = new Obj();
    TimeZone tz = TimeZone.getTimeZone("UTC");
    obj.simpleTimeZone = new SimpleTimeZone(tz.getRawOffset(), tz.getID());
    Obj o2 = test0(Obj.class, obj, "{\"simpleTimeZone\":\"UTC\"}");
    Assert.assertEquals(obj.simpleTimeZone, o2.simpleTimeZone);
  }

  @Test
  public void testDuration() {
    Obj obj = new Obj();
    obj.duration = Duration.of(5, ChronoUnit.MINUTES);
    Obj o2 = test0(Obj.class, obj, "{\"duration\":\"PT5M\"}");
    Assert.assertEquals(obj.duration, o2.duration);
  }

  @Test
  public void testPeriod() {
    Obj obj = new Obj();
    obj.period = Period.ofDays(2);
    Obj o2 = test0(Obj.class, obj, "{\"period\":\"P2D\"}");
    Assert.assertEquals(obj.period, o2.period);
  }

  @Test
  public void testInstant() {
    Obj obj = new Obj();
    obj.instant = Instant.now();
    Obj o2 = test0(Obj.class, obj,
        "{\"instant\":\"" + DateTimeFormatter.ISO_INSTANT.format(obj.instant) + "\"}");
    Assert.assertEquals(obj.instant, o2.instant);
  }

  @Test
  public void testLocalDate() {
    Obj obj = new Obj();
    obj.localDate = LocalDate.now();
    Obj o2 = test0(Obj.class, obj,
        "{\"localDate\":\"" + DateTimeFormatter.ISO_LOCAL_DATE.format(obj.localDate) + "\"}");
    Assert.assertEquals(obj.localDate, o2.localDate);
  }

  @Test
  public void testLocalTime() {
    Obj obj = new Obj();
    obj.localTime = LocalTime.now();
    Obj o2 = test0(Obj.class, obj,
        "{\"localTime\":\"" + DateTimeFormatter.ISO_LOCAL_TIME.format(obj.localTime) + "\"}");
    Assert.assertEquals(obj.localTime, o2.localTime);
  }

  @Test
  public void testLocalDateTime() {
    Obj obj = new Obj();
    obj.localDateTime = LocalDateTime.now();
    Obj o2 = test0(Obj.class, obj, "{\"localDateTime\":\""
        + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(obj.localDateTime) + "\"}");
    Assert.assertEquals(obj.localDateTime, o2.localDateTime);
  }

  @Test
  public void testZonedDateTime() {
    Obj obj = new Obj();
    obj.zonedDateTime = ZonedDateTime.now();
    Obj o2 = test0(Obj.class, obj, "{\"zonedDateTime\":\""
        + DateTimeFormatter.ISO_ZONED_DATE_TIME.format(obj.zonedDateTime) + "\"}");
    Assert.assertEquals(obj.zonedDateTime, o2.zonedDateTime);
  }

  @Test
  public void testZoneId() {
    Obj obj = new Obj();
    obj.zoneid = ZoneId.of("GMT+2");
    Obj o2 = test0(Obj.class, obj, "{\"zoneid\":\"GMT+02:00\"}");
    Assert.assertEquals(obj.zoneid, o2.zoneid);
  }

  @Test
  public void testZoneOffset() {
    Obj obj = new Obj();
    obj.zoneOffset = ZoneOffset.UTC;
    Obj o2 = test0(Obj.class, obj, "{\"zoneOffset\":\"Z\"}");
    Assert.assertEquals(obj.zoneOffset, o2.zoneOffset);
  }

  @Test
  public void testOffsetDateTime() {
    Obj obj = new Obj();
    obj.offsetDateTime = OffsetDateTime.now();
    Obj o2 = test0(Obj.class, obj, "{\"offsetDateTime\":\""
        + DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(obj.offsetDateTime) + "\"}");
    Assert.assertEquals(obj.offsetDateTime, o2.offsetDateTime);
  }

  @Test
  public void testOffsetTime() {
    Obj obj = new Obj();
    obj.offsetTime = OffsetTime.now();
    Obj o2 = test0(Obj.class, obj,
        "{\"offsetTime\":\"" + DateTimeFormatter.ISO_OFFSET_TIME.format(obj.offsetTime) + "\"}");
    Assert.assertEquals(obj.offsetTime, o2.offsetTime);
  }
}
