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

package net.gcolin.rest;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * An internal data structure.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class InvocationContext {

  private Map<String, Object> attributes;
  private FastMediaType consume;
  private FastMediaType produce;
  private MessageBodyReader<Object> reader;
  private MessageBodyWriter<Object> writer;
  private InputStream entityStream;

  public InvocationContext() {}

  public InvocationContext(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  private Map<String, Object> getAttributes() {
    if (attributes == null) {
      attributes = new HashMap<>();
    }
    return attributes;
  }

  public Object getProperty(String name) {
    return getAttributes().get(name);
  }

  public Collection<String> getPropertyNames() {
    return getAttributes().keySet();
  }

  public void setProperty(String name, Object object) {
    getAttributes().put(name, object);
  }

  public void removeProperty(String name) {
    getAttributes().remove(name);
  }

  public FastMediaType getConsume() {
    return consume;
  }

  public void setConsume(FastMediaType consume) {
    this.consume = consume;
  }

  public FastMediaType getProduce() {
    return produce;
  }

  public void setProduce(FastMediaType produce) {
    this.produce = produce;
  }

  public MessageBodyReader<Object> getReader() {
    return reader;
  }

  public void setReader(MessageBodyReader<Object> reader) {
    this.reader = reader;
  }

  public MessageBodyWriter<Object> getWriter() {
    return writer;
  }

  public void setWriter(MessageBodyWriter<Object> writer) {
    this.writer = writer;
  }

  public InputStream getEntityStream() {
    return entityStream;
  }

  public void setEntityStream(InputStream entityStream) {
    this.entityStream = entityStream;
  }
}
