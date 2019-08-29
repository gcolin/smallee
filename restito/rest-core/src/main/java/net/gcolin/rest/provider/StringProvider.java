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

import net.gcolin.common.collection.Collections2;
import net.gcolin.common.io.Io;
import net.gcolin.rest.FastMediaType;
import net.gcolin.rest.util.HttpHeader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Read/Write String entity.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
@Produces({MediaType.TEXT_PLAIN, MediaType.WILDCARD})
@Consumes({MediaType.WILDCARD})
public class StringProvider extends Provider<String> {

  private MediaType contentType =
      FastMediaType.valueOf(MediaType.TEXT_PLAIN + ";charset=" + Charset.defaultCharset().name());
  private Charset charset = Charset.defaultCharset();
  private StringProviderExtension[] extensions;

  /**
   * Create a StringProvider.
   */
  public StringProvider() {
    super(String.class);
    extensions = Collections2.safeFillServiceLoaderAsArray(StringProvider.class.getClassLoader(),
        StringProviderExtension.class);
  }

  @Override
  public void writeTo(String entity, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
          throws IOException {
    for (int i = 0; i < extensions.length; i++) {
      if (extensions[i].writeTo(entity, type, genericType, annotations, mediaType, httpHeaders,
          entityStream)) {
        return;
      }
    }
    if (badMediaType(mediaType) && !httpHeaders.containsKey(HttpHeader.CONTENT_TYPE)) {
      httpHeaders.putSingle(HttpHeader.CONTENT_TYPE, contentType);
    }
    entityStream.write(entity.getBytes(charset));
  }

  private boolean badMediaType(MediaType mediaType) {
    return mediaType == null || mediaType.isWildcardType() || !"text".equals(mediaType.getType());
  }

  @Override
  public String readFrom(Class<String> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
          throws IOException {
    return Io.toString(entityStream, false);
  }

}
