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

package net.gcolin.jsonb.build;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The context used during the bean discover phase.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JContext implements Cloneable {

  private boolean timeInMillisecond;
  private Locale locale;
  private String format;
  private boolean nillable;
  private final ThreadLocal<Map<Type, JNode>> currents = new ThreadLocal<>();

  public Map<Type, JNode> getCurrents() {
    Map<Type, JNode> map = currents.get();
    if(map == null) {
      map = new HashMap<>();
      currents.set(map);
    }
    return map;
  }

  public boolean isTimeInMillisecond() {
    return timeInMillisecond;
  }

  public void setTimeInMillisecond(boolean timeInMillisecond) {
    this.timeInMillisecond = timeInMillisecond;
  }

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  @Override
  public JContext clone() {
    try {
      return (JContext) super.clone();
    } catch (CloneNotSupportedException ex) {
      throw new IllegalStateException(ex);
    }
  }

  public boolean isNillable() {
    return nillable;
  }

  public void setNillable(boolean nillable) {
    this.nillable = nillable;
  }
}
