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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.RuntimeDelegate;

/**
 * A MultivaluedMap containing header in object.
 * 
 * <p>
 * When this map is modified, it modify a map of string too.
 * </p>
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class HeaderObjectMap implements MultivaluedMap<String, Object> {

  private MultivaluedMap<String, String> stringheaders;
  private MultivaluedMap<String, Object> headers;
  private RuntimeDelegate delegate = RuntimeDelegate.getInstance();

  public HeaderObjectMap(MultivaluedMap<String, Object> headers,
      MultivaluedMap<String, String> stringheaders) {
    this.stringheaders = stringheaders;
    this.headers = headers;
  }

  @Override
  public int size() {
    return headers.size();
  }

  @Override
  public boolean isEmpty() {
    return headers.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return headers.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return headers.containsValue(value);
  }

  @Override
  public List<Object> get(Object key) {
    return headers.get(key);
  }

  @Override
  public List<Object> put(String key, List<Object> value) {
    List<Object> old = headers.get(key);
    remove(key);
    if (value != null) {
      for (Object o : value) {
        add(key, o);
      }
    }
    return old;
  }

  @Override
  public List<Object> remove(Object key) {
    stringheaders.remove(key);
    return headers.remove(key);
  }

  @Override
  public void putAll(Map<? extends String, ? extends List<Object>> map) {
    for (Entry<? extends String, ? extends List<Object>> e : map.entrySet()) {
      put(e.getKey(), e.getValue());
    }
  }

  @Override
  public void clear() {
    stringheaders.clear();
    headers.clear();
  }

  @Override
  public Set<String> keySet() {
    return headers.keySet();
  }

  @Override
  public Collection<List<Object>> values() {
    return headers.values();
  }

  @Override
  public Set<Entry<String, List<Object>>> entrySet() {
    return headers.entrySet();
  }

  @Override
  public void putSingle(String key, Object value) {
    stringheaders.remove(key);
    headers.remove(key);
    add(key, value);
  }

  @Override
  public void add(String key, Object value) {
    if (value == null) {
      return;
    }
    if (value instanceof String) {
      add0(key, (String) value);
      Function<String, List<Object>> fun = HttpHeader.CONVERTERS.get(key);
      if (fun != null) {
        headers.addAll(key, fun.apply((String) value));
      } else {
        headers.add(key, value);
      }
    } else {
      add0(key, convert(value));
      headers.add(key, value);
    }
  }

  private void add0(String key, String converted) {
    if (HttpHeader.LIST_HEADERS.containsKey(key)) {
      String other = stringheaders.getFirst(key);
      stringheaders.putSingle(key,
          other == null ? converted : other + HttpHeader.LIST_HEADERS.get(key) + converted);
    } else {
      stringheaders.add(key, converted);
    }
  }

  @Override
  public Object getFirst(String key) {
    return headers.getFirst(key);
  }

  @Override
  public void addAll(String key, Object... newValues) {
    if (newValues != null) {
      addAll(key, Arrays.asList(newValues));
    }
  }

  @Override
  public void addAll(String key, List<Object> valueList) {
    if (valueList != null) {
      for (Object o : valueList) {
        add(key, o);
      }
    }
  }

  @Override
  public void addFirst(String key, Object value) {
    if (value == null) {
      return;
    }
    if (value instanceof String) {
      String strValue = (String) value;
      addFirst0(key, strValue);
      Function<String, List<Object>> fun = HttpHeader.CONVERTERS.get(key);
      if (fun != null) {
        for (Object o : fun.apply(strValue)) {
          headers.addFirst(key, o);
        }
      } else {
        headers.addFirst(key, value);
      }
    } else {
      addFirst0(key, convert(value));
      headers.addFirst(key, value);
    }
  }

  private void addFirst0(String key, String converted) {
    if (HttpHeader.LIST_HEADERS.containsKey(key)) {
      String other = stringheaders.getFirst(key);
      stringheaders.putSingle(key,
          other == null ? converted : converted + HttpHeader.LIST_HEADERS.get(key) + other);
    } else {
      stringheaders.addFirst(key, converted);
    }
  }

  @Override
  public boolean equalsIgnoreValueOrder(MultivaluedMap<String, Object> otherMap) {
    return headers.equalsIgnoreValueOrder(otherMap);
  }

  @SuppressWarnings("unchecked")
  private String convert(Object obj) {
    if (obj == null || obj instanceof String) {
      return (String) obj;
    }
    return delegate.createHeaderDelegate((Class<Object>) obj.getClass()).toString(obj);
  }

  /**
   * Create headers maps.
   * 
   * @return a pair of headers map
   */
  public static HeaderPair createHeaders() {
    MultivaluedMap<String, String> sheaders = new MultivaluedHashMap<>();
    MultivaluedMap<String, Object> httpHeaders =
        new IgnoreCaseMultivaluedMap<>(new HeaderObjectMap(new MultivaluedHashMap<>(), sheaders));
    return new HeaderPair(httpHeaders,
        new UnmodifiableMultivaluedMap<>(new IgnoreCaseMultivaluedMap<>(sheaders)));
  }

}
