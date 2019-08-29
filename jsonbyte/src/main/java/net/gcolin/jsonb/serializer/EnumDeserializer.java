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

package net.gcolin.jsonb.serializer;

import java.util.HashMap;
import java.util.Map;

import javax.json.stream.JsonParser;

/**
 * An enum deserializer.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class EnumDeserializer extends AbstractStringDeserializer<Enum<?>> {

  private Map<String, Enum<?>> map = new HashMap<>();

  /**
   * Create an EnumDeserializer.
   * 
   * @param clazz type of enumeration
   */
  public EnumDeserializer(Class<Enum<?>> clazz) {
    for (Enum<?> en : clazz.getEnumConstants()) {
      map.put(en.name(), en);
    }
  }

  @Override
  protected Enum<?> deserialize(JsonParser parser) {
    return map.get(parser.getString());
  }

}
