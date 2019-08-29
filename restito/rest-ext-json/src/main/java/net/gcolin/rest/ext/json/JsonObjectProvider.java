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

import net.gcolin.common.io.Io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * Read/Write JsonObject.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class JsonObjectProvider
    implements
      MessageBodyReader<JsonObject>,
      MessageBodyWriter<JsonObject> {

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return JsonObject.class.isAssignableFrom(type) && JsonProvider.is(mediaType, type);
  }

  @Override
  public long getSize(JsonObject entity, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(JsonObject entity, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
          throws IOException {
    JsonWriter jw = null;
    try {
      jw = JsonProvider.getJsonProvider().createWriter(entityStream);
      jw.writeObject(entity);
    } finally {
      Io.close(jw);
    }
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return type == JsonObject.class && JsonProvider.is(mediaType, type);
  }

  @Override
  public JsonObject readFrom(Class<JsonObject> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
          throws IOException {
    JsonReader jr = null;
    try {
      jr = JsonProvider.getJsonProvider().createReader(entityStream);
      return jr.readObject();
    } finally {
      Io.close(jr);
    }
  }

}
