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

package net.gcolin.jsonb.build;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Locale;

import javax.json.stream.JsonParser;

import net.gcolin.jsonb.JsonbDeserializerExtended;
import net.gcolin.jsonb.JsonbSerializerExtended;
import net.gcolin.jsonb.serializer.AbstractNumberDeserializer;
import net.gcolin.jsonb.serializer.CalendarDeserializer;
import net.gcolin.jsonb.serializer.CalendarMilliSerializer;
import net.gcolin.jsonb.serializer.CalendarSerializer;

/**
 * A factory for generating a {@code Calendar} serializer.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class CalendarSerializerFactory implements SerializerFactory {

  private SerializerPair createDateInMilliseconds(
      Locale locale) {
    JsonbDeserializerExtended<Calendar> des;

    if (locale == null) {
      des = new AbstractNumberDeserializer<Calendar>() {

        @Override
        public Calendar deserialize(JsonParser parser) {
          Calendar cal = Calendar.getInstance();
          cal.setTimeInMillis(parser.getLong());
          return cal;
        }

      };
    } else {
      des = new AbstractNumberDeserializer<Calendar>() {

        @Override
        public Calendar deserialize(JsonParser parser) {
          Calendar cal = Calendar.getInstance(locale);
          cal.setTimeInMillis(parser.getLong());
          return cal;
        }
      };
    }

    return new SerializerPair((JsonbSerializerExtended) new CalendarMilliSerializer(),
        (JsonbDeserializerExtended) des);
  }

  @Override
  public SerializerPair create(
      Type genericType, JNodeBuilder nodeBuider, JContext date) {
    if (date.isTimeInMillisecond()) {
      return createDateInMilliseconds(date.getLocale());
    }
    return new SerializerPair(
        (JsonbSerializerExtended) new CalendarSerializer(date.getFormat(), date.getLocale()),
        (JsonbDeserializerExtended) new CalendarDeserializer(date.getFormat(), date.getLocale()));
  }

}
