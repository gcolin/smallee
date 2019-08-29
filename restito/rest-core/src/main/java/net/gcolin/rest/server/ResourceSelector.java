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

/**
 * Can select a resource from a server request.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public abstract class ResourceSelector {

  private int type;
  private ResourceArray array;
  private ResourceCollection collection;

  public abstract AbstractResource select(ServerInvocationContext context);

  public void set(ResourceArray array, int type) {
    this.type = type;
    this.array = array;
  }

  public void set(ResourceCollection collection) {
    this.collection = collection;
  }

  /**
   * Bind a resource to this selector.
   * 
   * @param res a resource
   */
  public void bind(AbstractResource res) {
    if (collection != null) {
      collection.replace((AbstractResource) this, res);
    } else {
      array.add(type, res);
    }
  }
}
