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

package net.gcolin.validator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.Path.Node;

/**
 * An implementation for Path.*.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class NodeImpl
    implements
      Path.PropertyNode,
      Path.MethodNode,
      Path.ConstructorNode,
      Path.BeanNode,
      Path.ParameterNode,
      Path.ReturnValueNode,
      Path.CrossParameterNode {

  private String name;
  private Integer index;
  private Object key;
  private int parameterIndex;
  private List<Class<?>> parameterTypes;
  private ElementKind kind;
  private boolean inIterable;
  private static final Map<Class<?>, ElementKind> CAST_MAP = new HashMap<>();

  static {
    CAST_MAP.put(Path.BeanNode.class, ElementKind.BEAN);
    CAST_MAP.put(Path.ConstructorNode.class, ElementKind.CONSTRUCTOR);
    CAST_MAP.put(Path.CrossParameterNode.class, ElementKind.CROSS_PARAMETER);
    CAST_MAP.put(Path.MethodNode.class, ElementKind.METHOD);
    CAST_MAP.put(Path.ParameterNode.class, ElementKind.PARAMETER);
    CAST_MAP.put(Path.PropertyNode.class, ElementKind.PROPERTY);
    CAST_MAP.put(Path.ReturnValueNode.class, ElementKind.RETURN_VALUE);
  }

  public NodeImpl(String name, ElementKind kind) {
    this.name = name;
    this.kind = kind;
  }

  public NodeImpl(int index, ElementKind kind) {
    this.index = index;
    this.kind = kind;
  }

  /**
   * Create a node.
   * 
   * @param name name
   * @param kind element kind
   * @param parameters parameter types
   */
  public NodeImpl(String name, ElementKind kind, Class<?>[] parameters) {
    this.name = name;
    this.kind = kind;
    this.parameterTypes = Arrays.asList(parameters);
  }
  
  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isInIterable() {
    return inIterable;
  }

  @Override
  public Integer getIndex() {
    return index;
  }

  @Override
  public Object getKey() {
    return key;
  }

  @Override
  public ElementKind getKind() {
    return kind;
  }

  @Override
  public <T extends Node> T as(Class<T> nodeType) {
    ElementKind ek = CAST_MAP.get(nodeType);
    if (ek != null && kind == ek) {
      return nodeType.cast(this);
    }
    throw new UnsupportedOperationException("the node type " + nodeType + " is not supported");
  }

  @Override
  public int getParameterIndex() {
    return parameterIndex;
  }

  @Override
  public List<Class<?>> getParameterTypes() {
    return parameterTypes;
  }

  public void setInIterable(boolean inIterable) {
    this.inIterable = inIterable;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public void setKey(Object key) {
    this.key = key;
  }

  @Override
  public String toString() {
    String resp;
    if (kind == ElementKind.BEAN && name == null) {
      resp = "root";
    } else if (kind == ElementKind.RETURN_VALUE) {
      resp = "()";
    } else if (kind == ElementKind.CONSTRUCTOR) {
      resp = ".new " + name;
    } else if (kind == ElementKind.PARAMETER && name != null) {
      resp = "[" + name + "]";
    } else if (index != null) {
      resp = "[" + index + "]";
    } else if (key != null) {
      resp = "[" + key + "]";
    } else {
      resp = "." + name;
    }
    return resp;
  }

}
