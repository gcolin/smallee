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

package net.gcolin.rest.provider;

import net.gcolin.rest.FastMediaType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * A MessageBodyWriter/MessageBodyReader that uses Produces and Consumes for the method
 * isWriteable/isReadable.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public abstract class Provider<T> implements MessageBodyWriter<T>, MessageBodyReader<T> {

  private Class<T> type;
  protected FastMediaType[] produces;
  protected FastMediaType[] consumes;
  private boolean isProduceWildcard;
  private boolean isConsumeWildcard;

  /**
   * Create a Provider.
   * 
   * @param type the provider type
   */
  @SuppressWarnings("unchecked")
  public Provider(Class<?> type) {
    this.type = (Class<T>) type;
    Produces produce = this.getClass().getAnnotation(Produces.class);
    produces = produce == null ? parse() : parse(produce.value());
    isProduceWildcard = has(FastMediaType.valueOf(MediaType.WILDCARD), produces);

    Consumes consume = this.getClass().getAnnotation(Consumes.class);
    consumes = consume == null ? parse() : parse(consume.value());
    isConsumeWildcard = has(FastMediaType.valueOf(MediaType.WILDCARD), consumes);
  }

  @Override
  public long getSize(T entity, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  public FastMediaType getDefaultProduceMediaType() {
    return produces[0];
  }

  public FastMediaType getDefaultConsumeMediaType() {
    return consumes[0];
  }

  private boolean has(FastMediaType mediatype, FastMediaType[] array) {
    for (int i = 0; i < array.length; i++) {
      if (mediatype == array[i]) {
        return true;
      }
    }
    return false;
  }

  private FastMediaType[] parse(String... args) {
    List<FastMediaType> list = new ArrayList<>();
    if (args.length == 0) {
      list.add(FastMediaType.valueOf(MediaType.WILDCARD));
    }
    for (String a : args) {
      list.add(FastMediaType.valueOf(a));
    }
    FastMediaType[] array = new FastMediaType[list.size()];
    list.toArray(array);
    return array;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return checkType(type) && (isProduceWildcard || checkAnnotations(mediaType, produces));
  }

  protected boolean checkType(Class<?> type) {
    return this.type.isAssignableFrom(type);
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return checkType(type) && (isConsumeWildcard || checkAnnotations(mediaType, consumes));
  }

  protected boolean checkAnnotations(MediaType mediaType, FastMediaType[] array) {
    if (mediaType == null) {
      return false;
    }
    if (mediaType instanceof FastMediaType) {
      FastMediaType fmt = (FastMediaType) mediaType;
      for (int i = 0, l = array.length; i < l; i++) {
        if (fmt.isCompatible(array[i])) {
          return true;
        }
      }
    } else {
      for (int i = 0, l = array.length; i < l; i++) {
        if (mediaType.isCompatible(array[i])) {
          return true;
        }
      }
    }
    return false;
  }
}
