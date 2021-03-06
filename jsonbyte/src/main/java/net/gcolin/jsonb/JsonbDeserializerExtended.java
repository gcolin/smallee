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

package net.gcolin.jsonb;

import java.lang.reflect.Type;

import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

/**
 * The {@code JsonbDeserializerExtended} adds some methods for a faster deserialization.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public abstract class JsonbDeserializerExtended<T> implements JsonbDeserializer<T> {

  public abstract T deserialize(Event last, Object parent, JsonParser parser,
      DeserializationContext ctx, Type rtType);

  public T deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
    return deserialize(null, null, parser, ctx, rtType);
  }

}
