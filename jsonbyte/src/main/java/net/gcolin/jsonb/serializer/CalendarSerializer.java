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

import net.gcolin.json.JsonGeneratorImpl;
import net.gcolin.json.Utf8JsonGeneratorImpl;
import net.gcolin.jsonb.JsonbSerializerExtended;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

/**
 * A {@code Calendar} serializer.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class CalendarSerializer extends AbstractDateSerializer
    implements JsonbSerializerExtended<Calendar> {

  public CalendarSerializer(String dateFormat, Locale locale) {
    super(dateFormat, locale);
  }

  @Override
  public void serialize(Calendar obj, JsonGenerator generator, SerializationContext ctx) {
    generator.write(serialize(obj));
  }

  @Override
  public void serialize(String key, Calendar obj, JsonGenerator generator,
      SerializationContext ctx) {
    generator.write(key, serialize(obj));
  }

  private String serialize(Calendar obj) {
    TimeZone tz = obj.getTimeZone();
    return formatter.format(
        ZonedDateTime.ofInstant(obj.toInstant(), tz == null ? ZoneOffset.UTC : tz.toZoneId()));
  }

  @Override
  public void serialize(Calendar obj, Utf8JsonGeneratorImpl generator, SerializationContext ctx)
      throws IOException {
    generator.write0Quoted(serialize(obj));
  }

  @Override
  public void serialize(byte[] key, Calendar obj, Utf8JsonGeneratorImpl generator,
      SerializationContext ctx) throws IOException {
    generator.write0Quoted(key, serialize(obj));
  }

  @Override
  public void serialize(char[] key, Calendar obj, JsonGeneratorImpl generator,
      SerializationContext ctx) {
    generator.write0Quoted(key, serialize(obj));
  }

  @Override
  public void serialize(Calendar obj, JsonGeneratorImpl generator, SerializationContext ctx) {
    generator.write0Quoted(serialize(obj));
  }

}
