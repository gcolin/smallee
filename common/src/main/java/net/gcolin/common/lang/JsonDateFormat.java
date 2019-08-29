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

package net.gcolin.common.lang;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * The {@code JsonDateFormat} class parses and formats date to JSON in the UTC time zone.
 * 
 * @author Gaël COLIN
 * @since 1.0
 * @see DateFormat
 */
public class JsonDateFormat extends DateFormat {

   
  private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
  private static final long serialVersionUID = 1L;
  
  @Override
  public StringBuffer format(Date date, StringBuffer sb, FieldPosition fieldPosition) {
    Calendar cal = Calendar.getInstance(UTC);
    cal.setTime(date);
    StringBuilder toAppendTo = new StringBuilder();
    append(toAppendTo, 4, cal.get(Calendar.YEAR));
    toAppendTo.append("-");
    append(toAppendTo, 2, cal.get(Calendar.MONTH) + 1);
    toAppendTo.append("-");
    append(toAppendTo, 2, cal.get(Calendar.DAY_OF_MONTH));
    toAppendTo.append("T");
    append(toAppendTo, 2, cal.get(Calendar.HOUR_OF_DAY));
    toAppendTo.append(":");
    append(toAppendTo, 2, cal.get(Calendar.MINUTE));
    toAppendTo.append(":");
    append(toAppendTo, 2, cal.get(Calendar.SECOND));
    toAppendTo.append(".");
    append(toAppendTo, 3, cal.get(Calendar.MILLISECOND));
    toAppendTo.append("Z");
    sb.append(toAppendTo.toString());
    return sb;
  }
  
  @Override
  public Date parse(String source, ParsePosition pos) {
    int len = source.length();
    Calendar cal = Calendar.getInstance(UTC);
    try {
      int year = parse(source, 0, 3);
      int month = parse(source, 5, 6) - 1;
      int day = parse(source, 8, 9);
      if (len >= 13) {
        int hour = parse(source, 11, 12);
        int min = parse(source, 14, 15);
        int sec = parse(source, 17, 18);
        cal.set(year, month, day, hour, min, sec);
        if (len >= 23) {
          cal.set(Calendar.MILLISECOND, parse(source, 20, 22));
        }
      } else {
        cal.set(year, month, day);
      }
      if (pos != null) {
        pos.setIndex(len);
      }
      return cal.getTime();
    } catch (ParseException ex) {
      if (pos != null) {
        pos.setIndex(0);
        pos.setErrorIndex(ex.getErrorOffset());
      }
    }
    return null;
  }

  private int parse(String str, int start, int end) throws ParseException {
    int sum = 0;
    for (int i = start; i <= end; i++) {
      char ch = str.charAt(i);
      if (ch < '0' || ch > '9') {
        throw new ParseException(str, i);
      }
      sum *= 10;
      sum += ch - 48;
    }
    return sum;
  }
  
  private void append(StringBuilder str, int pad, int nb) {
    String ns = Integer.toString(nb);
    int pf = pad - ns.length();
    while (pf > 0) {
      str.append('0');
      pf--;
    }
    str.append(ns);
  }

}
