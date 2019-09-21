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

import javax.json.JsonValue;
import javax.json.bind.JsonbException;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import net.gcolin.json.BigDecimalJsonNumber;
import net.gcolin.json.JsonStringImpl;
import net.gcolin.jsonb.JsonbDeserializerExtended;

/**
 * A {@code JsonValue} deserializer.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JsonValueDeserializer extends JsonbDeserializerExtended<JsonValue> {

  @Override
  public JsonValue deserialize(Event event, Object parent, JsonParser parser,
      DeserializationContext ctx, Type rtType) {
    switch (event) {
      case START_ARRAY:
        return new JsonArrayDeserializer().deserialize(event, parent, parser, ctx, rtType);
      case START_OBJECT:
        return new JsonObjectDeserializer().deserialize(event, parent, parser, ctx, rtType);
      case VALUE_FALSE:
        return JsonValue.FALSE;
      case VALUE_TRUE:
        return JsonValue.TRUE;
      case VALUE_NULL:
        return JsonValue.NULL;
      case VALUE_NUMBER:
        return new BigDecimalJsonNumber(parser.getBigDecimal());
      case VALUE_STRING:
        return new JsonStringImpl(parser.getString());
      default:
        throw new JsonbException("cannot find a valid JsonValue");
    }
  }

}
