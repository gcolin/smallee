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

package net.gcolin.rest.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.HttpMethod;

import net.gcolin.rest.router.HasPath;

/**
 * Contains the resources for each HTTP method type.
 * 
 * <p>
 * It is HTTP path unique.
 * </p>
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ResourceArray implements HasPath {

  public static final int GET = 0;
  public static final int POST = 1;
  public static final int PUT = 2;
  public static final int HEAD = 3;
  public static final int OPTIONS = 4;
  public static final int DELETE = 5;
  private static final Map<String, Integer> METHOD_TO_INDEX = new HashMap<>();

  static {
    METHOD_TO_INDEX.put(HttpMethod.GET, GET);
    METHOD_TO_INDEX.put(HttpMethod.POST, POST);
    METHOD_TO_INDEX.put(HttpMethod.PUT, PUT);
    METHOD_TO_INDEX.put(HttpMethod.HEAD, HEAD);
    METHOD_TO_INDEX.put(HttpMethod.OPTIONS, OPTIONS);
    METHOD_TO_INDEX.put(HttpMethod.DELETE, DELETE);
  }

  private String path;
  private ResourceSelector[] resources = new ResourceSelector[METHOD_TO_INDEX.size()];
  private String[] alloweds;

  public ResourceArray(String path) {
    super();
    this.path = path;
  }

  public static int toInt(String method) {
    return METHOD_TO_INDEX.get(method);
  }

  @Override
  public String getPath() {
    return path;
  }

  /**
   * Add a resource selector.
   * 
   * @param type type of resource
   * @param rs resource selector
   */
  public void add(int type, ResourceSelector rs) {
    resources[type] = rs;
    alloweds = null;
    rs.set(this, type);
  }

  public ResourceSelector get(int type) {
    return resources[type];
  }

  /**
   * Get a ResourceSelector from an HTTP method.
   * 
   * @param method an HTTP method
   * @return a ResourceSelector
   */
  public ResourceSelector get(String method) {
    return get(toInt(method));
  }

  /**
   * Get allowed methods.
   * 
   * @return allowed methods for this resource array.
   */
  public String[] getAlloweds() {
    if (alloweds == null) {
      List<String> list = new ArrayList<>();
      for (int i = 0; i < resources.length; i++) {
        if (resources[i] != null) {
          list.add(toString(i));
        }
      }
      alloweds = list.toArray(new String[list.size()]);
    }
    return alloweds;
  }

  /**
   * Convert an internal index of HTTP method to text.
   * 
   * @param type an internal index
   * @return a text (GET, PUT, POST, ...)
   */
  public static String toString(Integer type) {
    for (Entry<String, Integer> e : METHOD_TO_INDEX.entrySet()) {
      if (e.getValue().equals(type)) {
        return e.getKey();
      }
    }
    return null;
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    str.append('[');
    for (int i = 0; i < resources.length; i++) {
      if (resources[i] != null) {
        if (str.length() > 1) {
          str.append(", ");
        }
        switch (i) {
          case GET:
            str.append(HttpMethod.GET);
            break;
          case POST:
            str.append(HttpMethod.POST);
            break;
          case DELETE:
            str.append(HttpMethod.DELETE);
            break;
          case PUT:
            str.append(HttpMethod.PUT);
            break;
          case OPTIONS:
            str.append(HttpMethod.OPTIONS);
            break;
          case HEAD:
            str.append(HttpMethod.HEAD);
            break;
          default:
            throw new IllegalArgumentException("impossible");
        }
      }
    }
    str.append(']');
    return str.toString();
  }

}
