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

package net.gcolin.jsonb.build;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.jsonb.JsonbDeserializerExtended;
import net.gcolin.jsonb.serializer.AbstractNumberDeserializer;
import net.gcolin.jsonb.serializer.AbstractStringDeserializer;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.json.bind.JsonbException;
import javax.json.bind.adapter.JsonbAdapter;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

/**
 * A factory for adapting a {@code JsonbDeserializer} to a {@code JsonbDeserializerExtended}.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JsonbDeserializerAdapter {

  /**
   * Adapt a JsonbDeserializer to a JsonbDeserializerExtended.
   * 
   * @param sr a JsonbDeserializer
   * @return a JsonbDeserializerExtended
   */
  public static JsonbDeserializerExtended<Object> createDeserializer(JsonbDeserializer<Object> sr) {
    return new JsonbDeserializerExtended<Object>() {

      @Override
      public Object deserialize(Event event, Object parent, JsonParser parser,
          DeserializationContext ctx, Type rtType) {
        if (event != Event.START_OBJECT) {
          throw new JsonbException(
              "expected an object for this JsonbDeserializer " + sr.getClass());
        }
        LimitedParser lp = new LimitedParser(parser, event);
        Object obj = sr.deserialize(lp, ctx, rtType);
        while (lp.hasNext()) {
          lp.next();
        }
        return obj;
      }

    };
  }

  /**
   * Create a JsonbDeserializerExtended from a standard type 
   * and an adapter.
   * 
   * @param adapter an adapter
   * @param builder a builder
   * @param parent type of the declaring POJO
   * @return a JsonbDeserializerExtended
   */
  @SuppressWarnings("unchecked")
  public static JsonbDeserializerExtended<Object> createDeserializer(
      JsonbAdapter<Object, Object> adapter, JNodeBuilder builder, Type parent) {
    Class<?> clazz =
        Reflect.getTypeArguments(JsonbAdapter.class, adapter.getClass(), parent).get(1);
    if (clazz == null) {
      clazz = Object.class;
    }
    if (Number.class.isAssignableFrom(clazz)) {
      if (clazz == Integer.class) {
        return new AbstractNumberDeserializer<Object>() {

          @Override
          protected Object deserialize(JsonParser parser) {
            try {
              return adapter.adaptFromJson(parser.getInt());
            } catch (Exception ex) {
              throw new JsonbException(ex.getMessage(), ex);
            }
          }

        };
      } else if (clazz == Long.class) {
        return new AbstractNumberDeserializer<Object>() {

          @Override
          protected Object deserialize(JsonParser parser) {
            try {
              return adapter.adaptFromJson(parser.getLong());
            } catch (Exception ex) {
              throw new JsonbException(ex.getMessage(), ex);
            }
          }

        };
      } else if (clazz == Double.class) {
        return new AbstractNumberDeserializer<Object>() {

          @Override
          protected Object deserialize(JsonParser parser) {
            try {
              return adapter.adaptFromJson(parser.getBigDecimal().doubleValue());
            } catch (Exception ex) {
              throw new JsonbException(ex.getMessage(), ex);
            }
          }

        };
      } else if (clazz == Float.class) {
        return new AbstractNumberDeserializer<Object>() {

          @Override
          protected Object deserialize(JsonParser parser) {
            try {
              return adapter.adaptFromJson(parser.getBigDecimal().floatValue());
            } catch (Exception ex) {
              throw new JsonbException(ex.getMessage(), ex);
            }
          }

        };
      } else if (clazz == Short.class) {
        return new AbstractNumberDeserializer<Object>() {

          @Override
          protected Object deserialize(JsonParser parser) {
            try {
              return adapter.adaptFromJson(parser.getBigDecimal().shortValue());
            } catch (Exception ex) {
              throw new JsonbException(ex.getMessage(), ex);
            }
          }

        };
      } else if (clazz == Byte.class) {
        return new AbstractNumberDeserializer<Object>() {

          @Override
          protected Object deserialize(JsonParser parser) {
            try {
              return adapter.adaptFromJson(parser.getBigDecimal().byteValue());
            } catch (Exception ex) {
              throw new JsonbException(ex.getMessage(), ex);
            }
          }

        };
      } else if (clazz == BigDecimal.class) {
        return new AbstractNumberDeserializer<Object>() {

          @Override
          protected Object deserialize(JsonParser parser) {
            try {
              return adapter.adaptFromJson(parser.getBigDecimal());
            } catch (Exception ex) {
              throw new JsonbException(ex.getMessage(), ex);
            }
          }

        };
      } else if (clazz == BigInteger.class) {
        return new AbstractNumberDeserializer<Object>() {

          @Override
          protected Object deserialize(JsonParser parser) {
            try {
              return adapter.adaptFromJson(parser.getBigDecimal().toBigInteger());
            } catch (Exception ex) {
              throw new JsonbException(ex.getMessage(), ex);
            }
          }

        };
      } else {
        throw new JsonbException("number not supported : " + clazz);
      }
    } else if (clazz == Boolean.class) {
      return new JsonbDeserializerExtended<Object>() {

        @Override
        public Object deserialize(Event event, Object parent, JsonParser parser,
            DeserializationContext ctx, Type rtType) {
          if (event == Event.VALUE_NULL) {
            try {
              return adapter.adaptFromJson(null);
            } catch (Exception ex) {
              throw new JsonbException(ex.getMessage(), ex);
            }
          }
          if (event != Event.VALUE_FALSE && event != Event.VALUE_TRUE) {
            throw new JsonbException("expect boolean");
          }
          try {
            return adapter.adaptFromJson(event == Event.VALUE_TRUE);
          } catch (Exception ex) {
            throw new JsonbException(ex.getMessage(), ex);
          }
        }

      };
    } else if (clazz == String.class) {
      return new AbstractStringDeserializer<Object>() {

        @Override
        protected Object deserialize(JsonParser parser) {
          try {
            return adapter.adaptFromJson(parser.getString());
          } catch (Exception ex) {
            throw new JsonbException(ex.getMessage(), ex);
          }
        }

      };
    } else {
      Type type =
          Reflect.getGenericTypeArguments(JsonbAdapter.class, adapter.getClass(), parent).get(0);
      if (type instanceof TypeVariable) {
        type = Object.class;
      }
      JNode node = builder.build(parent, (Class<Object>) clazz, type, null, null, new JContext());
      return new JsonbDeserializerExtended<Object>() {

        @Override
        public Object deserialize(Event last, Object parent, JsonParser parser,
            DeserializationContext ctx, Type rtType) {
          Object obj = node.getDeserializer().deserialize(last, parent, parser, ctx, rtType);
          try {
            return adapter.adaptFromJson(obj);
          } catch (Exception ex) {
            throw new JsonbException(ex.getMessage(), ex);
          }
        }

      };
    }
  }

}
