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
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.jsonb.JsonbSerializerExtended;
import net.gcolin.jsonb.build.JContext;
import net.gcolin.jsonb.build.JNode;
import net.gcolin.jsonb.build.JNodeBuilder;

/**
 * An array serializer.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JNodeArraySerializer implements JsonbSerializerExtended<Object> {

  interface Iter {
    Iterator<Object> iterator(Object obj);
  }

  private JNode component;
  private Iter iterBuilder;

  /**
   * Create a JNodeArraySerializer.
   * 
   * @param parent parent type
   * @param genericType generic type
   * @param builder node builder
   * @param context context
   */
  @SuppressWarnings("unchecked")
  public JNodeArraySerializer(Type parent, Type genericType, JNodeBuilder builder,
      JContext context) {
    Class<?> type = Reflect.toClass(genericType).getComponentType();
    this.component = builder.build(parent, (Class<Object>) type, type, null, null, context);
    if (type == int.class) {
      iterBuilder = new Iter() {

        @Override
        public Iterator<Object> iterator(Object obj) {
          return new Iterator<Object>() {

            int idx = 0;
            int[] array = (int[]) obj;

            @Override
            public boolean hasNext() {
              return array.length > idx;
            }

            @Override
            public Object next() {
              if (idx == array.length) {
                throw new NoSuchElementException();
              }
              return array[idx++];
            }

          };
        }
      };
    } else if (type == long.class) {
      iterBuilder = new Iter() {

        @Override
        public Iterator<Object> iterator(Object obj) {
          return new Iterator<Object>() {

            int idx = 0;
            long[] array = (long[]) obj;

            @Override
            public boolean hasNext() {
              return array.length > idx;
            }

            @Override
            public Object next() {
              if (idx == array.length) {
                throw new NoSuchElementException();
              }
              return array[idx++];
            }

          };
        }
      };
    } else if (type == byte.class) {
      iterBuilder = new Iter() {

        @Override
        public Iterator<Object> iterator(Object obj) {
          return new Iterator<Object>() {

            int idx = 0;
            byte[] array = (byte[]) obj;

            @Override
            public boolean hasNext() {
              return array.length > idx;
            }

            @Override
            public Object next() {
              if (idx == array.length) {
                throw new NoSuchElementException();
              }
              return array[idx++];
            }

          };
        }
      };
    } else if (type == double.class) {
      iterBuilder = new Iter() {

        @Override
        public Iterator<Object> iterator(Object obj) {
          return new Iterator<Object>() {

            int idx = 0;
            double[] array = (double[]) obj;

            @Override
            public boolean hasNext() {
              return array.length > idx;
            }

            @Override
            public Object next() {
              if (idx == array.length) {
                throw new NoSuchElementException();
              }
              return array[idx++];
            }

          };
        }
      };
    } else if (type == boolean.class) {
      iterBuilder = new Iter() {

        @Override
        public Iterator<Object> iterator(Object obj) {
          return new Iterator<Object>() {

            int idx = 0;
            boolean[] array = (boolean[]) obj;

            @Override
            public boolean hasNext() {
              return array.length > idx;
            }

            @Override
            public Object next() {
              if (idx == array.length) {
                throw new NoSuchElementException();
              }
              return array[idx++];
            }

          };
        }
      };
    } else if (type == float.class) {
      iterBuilder = new Iter() {

        @Override
        public Iterator<Object> iterator(Object obj) {
          return new Iterator<Object>() {

            int idx = 0;
            float[] array = (float[]) obj;

            @Override
            public boolean hasNext() {
              return array.length > idx;
            }

            @Override
            public Object next() {
              if (idx == array.length) {
                throw new NoSuchElementException();
              }
              return array[idx++];
            }

          };
        }
      };
    } else if (type == char.class) {
      iterBuilder = new Iter() {

        @Override
        public Iterator<Object> iterator(Object obj) {
          return new Iterator<Object>() {

            int idx = 0;
            char[] array = (char[]) obj;

            @Override
            public boolean hasNext() {
              return array.length > idx;
            }

            @Override
            public Object next() {
              if (idx == array.length) {
                throw new NoSuchElementException();
              }
              return array[idx++];
            }

          };
        }
      };
    } else if (type == short.class) {
      iterBuilder = new Iter() {

        @Override
        public Iterator<Object> iterator(Object obj) {
          return new Iterator<Object>() {

            int idx = 0;
            short[] array = (short[]) obj;

            @Override
            public boolean hasNext() {
              return array.length > idx;
            }

            @Override
            public Object next() {
              if (idx == array.length) {
                throw new NoSuchElementException();
              }
              return array[idx++];
            }

          };
        }
      };
    } else {
      iterBuilder = new Iter() {

        @Override
        public Iterator<Object> iterator(Object obj) {
          return new Iterator<Object>() {

            int idx = 0;
            Object[] array = (Object[]) obj;

            @Override
            public boolean hasNext() {
              return array.length > idx;
            }

            @Override
            public Object next() {
              if (idx == array.length) {
                throw new NoSuchElementException();
              }
              return array[idx++];
            }

          };
        }
      };
    }
  }

  @Override
  public void serialize(Object obj, JsonGenerator generator, SerializationContext ctx) {
    generator.writeStartArray();
    serialize0(obj, generator, ctx);
  }

  @Override
  public void serialize(String key, Object obj, JsonGenerator generator, SerializationContext ctx) {
    generator.writeStartArray(key);
    serialize0(obj, generator, ctx);
  }
  
  private void serialize0(Object obj, JsonGenerator generator, SerializationContext ctx) {
    Iterator<Object> it = iterBuilder.iterator(obj);
    while (it.hasNext()) {
      component.getSerializer().serialize(it.next(), generator, ctx);
    }
    generator.writeEnd();
  }

}
