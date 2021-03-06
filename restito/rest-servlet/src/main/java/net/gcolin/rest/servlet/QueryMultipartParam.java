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

package net.gcolin.rest.servlet;

import net.gcolin.common.io.Io;
import net.gcolin.rest.param.ParsableParam;
import net.gcolin.rest.server.ServerInvocationContext;

import java.io.IOException;

import javax.servlet.ServletException;

/**
 * Parameter from Servlet Part.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class QueryMultipartParam extends ParsableParam {

  private String name;

  public QueryMultipartParam(String name) {
    this.name = name;
  }

  @Override
  public Object update(ServerInvocationContext context) throws IOException {
    try {
      return update(Io.toString(
          ((ServletExchange) context.getExchange()).getRequest().getPart(name).getInputStream(),
          false));
    } catch (ServletException ex) {
      throw new IOException(ex);
    }
  }

}
