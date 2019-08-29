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
import net.gcolin.jsonb.build.JNode;
import net.gcolin.jsonb.build.JNodeBuilder;
import net.gcolin.jsonb.build.Reflects;

import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

import javax.json.bind.JsonbException;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

/**
 * A collection deserializer.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JNodeCollectionDeserializer extends JsonbDeserializerExtended<Object> {

  private JNode componentNode;
  private Type objType;
  private Function<Object, Object> generator;

  /**
   * Create a JNodeCollectionDeserializer.
   * 
   * @param parent parent type
   * @param genericType generic type
   * @param builder node builder
   * @param context context
   */
  @SuppressWarnings("unchecked")
  public JNodeCollectionDeserializer(Type parent, Type genericType, JNodeBuilder builder,
      JContext context) {
    Class<?> type = Reflect.toClass(genericType);
    objType = Reflect.getGenericTypeArguments(Collection.class, genericType, parent).get(0);
    Class<Object> objClazz = (Class<Object>) Reflect.toClass(objType);
    componentNode = builder.build(parent, objClazz, objType, null, null, context);
    Class<?> collectionType;
    if (type.isInterface()) {
      if (Queue.class.isAssignableFrom(type)) {
        collectionType = ArrayDeque.class;
      } else if (SortedSet.class.isAssignableFrom(type)) {
        collectionType = TreeSet.class;
      } else if (Set.class.isAssignableFrom(type)) {
        collectionType = HashSet.class;
      } else {
        collectionType = ArrayList.class;
      }
    } else {
      collectionType = type;
    }
    generator = Reflects.buildGenerator(collectionType);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object deserialize(Event precevent, Object parent, JsonParser parser,
      DeserializationContext ctx, Type rtType) {
    Collection<Object> collection = (Collection<Object>) generator.apply(parent);

    if (precevent != JsonParser.Event.START_ARRAY) {
      throw new JsonbException("bad json expected [");
    }
    boolean cont = true;
    while (cont && parser.hasNext()) {
      Event event = parser.next();
      switch (event) {
        case START_OBJECT:
        case START_ARRAY:
        case VALUE_FALSE:
        case VALUE_TRUE:
        case VALUE_STRING:
        case VALUE_NUMBER:
        case VALUE_NULL:
          collection.add(
              componentNode.getDeserializer().deserialize(event, collection, parser, ctx, objType));
          break;
        case END_ARRAY:
          cont = false;
          break;
        case END_OBJECT:
        case KEY_NAME:
          throw new JsonbException("unexpexted char in json");
        default:
          throw new JsonbException(
              "bad json " + event + " at " + parser.getLocation().getLineNumber() + " : "
                  + parser.getLocation().getColumnNumber());
      }
    }
    return collection;
  }

}
