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
package net.gcolin.di.atinject.jndi;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.gcolin.di.atinject.InjectionPoint;
import net.gcolin.di.core.InjectException;

/**
 * ResourceInjectionPoint.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public abstract class ResourceInjectionPoint implements InjectionPoint {

  private String name;

  public ResourceInjectionPoint(String name) {
    this.name = name;
  }

  public Object getObject() {
    try {
      InitialContext ctx = new InitialContext();
      return ctx.lookup(name);
    } catch (NamingException ex) {
      throw new InjectException(ex);
    }
  }

}
