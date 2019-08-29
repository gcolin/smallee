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

import net.gcolin.common.io.Io;
import net.gcolin.json.JsonParserImpl;
import net.gcolin.json.JsonParserReaderImpl;
import net.gcolin.jsonb.JsonbDeserializerExtended;

import java.lang.reflect.Type;

import javax.json.JsonArray;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

/**
 * A {@code JsonArray} deserializer.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JsonArrayDeserializer extends JsonbDeserializerExtended<JsonArray> {

  @Override
  public JsonArray deserialize(Event event, Object parent, JsonParser parser,
      DeserializationContext ctx, Type rtType) {
    JsonParserReaderImpl reader = null;
    try {
      reader = new JsonParserReaderImpl(parser);
      return (JsonArray) JsonParserImpl.getValue0(parser, event);
    } finally {
      Io.close(reader);
    }
  }
}
