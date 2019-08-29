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

package net.gcolin.rest.util.lb;

import net.gcolin.common.lang.Strings;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Converter String RFC_1123_DATE_TIME to Date.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class DateHeaderParamConverter implements Converter<Date> {

  private static final ZoneId ZONE = ZoneId.of("GMT");
  private static final DateTimeFormatter[] FORMATTERS = {DateTimeFormatter.RFC_1123_DATE_TIME,
      DateTimeFormatter.ofPattern("EEEE, dd-MMM-yy HH:mm:ss zzz"),
      DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy"),
      DateTimeFormatter.ofPattern("EEE, dd-MMM-yyyy HH:mm:ss z")};

  @Override
  public String toString(Date date) {
    return DateTimeFormatter.RFC_1123_DATE_TIME
        .format(ZonedDateTime.ofInstant(date.toInstant(), ZONE));
  }

  @Override
  public Date fromString(String str) {
    if (Strings.isNullOrEmpty(str)) {
      return null;
    }

    DateTimeParseException ex = null;
    TemporalAccessor accessor = null;

    for (int i = 0; i < FORMATTERS.length; i++) {
      try {
        accessor = FORMATTERS[i].parse(str);
      } catch (DateTimeParseException ex2) {
        ex = ex2;
      }
    }

    if (accessor == null && ex != null) {
      throw ex;
    }

    return GregorianCalendar.from(ZonedDateTime.from(accessor)).getTime();
  }

}
