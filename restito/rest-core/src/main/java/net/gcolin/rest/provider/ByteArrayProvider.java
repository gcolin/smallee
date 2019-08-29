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

import net.gcolin.common.io.Io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Read/Write byte array entity.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
@Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.WILDCARD})
@Consumes({MediaType.APPLICATION_OCTET_STREAM, MediaType.WILDCARD})
public class ByteArrayProvider extends Provider<byte[]> {

  public ByteArrayProvider() {
    super(byte[].class);
  }

  @Override
  public long getSize(byte[] entity, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return entity == null ? 0 : entity.length;
  }

  @Override
  public void writeTo(byte[] entity, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
          throws IOException {
    entityStream.write(entity);
  }

  @Override
  public byte[] readFrom(Class<byte[]> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
          throws IOException {
    return Io.toByteArray(entityStream);
  }

}
