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
import java.util.Map;
import java.util.Map.Entry;

import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.jsonb.JsonbSerializerExtended;
import net.gcolin.jsonb.build.JContext;
import net.gcolin.jsonb.build.JNode;
import net.gcolin.jsonb.build.JNodeBuilder;

/**
 * A map serializer.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JNodeMapSerializer implements JsonbSerializerExtended<Object> {
  private JNode mapValueNode;
  private Type objType;
  private static final char QUOTE = '"';
  private static final char TWOPOINT = ':';

  /**
   * Create a JNodeMapSerializer.
   * 
   * @param parent parent type
   * @param genericType generic type
   * @param builder node builder
   * @param context builder context
   */
  @SuppressWarnings("unchecked")
  public JNodeMapSerializer(Type parent, Type genericType, JNodeBuilder builder, JContext context) {
    objType = Reflect.getGenericTypeArguments(Map.class, genericType, parent).get(1);
    Class<Object> objClazz = (Class<Object>) Reflect.toClass(objType);
    mapValueNode = builder.build(parent, objClazz, objType, null, null, context);
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

  @SuppressWarnings("unchecked")
  private void serialize0(Object obj, JsonGenerator generator, SerializationContext ctx) {
    for (Entry<Object, Object> entry : ((Map<Object, Object>) obj).entrySet()) {
      Object val = entry.getValue();
      if (val != null) {
        mapValueNode.getSerializer().serialize(serializeKey(entry.getKey()), val, generator, ctx);
      }
    }
    generator.writeEnd();
  }

  protected String serializeKey(Object key) {
    return (String) key;
  }

  protected char[] serializeKeyCh(Object key) {
    String ke = (String) key;
    char[] ch = new char[ke.length() + 3];
    ke.getChars(0, ke.length(), ch, 1);
    ch[0] = QUOTE;
    ch[ch.length - 2] = QUOTE;
    ch[ch.length - 1] = TWOPOINT;
    return ch;
  }

}
