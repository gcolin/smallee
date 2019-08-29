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

package net.gcolin.jsonb.serializer;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import javax.json.bind.JsonbException;

/**
 * A date deserializer.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public abstract class AbstractDateDeserializer<T> extends AbstractStringDeserializer<T> {

  protected DateTimeFormatter shortformatter = DateTimeFormatter.ISO_DATE.withZone(ZoneOffset.UTC);
  protected DateTimeFormatter longformatter =
      DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC);
  protected DateTimeFormatter customformatter;

  /**
   * Create an AbstractDateDeserializer.
   * 
   * @param dateFormat a date format pattern
   * @param locale a locale
   */
  public AbstractDateDeserializer(String dateFormat, Locale locale) {
    if (locale == null) {
      if (dateFormat == null) {
        customformatter = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC);
      } else {
        customformatter = DateTimeFormatter.ofPattern(dateFormat).withZone(ZoneOffset.UTC);
      }
    } else {
      if (dateFormat == null) {
        customformatter =
            DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC).withLocale(locale);
      } else {
        customformatter = DateTimeFormatter.ofPattern(dateFormat, locale).withZone(ZoneOffset.UTC);
      }
    }
  }

  protected Instant getTemporalAccessor(String value) {
    Instant ta = null;
    try {
      ta = customformatter.parse(value, Instant::from);
    } catch (DateTimeParseException ex) {
      try {
        ta = longformatter.parse(value, Instant::from);
      } catch (DateTimeParseException e1) {
        try {
          ta = shortformatter.parse(value, Instant::from);
        } catch (DateTimeParseException e2) {
          throw new JsonbException("cannot parse date : " + value);
        }
      }
    }
    return ta;
  }

}
