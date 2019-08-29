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

package net.gcolin.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A fast formatter like slf4j
 * 
 * <p>yyyy-mm-dd hh:mm:sss Level LoggerName - message</p>
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JulFormatter extends Formatter {

  private static class Inner {
    private final Calendar cal = Calendar.getInstance();
    private final StringBuilder str = new StringBuilder();
    private StringWriter sw = new StringWriter();
    private PrintWriter pw = new PrintWriter(sw);
    private String prec;
    private LogRecord precRecord;

    public synchronized String format(LogRecord record) {
      if (record == precRecord) {
        return prec;
      }
      precRecord = record;
      cal.setTimeInMillis(record.getMillis());
      str.setLength(0);
      str.append(cal.get(Calendar.YEAR));
      str.append('-');
      appendTrail(str, 2, cal.get(Calendar.MONTH) + 1);
      str.append('-');
      appendTrail(str, 2, cal.get(Calendar.DAY_OF_MONTH));
      str.append(' ');
      appendTrail(str, 2, cal.get(Calendar.HOUR_OF_DAY));
      str.append(':');
      appendTrail(str, 2, cal.get(Calendar.MINUTE));
      str.append(':');
      appendTrail(str, 3, cal.get(Calendar.MILLISECOND));
      str.append(' ');
      str.append(record.getLevel());
      str.append(' ');
      str.append(record.getLoggerName());
      str.append(" - ");
      if (record.getParameters() == null) {
        str.append(record.getMessage());
      } else {
        str.append(MessageFormat.format(record.getMessage(), record.getParameters()));
      }

      str.append('\n');
      if (record.getThrown() != null) {
        sw.getBuffer().setLength(0);
        record.getThrown().printStackTrace(pw);
        pw.flush();
        str.append(sw.toString());
        str.append('\n');
      }

      return prec = str.toString();
    }

    private void appendTrail(StringBuilder str, int length, int value) {
      String val = Integer.toString(value);
      for (int i = val.length(); i < length; i++) {
        str.append('0');
      }
      str.append(val);
    }
  }

  private static Inner inner = new Inner();

  @Override
  public String format(LogRecord record) {
    return inner.format(record);
  }

}
