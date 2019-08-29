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

package net.gcolin.rest.test;

import net.gcolin.rest.AbstractHttpHeaders;
import net.gcolin.rest.util.HttpHeader;
import net.gcolin.rest.util.lb.DateHeaderParamConverter;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class AbstractHttpHeadersTest {

  MultivaluedMap<String, String> map = new MultivaluedHashMap<>();

  AbstractHttpHeaders headers = new AbstractHttpHeaders() {

    @Override
    public MultivaluedMap<String, String> getRequestHeaders() {
      return map;
    }

    @Override
    public MediaType getMediaType() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Locale getLanguage() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Cookie> getCookies() {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<MediaType> getAcceptableMediaTypes() {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<Locale> getAcceptableLanguages() {
      throw new UnsupportedOperationException();
    }
  };

  @Test
  public void getHeaderStringTest() {
    map.addAll("hello", "1", "2", "3");
    map.addAll("one", "word");

    Assert.assertNull(headers.getHeaderString("unknown"));
    Assert.assertEquals("word", headers.getHeaderString("one"));
    Assert.assertEquals("1,2,3", headers.getHeaderString("hello"));
  }

  @Test
  public void getRequestHeaderTest() {
    map.addAll("hello", "1", "2", "3");
    map.addAll("one", "word");

    Assert.assertNull(headers.getRequestHeader("unknown"));
    Assert.assertEquals(Arrays.asList("word"), headers.getRequestHeader("one"));
    Assert.assertEquals(Arrays.asList("1", "2", "3"), headers.getRequestHeader("hello"));
  }

  @Test
  public void getDateTest() {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MILLISECOND, 0);
    Date date = cal.getTime();
    map.addAll(HttpHeader.DATE, new DateHeaderParamConverter().toString(date));

    Assert.assertEquals(date, headers.getDate());
    Assert.assertTrue(headers.getDate() == headers.getDate());
  }

  @Test
  public void getLengthTest() {
    map.addAll(HttpHeader.CONTENT_LENGTH, "123");

    Assert.assertEquals(123, headers.getLength());
    Assert.assertTrue(headers.getLength() == headers.getLength());
  }



}
