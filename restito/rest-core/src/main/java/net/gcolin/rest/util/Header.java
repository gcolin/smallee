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

package net.gcolin.rest.util;

import java.util.Map;

/**
 * The {@code Header} class represents an HTTP header
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class Header {

  private float sort = 1;
  private String value;
  private final Map<String, String> parameters;

  /**
   * Create an HTTP Header.
   * 
   * @param sort the weight of the header
   * @param value the value of the header
   * @param parameters the parameters of the header
   */
  public Header(float sort, String value, Map<String, String> parameters) {
    this.sort = sort;
    this.value = value;
    this.parameters = parameters;
  }

  public String getValue() {
    return value;
  }

  public float getSort() {
    return sort;
  }

  public Map<String, String> getParameters() {
    return parameters;
  }

  @Override
  public String toString() {
    return "Header [sort=" + sort + ", value=" + value + ", parameters=" + parameters + "]";
  }

}
