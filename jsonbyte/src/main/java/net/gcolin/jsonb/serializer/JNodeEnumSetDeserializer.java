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

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.EnumSet;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.jsonb.build.JContext;
import net.gcolin.jsonb.build.JNodeBuilder;

/**
 * An enumMap serializer.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JNodeEnumSetDeserializer extends JNodeArrayDeserializer {

  /**
   * Create a JNodeEnumSetDeserializer.
   * 
   * @param parent parent type
   * @param genericType generic type
   * @param builder node builder
   * @param context builder context
   */
  public JNodeEnumSetDeserializer(Type parent, Type genericType, JNodeBuilder builder,
      JContext context) {
	  init(parent, builder, context, Reflect.getGenericTypeArguments(EnumSet.class, genericType, parent).get(0));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  protected Object deserialize0(Collection<Object> collection) {
    return EnumSet.copyOf((Collection) collection);
  }

}
