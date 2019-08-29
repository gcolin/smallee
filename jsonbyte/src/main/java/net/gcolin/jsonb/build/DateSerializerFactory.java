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

import net.gcolin.json.JsonGeneratorImpl;
import net.gcolin.json.Utf8JsonGeneratorImpl;
import net.gcolin.jsonb.JsonbDeserializerExtended;
import net.gcolin.jsonb.JsonbSerializerExtended;
import net.gcolin.jsonb.serializer.AbstractNumberDeserializer;
import net.gcolin.jsonb.serializer.DateDeserializer;
import net.gcolin.jsonb.serializer.DateSerializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;

import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

/**
 * A factory for generating a {@code Date} serializer.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class DateSerializerFactory implements SerializerFactory {

  private SerializerPair dateInMilliseconds;


  private SerializerPair createDateInMilliseconds() {
    return new SerializerPair((JsonbSerializerExtended) new JsonbSerializerExtended<Date>() {

      @Override
      public void serialize(Date obj, JsonGenerator generator, SerializationContext ctx) {
        generator.write(obj.getTime());
      }

      @Override
      public void serialize(String key, Date obj, JsonGenerator generator,
          SerializationContext ctx) {
        generator.write(key, obj.getTime());
      }

      @Override
      public void serialize(Date obj, Utf8JsonGeneratorImpl generator, SerializationContext ctx)
          throws IOException {
        generator.write0(obj.getTime());
      }

      @Override
      public void serialize(byte[] key, Date obj, Utf8JsonGeneratorImpl generator,
          SerializationContext ctx) throws IOException {
        generator.write0(key, obj.getTime());
      }

      @Override
      public void serialize(char[] key, Date obj, JsonGeneratorImpl generator,
          SerializationContext ctx) {
        generator.write0(key, obj.getTime());
      }

      @Override
      public void serialize(Date obj, JsonGeneratorImpl generator, SerializationContext ctx) {
        generator.write0(obj.getTime());
      }

    }, (JsonbDeserializerExtended) new AbstractNumberDeserializer<Date>() {

      @Override
      public Date deserialize(JsonParser parser) {
        return new Date(parser.getLong());
      }

    });
  }

  @Override
  public SerializerPair create(
      Type genericType, JNodeBuilder nodeBuider, JContext date) {
    if (date.isTimeInMillisecond()) {
      if (dateInMilliseconds == null) {
        dateInMilliseconds = createDateInMilliseconds();
      }
      return dateInMilliseconds;
    }
    return new SerializerPair(
        (JsonbSerializerExtended) new DateSerializer(date.getFormat(), date.getLocale()),
        (JsonbDeserializerExtended) new DateDeserializer(date.getFormat(), date.getLocale()));
  }

}
