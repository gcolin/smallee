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

package net.gcolin.rest.ext.datasource;

import net.gcolin.common.io.Io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.activation.DataSource;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * Read/Write DataSource entity.
 * 
 * @author Gaël COLIN
 * @since 1.0
 * @see DataSource
 */
public class DataSourceProvider
    implements MessageBodyWriter<DataSource>, MessageBodyReader<DataSource> {

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return type == DataSource.class;
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return type == DataSource.class;
  }

  @Override
  public void writeTo(DataSource source, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException {
    httpHeaders.putSingle("dataSourceName", source.getName());
    httpHeaders.putSingle(HttpHeaders.CONTENT_TYPE, source.getContentType());
    Io.copy(source.getInputStream(), entityStream);
  }

  @Override
  public DataSource readFrom(Class<DataSource> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException {
    return new SimpleDataSource(httpHeaders.getFirst(HttpHeaders.CONTENT_TYPE),
        httpHeaders.getFirst("dataSourceName"), entityStream);
  }

  @Override
  public long getSize(DataSource datasource, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

}
