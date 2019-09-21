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

import java.lang.reflect.Type;

import javax.json.bind.JsonbException;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import net.gcolin.jsonb.JsonbDeserializerExtended;

/**
 * A number deserializer.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public abstract class AbstractNumberDeserializer<T> extends JsonbDeserializerExtended<T> {

  @Override
  public T deserialize(Event event, Object parent, JsonParser parser, DeserializationContext ctx,
      Type rtType) {
    if (event == null || event == Event.VALUE_NULL) {
      return null;
    } else if (event != Event.VALUE_NUMBER) {
      throw new JsonbException("expect a string");
    }
    return deserialize(parser);
  }

  protected abstract T deserialize(JsonParser parser);

}
