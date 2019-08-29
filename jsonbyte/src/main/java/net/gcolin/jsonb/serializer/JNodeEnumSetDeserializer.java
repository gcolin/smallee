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

import net.gcolin.common.reflect.Reflect;
import net.gcolin.jsonb.JsonbDeserializerExtended;
import net.gcolin.jsonb.build.JContext;
import net.gcolin.jsonb.build.JNodeBuilder;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

import javax.json.bind.serializer.DeserializationContext;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

/**
 * An enumMap serializer.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JNodeEnumSetDeserializer extends JsonbDeserializerExtended<Object> {

  private JNodeCollectionDeserializer collectionDeserializer;

  /**
   * Create a JNodeEnumSetDeserializer.
   * 
   * @param parent parent type
   * @param genericType generic type
   * @param builder node builder
   * @param context builder context
   */
  public JNodeEnumSetDeserializer(Type parent, Type genericType, JNodeBuilder builder,
      JContext context) {
    Type[] arguments = new Type[1];
    arguments[0] = Reflect.getGenericTypeArguments(EnumSet.class, genericType, parent).get(0);
    Type collectionType = new ParameterizedType() {

      @Override
      public Type getRawType() {
        return ArrayList.class;
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
    collectionDeserializer =
        new JNodeCollectionDeserializer(parent, collectionType, builder, context);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public Object deserialize(Event event, Object parent, JsonParser parser,
      DeserializationContext ctx, Type rtType) {
    Collection<Object> collection =
        (Collection<Object>) collectionDeserializer.deserialize(event, parent, parser, ctx, rtType);
    return EnumSet.copyOf((Collection) collection);
  }

}
