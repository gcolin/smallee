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

package net.gcolin.rest.router;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@code RouterResponse} is the object returned from a router query. It holds the result and
 * the path params.
 * 
 * @author Gaël COLIN
 * @since 1.0
 * @see Router
 */
public class RouterResponse<T extends HasPath> {

  private Map<String, List<String>> params = new HashMap<>();
  private T result;

  public Map<String, List<String>> getParams() {
    return params;
  }

  public void setParams(Map<String, List<String>> params) {
    this.params = params;
  }

  public T getResult() {
    return result;
  }

  public void setResult(T result) {
    this.result = result;
  }

}
