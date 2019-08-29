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

package net.gcolin.rest.param;

import net.gcolin.rest.MessageBodyReaderDecorator;
import net.gcolin.rest.server.ServerInvocationContext;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MultivaluedMap;

/**
 * The PostParam parses the post data of the request with a MessageBodyReader.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class PostParam extends Param {

  private Class<Object> type;
  private Type genericType;
  private Annotation[] annotations;
  private MessageBodyReaderDecorator decorator;

  /**
   * Create a PostParam.
   * 
   * @param type the type of parameter
   * @param genericType the generic type of parameter
   * @param annotations the annotations of parameter
   */
  @SuppressWarnings("unchecked")
  public PostParam(Class<?> type, Type genericType, Annotation[] annotations) {
    this.type = (Class<Object>) type;
    this.genericType = genericType;
    this.annotations = annotations;
  }

  @Override
  public Object update(ServerInvocationContext context) throws IOException {
    MultivaluedMap<String, String> headers = context.getHeaders();
    if (decorator != null) {
      return decorator.readFrom(context, annotations, type, genericType, headers);
    }
    return context.getReader().readFrom(type, genericType, context.getResource().getAnnotations(),
        context.getConsume(), headers, context.getEntityStream());
  }

  public Class<Object> getType() {
    return type;
  }

  public Type getGenericType() {
    return genericType;
  }

  public Annotation[] getAnnotations() {
    return annotations;
  }

  public MessageBodyReaderDecorator getDecorator() {
    return decorator;
  }

  public void setDecorator(MessageBodyReaderDecorator decorator) {
    this.decorator = decorator;
  }
}
