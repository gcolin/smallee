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

package net.gcolin.jsonb.build;

import net.gcolin.jsonb.JsonbDeserializerExtended;
import net.gcolin.jsonb.JsonbSerializerExtended;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * A Json bean metadata.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JNode {

  private boolean nocache;
  private Type boundType;
  private Map<String, JProperty> elements;
  private JsonbSerializerExtended<Object> serializer;
  private JsonbDeserializerExtended<Object> deserializer;
  private JProperty[] elementList;
  private Object defaultValue;
  private JProperty[] optionalElements;

  public boolean isNocache() {
    return nocache;
  }

  public void setNocache(boolean nocache) {
    this.nocache = nocache;
  }

  public Type getBoundType() {
    return boundType;
  }

  public void setBoundType(Type boundType) {
    this.boundType = boundType;
  }

  public Map<String, JProperty> getElements() {
    return elements;
  }

  public void setElements(Map<String, JProperty> elements) {
    this.elements = elements;
  }

  public JProperty[] getElementList() {
    return elementList;
  }

  public void setElementList(JProperty[] elementList) {
    this.elementList = elementList;
  }

  public Object getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(Object defaultValue) {
    this.defaultValue = defaultValue;
  }

  public JsonbSerializerExtended<Object> getSerializer() {
    return serializer;
  }

  public void setSerializer(JsonbSerializerExtended<Object> serializer) {
    this.serializer = serializer;
  }

  public JsonbDeserializerExtended<Object> getDeserializer() {
    return deserializer;
  }

  public void setDeserializer(JsonbDeserializerExtended<Object> deserializer) {
    this.deserializer = deserializer;
  }
  
  public JProperty[] getOptionalElements() {
    return optionalElements;
  }

  public void setOptionalElements(JProperty[] optionalElements) {
    this.optionalElements = optionalElements;
  }
}
