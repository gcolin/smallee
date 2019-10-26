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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import net.gcolin.common.io.Io;
import net.gcolin.common.lang.Strings;

/**
 * Read/Write Reader entity.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
@Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.WILDCARD})
@Consumes({MediaType.APPLICATION_OCTET_STREAM, MediaType.WILDCARD})
public class ReaderProvider extends Provider<Reader> {

  public ReaderProvider() {
    super(Reader.class);
  }

  @Override
  public void writeTo(Reader entity, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
          throws IOException {
    String charset = getCharset(httpHeaders);
    try (Writer writer = new OutputStreamWriter(entityStream, charset)) {
      Io.copy(entity, writer);
    }
  }

  private String getCharset(MultivaluedMap<String, ?> httpHeaders) {
    String content = (String) httpHeaders.getFirst(HttpHeaders.CONTENT_TYPE);
    String charset = StandardCharsets.UTF_8.name();
    if (content != null) {
      int cidx = content.indexOf("charset");
      if (cidx != -1) {
        int eq = content.indexOf('=', cidx) + 1;
        int end = content.indexOf(';', eq);
        if (end == -1) {
          end = content.length();
        }
        charset = Strings.substringTrimed(content, eq, end);
      }
    }
    return charset;
  }

  @Override
  public Reader readFrom(Class<Reader> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
          throws IOException {
    return new InputStreamReader(entityStream, getCharset(httpHeaders));
  }

}
