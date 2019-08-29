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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Read/Write Number entity.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
@Produces({MediaType.TEXT_PLAIN})
@Consumes({MediaType.TEXT_PLAIN})
public class NumberProvider extends Provider<Number> {

  public NumberProvider() {
    super(Number.class);
  }

  @Override
  public void writeTo(Number nb, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
          throws IOException {
    entityStream.write(nb.toString().getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public Number readFrom(Class<Number> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
          throws IOException {
    return new BigDecimal(Io.toString(entityStream, false));
  }

}
