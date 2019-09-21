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

import java.util.Calendar;

import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

import net.gcolin.json.JsonGeneratorImpl;
import net.gcolin.jsonb.JsonbSerializerExtended;

/**
 * A {@code Calendar} serializer in milliseconds.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class CalendarMilliSerializer implements JsonbSerializerExtended<Calendar> {

  @Override
  public void serialize(Calendar obj, JsonGenerator generator, SerializationContext ctx) {
    generator.write(obj.getTime().getTime());
  }

  @Override
  public void serialize(String key, Calendar obj, JsonGenerator generator,
      SerializationContext ctx) {
    generator.write(key, obj.getTime().getTime());
  }

  @Override
  public void serialize(char[] key, Calendar obj, JsonGeneratorImpl generator,
      SerializationContext ctx) {
    generator.write0(key, obj.getTime().getTime());
  }

  @Override
  public void serialize(Calendar obj, JsonGeneratorImpl generator, SerializationContext ctx) {
    generator.write0(obj.getTime().getTime());
  }

}
