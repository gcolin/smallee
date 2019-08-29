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
package net.gcolin.common.reflect;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import net.gcolin.common.Priority;

/**
 * A small utility class for priorities.
 * 
 * @author Gaël COLIN
 * @since 1.2
 */
public class Priorities {

  private Priorities() {}

  public static <T> void sortArray(T[] array, Function<T, Priority> fun) {
    Arrays.sort(array, comparator(fun));
  }
  
  public static <T> void sort(List<T> list, Function<T, Priority> fun) {
    Collections.sort(list, comparator(fun));
  }

  private static <T> Comparator<? super T> comparator(Function<T, Priority> fun) {
    return (a, b) -> {
      Priority pa = fun.apply((T) a);
      Priority pb = fun.apply((T) b);
      int na = pa == null ? Integer.MAX_VALUE : pa.value();
      int nb = pb == null ? Integer.MAX_VALUE : pb.value();
      if(na == nb) {
        return 0;
      }
      if(na > nb) {
        return 1;
      } else {
        return -1;
      }
    };
  }
}
