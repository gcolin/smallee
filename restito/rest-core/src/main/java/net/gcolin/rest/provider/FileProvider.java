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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
 * Read/Write File entity.
 * 
 * @author Gaël COLIN
 * @since 1.0
 * @see File
 */
@Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.WILDCARD})
@Consumes({MediaType.APPLICATION_OCTET_STREAM, MediaType.WILDCARD})
public class FileProvider extends Provider<File> {

  public FileProvider() {
    super(File.class);
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return type == File.class;
  }

  @Override
  public long getSize(File entity, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return entity.length();
  }

  @Override
  public void writeTo(File entity, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
          throws IOException {
    FileInputStream fin = null;
    try {
      fin = new FileInputStream(entity);
      httpHeaders.add("fileName", entity.getName());
      Io.copy(fin, entityStream);
    } finally {
      Io.close(fin);
    }
  }

  @Override
  public File readFrom(Class<File> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
          throws IOException {
    File file = File.createTempFile(httpHeaders.getFirst("fileName"), null);
    FileOutputStream fout = null;
    try {
      fout = new FileOutputStream(file);
      Io.copy(entityStream, fout);
    } finally {
      Io.close(fout);
    }
    return file;
  }

}
