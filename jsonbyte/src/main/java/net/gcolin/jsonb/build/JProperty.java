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

import java.nio.charset.StandardCharsets;

/**
 * A key value property.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JProperty {
  
  private final Getter getter;
  private final Setter setter;
  private final String name;
  private final byte[] utf8name;
  private final char[] chname;
  private final JNode node;
  private final boolean nillable;

  /**
   * Create a JSON property.
   * 
   * @param getter getter
   * @param setter setter
   * @param name property name
   * @param node property value
   * @param nillable can be null
   */
  public JProperty(Getter getter, Setter setter, String name, JNode node, boolean nillable) {
    this.node = node;
    this.getter = getter;
    this.setter = setter;
    this.name = name;
    this.nillable = nillable;
    utf8name = ('"' + name + "\":").getBytes(StandardCharsets.UTF_8);
    chname = ('"' + name + "\":").toCharArray();
  }
  
  public char[] getChname() {
    return chname;
  }
  
  public boolean isNillable() {
    return nillable;
  }
  
  public JNode getNode() {
    return node;
  }

  public Getter getGetter() {
    return getter;
  }
  
  public byte[] getUtf8name() {
    return utf8name;
  }

  public Setter getSetter() {
    return setter;
  }

  public String getName() {
    return name;
  }

}
