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

import javax.ws.rs.ext.ParamConverter;

import net.gcolin.rest.util.UrlEncoder;

/**
 * A Param that uses a ParamConverter.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public abstract class ParsableParam extends Param {

  private ParamConverter<?> boxer;
  private boolean encode;
  private Object defaultValue;

  public ParamConverter<?> getBoxer() {
    return boxer;
  }

  public void setBoxer(ParamConverter<?> boxer) {
    this.boxer = boxer;
  }

  /**
   * Get the default value and decode the parameter.
   * 
   * @param paramValue the parameter value
   * @return a better parameter value
   */
  public Object update(Object paramValue) {
    if (paramValue == null) {
      if (defaultValue == null) {
        defaultValue = update0(getDefaultValue());
      }
      return defaultValue;
    } else {
      return update0(paramValue);
    }
  }

  private Object update0(Object obj) {
    if (obj == null) {
      return null;
    }
    Object decoded = isEncode() ? encode((String) obj) : obj;
    if (boxer == null) {
      return decoded;
    } else {
      return boxer.fromString((String) decoded);
    }
  }

  public boolean isEncode() {
    return encode;
  }

  public void setEncode(boolean encode) {
    this.encode = encode;
  }

  /**
   * Encode a text.
   * 
   * @param decoded a decoded text
   * @return an encoded text or {@code null} if decoded is null
   */
  public static String encode(String decoded) {
    if (decoded == null) {
      return null;
    }
    return UrlEncoder.DEFAULT.encode(decoded);
  }
}
