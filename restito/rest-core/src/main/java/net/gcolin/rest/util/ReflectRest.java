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

import net.gcolin.common.collection.Func;
import net.gcolin.rest.FastMediaType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

/**
 * Some utilities methods for reflection.
 * 
 * @author Gaël COLIN
 * @since 1.0
 * @see Filters
 */
public class ReflectRest {

  private ReflectRest() {}

  /**
   * Extract FastMediaType from Produces annotation.
   * 
   * @param produces Produces annotation
   * @return a list of FastMediaType or an empty list
   */
  public static List<FastMediaType> buildMediaType(Produces produces) {
    if (produces == null) {
      return Collections.emptyList();
    } else {
      List<FastMediaType> list = new ArrayList<>();
      addMediaType0(values(produces), list);
      return list;
    }
  }

  public static List<FastMediaType> buildMediaType(Produces topclass, Produces produces,
      Produces annotation) {
    return buildMediaType0(new ArrayList<>(), values(topclass), values(produces),
        values(annotation));
  }

  public static Set<FastMediaType> buildMediaType(Consumes topclass, Consumes produces,
      Consumes annotation) {
    return buildMediaType0(new HashSet<>(), values(topclass), values(produces), values(annotation));
  }

  private static String[] values(Produces annotation) {
    return annotation == null ? null : annotation.value();
  }

  private static String[] values(Consumes annotation) {
    return annotation == null ? null : annotation.value();
  }

  private static <T extends Collection<FastMediaType>> T buildMediaType0(T mediatypes,
      String[] topclass, String[] produces, String[] annotation) {
    if (annotation == null) {
      if (produces == null) {
        if (topclass != null) {
          addMediaType0(topclass, mediatypes);
        }
      } else {
        addMediaType0(produces, mediatypes);
      }
    } else {
      addMediaType0(annotation, mediatypes);
    }
    return mediatypes;
  }

  private static void addMediaType0(String[] topclass, Collection<FastMediaType> set) {
    set.addAll(Func.map(Arrays.asList(topclass), e -> FastMediaType.valueOf(e)));
  }
}
