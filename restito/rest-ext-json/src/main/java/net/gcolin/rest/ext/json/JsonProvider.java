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

package net.gcolin.rest.ext.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.spi.JsonbProvider;
import javax.json.stream.JsonParsingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * Read/Write Object from a JSON.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class JsonProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object> {

  private static final byte[] NULL = "null".getBytes(StandardCharsets.UTF_8);
  private static javax.json.spi.JsonProvider jsonProvider;
  private static Jsonb jsonb;
  private static JsonbProvider jsonbProvider;

  public static javax.json.spi.JsonProvider getJsonProvider() {
    if(jsonProvider == null) {
      jsonProvider = javax.json.spi.JsonProvider.provider();
    }
    return jsonProvider;
  }
  
  public static void setJsonProvider(javax.json.spi.JsonProvider provider) {
    jsonProvider = provider;
    set0();
  }
  
  public static Jsonb getJsonb() {
    if(jsonb == null) {
      jsonb = JsonbBuilder.newBuilder().withProvider(getJsonProvider()).build();
    }
    return jsonb;
  }
  
  public static void setJsonbProvider(JsonbProvider provider) {
    jsonbProvider = provider;
    set0();
  }

  private static void set0() {
    if(jsonProvider != null && jsonbProvider != null) {
      jsonb = jsonbProvider.create().withProvider(jsonProvider).build();
    } else {
      jsonb = null;
    }
  }
  
  public static boolean is(MediaType mediaType, Class<?> type) {
    return MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType) && type != String.class;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return is(mediaType, type);
  }

  @Override
  public long getSize(Object entity, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(Object entity, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException {
    if (entity == null) {
      entityStream.write(NULL);
      return;
    }
    getJsonb().toJson(entity, entityStream);
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return is(mediaType, type);
  }

  @Override
  public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException {
    try {
      return getJsonb().fromJson(entityStream, genericType);
    } catch (JsonParsingException ex) {
      throw new IOException(ex.getMessage(), ex);
    }
  }

}
