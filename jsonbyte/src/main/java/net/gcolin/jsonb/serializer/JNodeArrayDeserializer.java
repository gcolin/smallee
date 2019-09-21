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

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

import javax.json.bind.serializer.DeserializationContext;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.jsonb.JsonbDeserializerExtended;
import net.gcolin.jsonb.build.JContext;
import net.gcolin.jsonb.build.JNodeBuilder;

/**
 * An array deserializer.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JNodeArrayDeserializer extends JsonbDeserializerExtended<Object> {

  private Class<?> componentType;
  private JNodeCollectionDeserializer collectionDeserializer;

  /**
   * Create a JNodeArrayDeserializer.
   * 
   * @param parent declaring class
   * @param genericType generic type
   * @param builder node builder
   * @param context builder context
   */
  public JNodeArrayDeserializer(Type parent, Type genericType, JNodeBuilder builder,
      JContext context) {
    componentType = Reflect.toClass(genericType).getComponentType();
    Type[] arguments = new Type[1];
    arguments[0] = componentType;
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

  @SuppressWarnings("unchecked")
  @Override
  public Object deserialize(Event event, Object parent, JsonParser parser,
      DeserializationContext ctx, Type rtType) {
    Collection<Object> collection =
        (Collection<Object>) collectionDeserializer.deserialize(event, parent, parser, ctx, rtType);
    Object array = Array.newInstance(componentType, collection.size());
    int idx = 0;
    for (Object obj : collection) {
      Array.set(array, idx++, obj);
    }
    return array;
  }

}
