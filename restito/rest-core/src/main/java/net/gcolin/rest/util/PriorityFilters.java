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

import java.util.Comparator;

import javax.annotation.Priority;

/**
 * Contains the comparator implementation with javax.annotation.Priority.
 * 
 * @author Gaël COLIN
 * @since 1.0
 * @see Filters
 */
public class PriorityFilters {

  public static final Comparator<Object> SORT = new Comparator<Object>() {

    private int priority(Object o1) {
      Priority pp = o1.getClass().getAnnotation(Priority.class);
      return pp == null ? 0 : pp.value();
    }

    @Override
    public int compare(Object o1, Object o2) {
      return priority(o2) - priority(o1);
    }
  };

  private PriorityFilters() {}
}
