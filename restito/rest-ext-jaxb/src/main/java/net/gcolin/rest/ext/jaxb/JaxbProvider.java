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

package net.gcolin.rest.ext.jaxb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Read/Write Object form XML.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
@Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
@Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
public class JaxbProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object> {

  @Context
  private ContextResolver<JAXBContext> contextResolver;

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return checkType(type) && checkMediaType(mediaType);
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return checkType(type) && checkMediaType(mediaType);
  }

  private boolean checkMediaType(MediaType mediaType) {
    return mediaType.isCompatible(MediaType.APPLICATION_XML_TYPE)
        || mediaType.isCompatible(MediaType.TEXT_XML_TYPE);
  }

  private boolean checkType(Class<?> type) {
    return type.isAnnotationPresent(XmlRootElement.class)
        || type.isAnnotationPresent(XmlType.class);
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
      throw new NotFoundException();
    }
    try {
      JAXBContext ctx = contextResolver.getContext(type);
      ctx.createMarshaller().marshal(entity, entityStream);
    } catch (JAXBException ex) {
      throw new IOException(ex);
    }
  }

  @Override
  public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
          throws IOException {
    try {
      return contextResolver.getContext(type).createUnmarshaller().unmarshal(entityStream);
    } catch (JAXBException ex) {
      throw new IOException(ex);
    }
  }

}
