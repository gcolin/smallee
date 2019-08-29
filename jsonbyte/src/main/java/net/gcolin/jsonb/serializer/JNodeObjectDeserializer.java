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
import net.gcolin.jsonb.build.JNode;
import net.gcolin.jsonb.build.JProperty;
import net.gcolin.jsonb.build.Reflects;

import java.lang.reflect.Type;
import java.util.function.Function;

import javax.json.bind.JsonbException;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

/**
 * A POJO deserializer.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JNodeObjectDeserializer extends JsonbDeserializerExtended<Object> {

  private JNode node;
  private Function<Object, Object> generator;

  /**
   * Create a JNodeObjectDeserializer.
   * 
   * @param node node
   */
  public JNodeObjectDeserializer(JNode node) {
    this.node = node;
    Class<?> type = Reflect.toClass(node.getBoundType());
    generator = Reflects.buildGenerator(type);
  }

  private void skipObject0(JsonParser parser) {
    boolean cont = true;
    while (cont) {
      switch (parser.next()) {
        case START_OBJECT:
          skipObject0(parser);
          break;
        case START_ARRAY:
          skipArray0(parser);
          break;
        case END_OBJECT:
          cont = false;
          break;
        case END_ARRAY:
          throw new JsonbException("bad json");
        default:
          break;
      }
    }
  }

  private void skipArray0(JsonParser parser) {
    boolean cont = true;
    while (cont) {
      switch (parser.next()) {
        case START_OBJECT:
          skipObject0(parser);
          break;
        case START_ARRAY:
          skipArray0(parser);
          break;
        case END_ARRAY:
          cont = false;
          break;
        case END_OBJECT:
        case KEY_NAME:
          throw new JsonbException("bad json");
        default:
          break;
      }
    }
  }

  @Override
  public Object deserialize(Event precevent, Object parent, JsonParser parser,
      DeserializationContext ctx, Type rtType) {
    if (precevent != JsonParser.Event.START_OBJECT) {
      throw new JsonbException("bad json expected {");
    }
    Object obj = generator.apply(parent);
    if (node.getOptionalElements() != null) {
      JProperty[] optionals = node.getOptionalElements();
      for (int i = 0; i < optionals.length; i++) {
        optionals[i].getSetter().set(obj, optionals[i].getNode().getDefaultValue());
      }
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
        case VALUE_NULL:
        case VALUE_TRUE:
        case VALUE_STRING:
        case VALUE_NUMBER:
          boolean skip = true;
          if (node.getElements() != null) {
            JProperty prop = node.getElements().get(key);
            if (prop != null) {
              prop.getSetter().set(obj, prop.getNode().getDeserializer().deserialize(event, obj,
                  parser, ctx, prop.getNode().getBoundType()));
              skip = false;
            }
          }
          if (skip) {
            if (event == Event.START_ARRAY) {
              skipArray0(parser);
            } else if (event == Event.START_OBJECT) {
              skipObject0(parser);
            }
          }
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
    return obj;
  }



}
