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

package net.gcolin.rest;

import net.gcolin.common.lang.NumberUtil;
import net.gcolin.rest.util.HttpHeader;

import java.util.Date;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

/**
 * A partial HttpHeaders implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public abstract class AbstractHttpHeaders implements HttpHeaders {

  private Date date;
  private int length = -2;
  private static final HeaderDelegate<Date> HD =
      RuntimeDelegate.getInstance().createHeaderDelegate(Date.class);

  @Override
  public List<String> getRequestHeader(String name) {
    return getRequestHeaders().get(name.toLowerCase());
  }

  @Override
  public String getHeaderString(String name) {
    List<String> header = getRequestHeaders().get(name.toLowerCase());
    if (header == null) {
      return null;
    }
    return String.join(",", header);
  }

  @Override
  public Date getDate() {
    if (date == null) {
      date = HD.fromString(getRequestHeaders().getFirst(HttpHeader.DATE));
    }
    return date;
  }

  @Override
  public int getLength() {
    if (length == -2) {
      length = NumberUtil.parseInt(getRequestHeaders().getFirst(HttpHeader.CONTENT_LENGTH), -1);
    }
    return length;
  }
}
