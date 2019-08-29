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

package net.gcolin.common.lang;

import java.io.PrintWriter;
import java.util.List;

/**
 * A runtime exception formed by a list of exceptions
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
@SuppressWarnings("serial")
public class CompositeException extends RuntimeException {

  private final List<Throwable> list;

  /**
   * Create a composite exception.
   * 
   * @param list a list of exceptions
   */
  public CompositeException(List<Throwable> list) {
    this.list = list;
    if (list == null || list.isEmpty()) {
      throw new IllegalArgumentException("list cannot be empty");
    }
  }

  @Override
  public String getMessage() {
    return Strings.join(list, Throwable::getMessage, ", ");
  }

  @Override
  public void printStackTrace(PrintWriter pw) {
    for (Throwable e : list) {
      e.printStackTrace(pw);
    }
  }

  @Override
  public synchronized Throwable getCause() {
    return list.get(0);
  }

  @Override
  public String toString() {
    return Strings.join(list, Throwable::toString, ", \n");
  }

  @Override
  public String getLocalizedMessage() {
    return Strings.join(list, Throwable::getLocalizedMessage, ", ");
  }

  @Override
  public StackTraceElement[] getStackTrace() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setStackTrace(StackTraceElement[] stackTrace) {
    throw new UnsupportedOperationException();
  }

  @Override
  public synchronized Throwable initCause(Throwable cause) {
    throw new UnsupportedOperationException();
  }
}
