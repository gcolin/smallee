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

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The PathParam extracts a PathParam from the request.
 * 
 * @author Gaël COLIN
 * @since 1.0
 * @see javax.ws.rs.PathParam
 */
public class PathParam extends ParsableParam {

  private String name;
  private boolean all;
  private Pattern pattern;

  public PathParam(String name) {
    this.setName(name);
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public Object update(ServerInvocationContext context) throws IOException {
    List<String> list = context.getParams().get(name);
    return update(list == null ? null : list.get(0));
  }

  public void setAll(boolean all) {
    this.all = all;
  }

  public boolean isAll() {
    return all;
  }

  public void setPattern(Pattern pattern) {
    this.pattern = pattern;
  }

  public Pattern getPattern() {
    return pattern;
  }

}
