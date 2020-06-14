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

package net.gcolin.cache;

/**
 * Convert by reference. So if a cached object is modified out of the cache, 
 * it may be modified in the cache.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ReferenceInternalConverter<T> implements InternalConverter<T> {

  @Override
  public Object toInternal(T value) {
    return value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T fromInternal(Object internal) {
    return (T) internal;
  }

}
