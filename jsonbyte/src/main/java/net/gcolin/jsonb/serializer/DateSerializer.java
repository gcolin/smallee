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

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;

import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

import net.gcolin.jsonb.JsonbSerializerExtended;

/**
 * A {@code Date} serializer.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class DateSerializer extends AbstractDateSerializer
    implements JsonbSerializerExtended<Date> {

  public DateSerializer(String dateFormat, Locale locale) {
    super(dateFormat, locale);
  }
  
  private String format(Date obj) {
    return formatter.format(ZonedDateTime.ofInstant(obj.toInstant(), ZoneOffset.UTC));
  }

  @Override
  public void serialize(Date obj, JsonGenerator generator, SerializationContext ctx) {
    generator.write(format(obj));
  }

  @Override
  public void serialize(String key, Date obj, JsonGenerator generator, SerializationContext ctx) {
    generator.write(key, format(obj));
  }
}
