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

import java.math.BigDecimal;
import java.util.NoSuchElementException;

import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;

/**
 * A parser for custom {@code JsonbDeserializer}.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class LimitedParser implements JsonParser {

  private JsonParser delegate;
  private int stack = 0;
  private Event event = null;

  /**
   * Create a LimitedParser.
   * 
   * @param delegate a delegate parser
   * @param event a starting event
   */
  public LimitedParser(JsonParser delegate, Event event) {
    this.delegate = delegate;
    this.event = event;
    if (event == Event.START_OBJECT || event == Event.START_ARRAY) {
      stack++;
    }
  }

  @Override
  public boolean hasNext() {
    if (event == null && delegate.hasNext() && stack > 0) {
      event = delegate.next();
      if (event == Event.START_OBJECT || event == Event.START_ARRAY) {
        stack++;
      } else if (event == Event.END_OBJECT || event == Event.END_ARRAY) {
        stack--;
      }
    }
    return event != null || stack > 0;
  }

  @Override
  public Event next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    Event evt = event;
    event = null;
    return evt;
  }

  @Override
  public String getString() {
    return delegate.getString();
  }

  @Override
  public boolean isIntegralNumber() {
    return delegate.isIntegralNumber();
  }

  @Override
  public int getInt() {
    return delegate.getInt();
  }

  @Override
  public long getLong() {
    return delegate.getLong();
  }

  @Override
  public BigDecimal getBigDecimal() {
    return delegate.getBigDecimal();
  }

  @Override
  public JsonLocation getLocation() {
    return delegate.getLocation();
  }

  @Override
  public void close() {
    delegate.close();
  }
}
