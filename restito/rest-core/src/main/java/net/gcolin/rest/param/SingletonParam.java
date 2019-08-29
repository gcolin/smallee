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

package net.gcolin.rest.param;

import net.gcolin.rest.server.ServerInvocationContext;

/**
 * The SingletonParam inject a given object.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class SingletonParam extends Param {

  private Object obj;

  public SingletonParam(Object obj) {
    this.obj = obj;
  }

  @Override
  public Object update(ServerInvocationContext context) {
    return obj;
  }

  @Override
  public boolean isContextual() {
    return false;
  }

}
