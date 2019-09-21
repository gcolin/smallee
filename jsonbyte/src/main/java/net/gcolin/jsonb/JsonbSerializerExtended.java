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

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

import net.gcolin.json.JsonGeneratorImpl;

/**
 * The {@code JsonbSerializerExtended} adds some methods for a faster serialization.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public interface JsonbSerializerExtended<T> extends JsonbSerializer<T> {

  void serialize(String key, T obj, JsonGenerator generator, SerializationContext ctx);

  default void serialize(char[] key, T obj, JsonGeneratorImpl generator, SerializationContext ctx) {
    serialize(new String(key, 1, key.length - 3), obj, (JsonGenerator) generator, ctx);
  }

  default void serialize(T obj, JsonGeneratorImpl generator, SerializationContext ctx) {
    serialize(obj, (JsonGenerator) generator, ctx);
  }

}
