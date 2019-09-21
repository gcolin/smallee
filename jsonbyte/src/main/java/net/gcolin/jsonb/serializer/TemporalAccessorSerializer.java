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

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

import net.gcolin.json.JsonGeneratorImpl;
import net.gcolin.jsonb.JsonbSerializerExtended;

/**
 * A {@code TemporalAccessor} serializer.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class TemporalAccessorSerializer implements JsonbSerializerExtended<TemporalAccessor> {

  private DateTimeFormatter formatter;

  public TemporalAccessorSerializer(DateTimeFormatter formatter) {
    this.formatter = formatter;
  }

  @Override
  public void serialize(TemporalAccessor obj, JsonGenerator generator, SerializationContext ctx) {
    generator.write(formatter.format(obj));
  }

  @Override
  public void serialize(String key, TemporalAccessor obj, JsonGenerator generator,
      SerializationContext ctx) {
    generator.write(key, formatter.format(obj));
  }

  @Override
  public void serialize(char[] key, TemporalAccessor obj, JsonGeneratorImpl generator,
      SerializationContext ctx) {
    generator.write0Quoted(key, formatter.format(obj));
  }

  @Override
  public void serialize(TemporalAccessor obj, JsonGeneratorImpl generator,
      SerializationContext ctx) {
    generator.write0Quoted(formatter.format(obj));
  }

}
