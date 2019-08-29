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

import net.gcolin.common.Priority;
import net.gcolin.common.reflect.Priorities;

/**
 * Hold many resources.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ResourceCollection extends ResourceSelector {

  private AbstractResource[] resources = new AbstractResource[0];

  /**
   * Add a resource to the collection.
   * 
   * @param ar the resource to add
   */
  public void add(AbstractResource ar) {
    AbstractResource[] tmp = new AbstractResource[resources.length + 1];
    System.arraycopy(resources, 0, tmp, 0, resources.length);
    tmp[resources.length] = ar;
    resources = tmp;
    Priorities.sortArray(tmp, x -> x.getResourceMethod().getAnnotation(Priority.class));
    ar.set(this);
  }

  /**
   * Replace a resource.
   * 
   * @param old the old resource
   * @param ne the new resource
   */
  public void replace(AbstractResource old, AbstractResource ne) {
    for (int i = 0; i < resources.length; i++) {
      if (resources[i] == old) {
        resources[i] = ne;
      }
    }
  }

  @Override
  public void bind(AbstractResource ar) {
    throw new IllegalStateException();
  }

  @Override
  public AbstractResource select(ServerInvocationContext context) {
    for (int i = 0; i < resources.length; i++) {
      AbstractResource ar = resources[i].select(context);
      if (ar != null) {
        return ar;
      }
    }
    return null;
  }

}
