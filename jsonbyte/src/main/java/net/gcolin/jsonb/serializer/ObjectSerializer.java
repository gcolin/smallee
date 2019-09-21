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

import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

import net.gcolin.json.JsonGeneratorImpl;
import net.gcolin.jsonb.JsonbSerializerExtended;
import net.gcolin.jsonb.build.JContext;
import net.gcolin.jsonb.build.JNode;
import net.gcolin.jsonb.build.JNodeBuilder;

/**
 * An {@code Object} serializer.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ObjectSerializer implements JsonbSerializerExtended<Object> {

  private JNodeBuilder builder;
  private JContext context;

  /**
   * Create an ObjectSerializer.
   * 
   * @param builder node builder
   * @param context builder context
   */
  public ObjectSerializer(JNodeBuilder builder, JContext context) {
    this.builder = builder;
    this.context = context;
  }

  @Override
  public void serialize(Object obj, JsonGenerator generator, SerializationContext ctx) {
    node(obj).getSerializer().serialize(obj, generator, ctx);
  }

  @Override
  public void serialize(String key, Object obj, JsonGenerator generator, SerializationContext ctx) {
    node(obj).getSerializer().serialize(key, obj, generator, ctx);
  }


  @Override
  public void serialize(char[] key, Object obj, JsonGeneratorImpl generator,
      SerializationContext ctx) {
    node(obj).getSerializer().serialize(key, obj, generator, ctx);
  }


  @Override
  public void serialize(Object obj, JsonGeneratorImpl generator, SerializationContext ctx) {
    node(obj).getSerializer().serialize(obj, generator, ctx);
  }

  @SuppressWarnings("unchecked")
  private JNode node(Object obj) {
    return builder.build(null, (Class<Object>) obj.getClass(), obj.getClass(), null, null, context);
  }

}
