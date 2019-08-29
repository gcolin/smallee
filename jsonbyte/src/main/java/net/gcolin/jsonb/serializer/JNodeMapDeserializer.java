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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.json.bind.JsonbException;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

/**
 * A map deserializer.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JNodeMapDeserializer extends JsonbDeserializerExtended<Object> {

  private Class<?> mapType;
  private JNode mapValueNode;
  private Type objType;

  /**
   * Create a JNodeMapDeserializer.
   * 
   * @param parent parent type
   * @param genericType generic type
   * @param builder node builder
   * @param context builder context
   */
  @SuppressWarnings("unchecked")
  public JNodeMapDeserializer(Type parent, Type genericType, JNodeBuilder builder,
      JContext context) {
    List<Type> mapTypes = Reflect.getGenericTypeArguments(Map.class, genericType, parent);
    Class<?> type = Reflect.toClass(genericType);
    objType = mapTypes.get(1);
    Class<Object> objClazz = (Class<Object>) Reflect.toClass(objType);
    mapValueNode = builder.build(parent, objClazz, objType, null, null, context);
    if (type.isInterface()) {
      if (SortedMap.class.isAssignableFrom(type)) {
        mapType = TreeMap.class;
      } else {
        mapType = LinkedHashMap.class;
      }
    } else {
      mapType = type;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object deserialize(Event precevent, Object parent, JsonParser parser,
      DeserializationContext ctx, Type rtType) {
    Map<Object, Object> map = (Map<Object, Object>) Reflects.newInstance(mapType);
    if (precevent != JsonParser.Event.START_OBJECT) {
      throw new JsonbException("bad json expected {");
    }
    String key = null;
    boolean cont = true;
    while (cont && parser.hasNext()) {
      Event event = parser.next();
      switch (event) {
        case KEY_NAME:
          key = parser.getString();
          break;
        case START_OBJECT:
        case START_ARRAY:
        case VALUE_FALSE:
        case VALUE_TRUE:
        case VALUE_STRING:
        case VALUE_NUMBER:
          map.put(deserializeKey(key),
              mapValueNode.getDeserializer().deserialize(event, map, parser, ctx, objType));
          key = null;
          break;
        case VALUE_NULL:
          map.put(deserializeKey(key), null);
          key = null;
          break;
        case END_OBJECT:
          cont = false;
          break;
        case END_ARRAY:
          throw new JsonbException("unexpexted char in json [");
        default:
          throw new JsonbException(
              "bad json " + event + " at " + parser.getLocation().getLineNumber() + " : "
                  + parser.getLocation().getColumnNumber());
      }
    }
    return map;
  }

  protected Object deserializeKey(String key) {
    return key;
  }

}
