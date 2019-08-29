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
import net.gcolin.jsonb.build.JNode;
import net.gcolin.jsonb.build.JProperty;

import java.io.IOException;

import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

/**
 * A POJO serializer.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JNodeObjectSerializer implements JsonbSerializerExtended<Object> {

  private JNode node;

  public JNodeObjectSerializer(JNode node) {
    this.node = node;
  }

  @Override
  public void serialize(Object obj, JsonGenerator generator, SerializationContext ctx) {
    generator.writeStartObject();
    serialize0(obj, generator, ctx);
  }

  @Override
  public void serialize(String key, Object obj, JsonGenerator generator, SerializationContext ctx) {
    generator.writeStartObject(key);
    serialize0(obj, generator, ctx);
  }

  @Override
  public void serialize(Object obj, Utf8JsonGeneratorImpl generator, SerializationContext ctx)
      throws IOException {
    generator.writeStartObject0();
    serialize1(obj, generator, ctx);
  }

  @Override
  public void serialize(byte[] key, Object obj, Utf8JsonGeneratorImpl generator,
      SerializationContext ctx) throws IOException {
    generator.writeStartObject0(key);
    serialize1(obj, generator, ctx);
  }

  @Override
  public void serialize(char[] key, Object obj, JsonGeneratorImpl generator,
      SerializationContext ctx) {
    generator.writeStartObject0(key);
    serialize2(obj, generator, ctx);
  }

  @Override
  public void serialize(Object obj, JsonGeneratorImpl generator, SerializationContext ctx) {
    generator.writeStartObject0();
    serialize2(obj, generator, ctx);
  }

  private void serialize0(Object obj, JsonGenerator generator, SerializationContext ctx) {
    JProperty[] props = node.getElementList();
    if (props != null) {
      for (int i = 0; i < props.length; i++) {
        Object val = props[i].getGetter().get(obj);
        if (val != null) {
          props[i].getNode().getSerializer().serialize(props[i].getName(), val, generator, ctx);
        }
      }
    }
    generator.writeEnd();
  }

  private void serialize1(Object obj, Utf8JsonGeneratorImpl generator, SerializationContext ctx)
      throws IOException {
    JProperty[] props = node.getElementList();
    if (props != null) {
      for (int i = 0; i < props.length; i++) {
        Object val = props[i].getGetter().get(obj);
        if (val != null) {
          props[i].getNode().getSerializer().serialize(props[i].getUtf8name(), val, generator, ctx);
        }
      }
    }
    generator.writeEnd0();
  }

  private void serialize2(Object obj, JsonGeneratorImpl generator, SerializationContext ctx) {
    JProperty[] props = node.getElementList();
    if (props != null) {
      for (int i = 0; i < props.length; i++) {
        Object val = props[i].getGetter().get(obj);
        if (val != null) {
          props[i].getNode().getSerializer().serialize(props[i].getChname(), val, generator, ctx);
        }
      }
    }
    generator.writeEnd0();
  }

}
