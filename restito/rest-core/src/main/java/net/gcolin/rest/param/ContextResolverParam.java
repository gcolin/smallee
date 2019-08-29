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

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;

/**
 * The ContextResolverParam extracts a ContextResolver.
 * 
 * @author Gaël COLIN
 * @since 1.0
 * @see Context
 * @see ContextResolver
 */
public class ContextResolverParam extends Param {

  private Class<?> clazz;
  private Providers providers;

  /**
   * Create a ContextResolverParam.
   * 
   * @param clazz the context type
   * @param providers the provider helper
   */
  public ContextResolverParam(Class<?> clazz, Providers providers) {
    super();
    this.clazz = clazz;
    this.providers = providers;
  }

  @Override
  public Object update(ServerInvocationContext context) {
    return providers.getContextResolver(clazz, context.getProduce());
  }

}
