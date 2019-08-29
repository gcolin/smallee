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

/**
 * An internal helper.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
import java.util.Set;

import javax.validation.metadata.GroupConversionDescriptor;

public class Util {

  private Util() {}

  static Class<?>[] convertGroups(Class<?>[] in,
      Set<GroupConversionDescriptor> groupConversionDescriptors) {
    if (in.length == 0) {
      return in;
    }
    Class<?>[] cp = null;
    for (GroupConversionDescriptor gd : groupConversionDescriptors) {
      cp = convert0(in, cp, gd);
    }
    return cp == null ? in : cp;
  }

  private static Class<?>[] convert0(Class<?>[] in, Class<?>[] cp0, GroupConversionDescriptor gd) {
    Class<?>[] cp = cp0;
    for (int i = 0; i < in.length; i++) {
      Class<?> cl = in[i];
      if (cl == gd.getFrom()) {
        if (cp == null) {
          cp = new Class[in.length];
          System.arraycopy(in, 0, cp, 0, cp.length);
        }
        cp[i] = gd.getTo();
      }
    }
    return cp;
  }
}
