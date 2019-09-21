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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.bind.serializer.DeserializationContext;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.jsonb.JsonbDeserializerExtended;
import net.gcolin.jsonb.build.JContext;
import net.gcolin.jsonb.build.JNodeBuilder;

/**
 * An enumMap deserializer.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JNodeEnumMapDeserializer extends JsonbDeserializerExtended<Object> {

  private JNodeMapDeserializer mapDeserializer;

  /**
   * Create a JNodeEnumMapDeserializer.
   * 
   * @param parent parent type
   * @param genericType generic type
   * @param builder node builder
   * @param context builder context
   */
  @SuppressWarnings("unchecked")
  public JNodeEnumMapDeserializer(Type parent, Type genericType, JNodeBuilder builder,
      JContext context) {
    Type[] arguments = new Type[2];
    List<Type> mapParamTypes = Reflect.getGenericTypeArguments(EnumMap.class, genericType, parent);
    arguments[0] = mapParamTypes.get(0);
    arguments[1] = mapParamTypes.get(1);
    Type mapType = new ParameterizedType() {

      @Override
      public Type getRawType() {
        return HashMap.class;
      }

      @Override
      public Type getOwnerType() {
        return null;
      }

      @Override
      public Type[] getActualTypeArguments() {
        return arguments;
      }
    };
    Map<String, Enum<?>> map = new HashMap<>();
    for (Enum<?> en : ((Class<Enum<?>>) Reflect.toClass(arguments[0])).getEnumConstants()) {
      map.put(en.name(), en);
    }

    mapDeserializer = new JNodeMapDeserializer(parent, mapType, builder, context) {

      @Override
      protected Object deserializeKey(String key) {
        return map.get(key);
      }

    };
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public Object deserialize(Event event, Object parent, JsonParser parser,
      DeserializationContext ctx, Type rtType) {
    Map<Object, Object> map =
        (Map<Object, Object>) mapDeserializer.deserialize(event, parent, parser, ctx, rtType);
    return new EnumMap<>((Map) map);
  }

}
