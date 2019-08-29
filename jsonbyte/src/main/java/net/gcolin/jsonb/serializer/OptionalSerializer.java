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

import net.gcolin.json.JsonGeneratorImpl;
import net.gcolin.json.Utf8JsonGeneratorImpl;
import net.gcolin.jsonb.JsonbSerializerExtended;

import java.io.IOException;
import java.util.Optional;

import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

/**
 * An {@code Optional} serializer.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class OptionalSerializer implements JsonbSerializerExtended<Optional<Object>> {

  private JsonbSerializerExtended<Object> delegate;

  public OptionalSerializer(JsonbSerializerExtended<Object> delegate) {
    this.delegate = delegate;
  }

  @Override
  public void serialize(Optional<Object> obj, JsonGenerator generator, SerializationContext ctx) {
    if (obj.isPresent()) {
      delegate.serialize(obj.get(), generator, ctx);
    }
  }

  @Override
  public void serialize(String key, Optional<Object> obj, JsonGenerator generator,
      SerializationContext ctx) {
    if (obj.isPresent()) {
      delegate.serialize(key, obj.get(), generator, ctx);
    }
  }

  @Override
  public void serialize(Optional<Object> obj, Utf8JsonGeneratorImpl generator,
      SerializationContext ctx) throws IOException {
    if (obj.isPresent()) {
      delegate.serialize(obj.get(), generator, ctx);
    }
  }

  @Override
  public void serialize(byte[] key, Optional<Object> obj, Utf8JsonGeneratorImpl generator,
      SerializationContext ctx) throws IOException {
    if (obj.isPresent()) {
      delegate.serialize(key, obj.get(), generator, ctx);
    }
  }

  @Override
  public void serialize(char[] key, Optional<Object> obj, JsonGeneratorImpl generator,
      SerializationContext ctx) {
    if (obj.isPresent()) {
      delegate.serialize(key, obj.get(), generator, ctx);
    }
  }

  @Override
  public void serialize(Optional<Object> obj, JsonGeneratorImpl generator,
      SerializationContext ctx) {
    if (obj.isPresent()) {
      delegate.serialize(obj.get(), generator, ctx);
    }
  }

}
