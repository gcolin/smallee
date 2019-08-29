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

package net.gcolin.rest.util;

import net.gcolin.rest.util.lb.BooleanParamConverter;
import net.gcolin.rest.util.lb.ByteArrayParamConverter;
import net.gcolin.rest.util.lb.ByteParamConverter;
import net.gcolin.rest.util.lb.CacheControlParamConverter;
import net.gcolin.rest.util.lb.CharParamConverter;
import net.gcolin.rest.util.lb.Converter;
import net.gcolin.rest.util.lb.CookieParamConverter;
import net.gcolin.rest.util.lb.DateHeaderParamConverter;
import net.gcolin.rest.util.lb.DateParamConverter;
import net.gcolin.rest.util.lb.DoubleParamConverter;
import net.gcolin.rest.util.lb.EntityTagParamConverter;
import net.gcolin.rest.util.lb.EnumParamConverter;
import net.gcolin.rest.util.lb.FileParamConverter;
import net.gcolin.rest.util.lb.FloatParamConverter;
import net.gcolin.rest.util.lb.IntParamConverter;
import net.gcolin.rest.util.lb.LinkParamConverter;
import net.gcolin.rest.util.lb.LocaleParamConverter;
import net.gcolin.rest.util.lb.LongParamConverter;
import net.gcolin.rest.util.lb.MediaTypeParamConverter;
import net.gcolin.rest.util.lb.NewCookieParamConverter;
import net.gcolin.rest.util.lb.NotNullableConverter;
import net.gcolin.rest.util.lb.ShortParamConverter;
import net.gcolin.rest.util.lb.StringBufferParamConverter;
import net.gcolin.rest.util.lb.StringBuilderParamConverter;
import net.gcolin.rest.util.lb.StringParamConverter;
import net.gcolin.rest.util.lb.UriParamConverter;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;

/**
 * The base ParamConverterProvider of the project.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ParamConverterProviderImpl implements ParamConverterProvider {

  private Map<Class<?>, Converter<?>> parser;
  private final Converter<Object> genericConverter = new Converter<Object>() {

    @Override
    public Object fromString(String paramString) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String toString(Object paramT) {
      return paramT == null ? "" : paramT.toString();
    }

  };

  public ParamConverterProviderImpl() {
    this(false);
  }

  /**
   * Create a ParamConverterProviderImpl.
   * 
   * @param header {@code true} id the ParamConverterProvider convert header parameters.
   */
  public ParamConverterProviderImpl(boolean header) {
    parser = new ConcurrentHashMap<>();
    loadMap(parser);
    if (header) {
      parser.put(Date.class, new DateHeaderParamConverter());
    } else {
      parser.put(Date.class, new DateParamConverter());
    }
  }

  private void loadMap(Map<Class<?>, Converter<?>> map) {
    Converter<Integer> lbint = new IntParamConverter();
    Converter<Boolean> lbbool = new BooleanParamConverter();
    Converter<Double> lbdouble = new DoubleParamConverter();
    Converter<Float> lbfloat = new FloatParamConverter();
    Converter<Long> lblong = new LongParamConverter();
    Converter<Character> lbchar = new CharParamConverter();
    Converter<Byte> lbbyte = new ByteParamConverter();
    Converter<Short> lbshort = new ShortParamConverter();

    map.put(int.class, new NotNullableConverter<>(lbint, 0));
    map.put(char.class, new NotNullableConverter<>(lbchar, '\0'));
    map.put(byte.class, new NotNullableConverter<>(lbbyte, (byte) 0));
    map.put(boolean.class, new NotNullableConverter<>(lbbool, false));
    map.put(double.class, new NotNullableConverter<>(lbdouble, 0.0));
    map.put(float.class, new NotNullableConverter<>(lbfloat, 0.0f));
    map.put(long.class, new NotNullableConverter<>(lblong, 0L));
    map.put(short.class, new NotNullableConverter<>(lbshort, (short) 0));

    map.put(Integer.class, lbint);
    map.put(Character.class, lbchar);
    map.put(Byte.class, lbbyte);
    map.put(Boolean.class, lbbool);
    map.put(Double.class, lbdouble);
    map.put(Float.class, lbfloat);
    map.put(Long.class, lblong);
    map.put(Short.class, lbshort);
    map.put(File.class, new FileParamConverter());

    map.put(URI.class, new UriParamConverter());
    map.put(String.class, new StringParamConverter());
    map.put(StringBuffer.class, new StringBufferParamConverter());
    map.put(StringBuilder.class, new StringBuilderParamConverter());
    map.put(byte[].class, new ByteArrayParamConverter());
    map.put(Locale.class, new LocaleParamConverter());
    map.put(EntityTag.class, new EntityTagParamConverter());
    map.put(MediaType.class, new MediaTypeParamConverter());
    map.put(Link.class, new LinkParamConverter());
    map.put(NewCookie.class, new NewCookieParamConverter());
    map.put(Cookie.class, new CookieParamConverter());
    map.put(CacheControl.class, new CacheControlParamConverter());
  }

  @Override
  public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType,
      Annotation[] annotations) {
    return (ParamConverter<T>) getHeaderDelegate(rawType);
  }

  /**
   * Get a header delegate.
   * 
   * @param <T> header object type
   * @param rawType the type of the header
   * @return a specific header delegate or a generic one
   */
  @SuppressWarnings("unchecked")
  public <T> Converter<T> getHeaderDelegate(Class<T> rawType) {
    if (rawType.isEnum()) {
      return (Converter<T>) EnumParamConverter.getInstance(rawType);
    }

    Converter<?> converter = parser.get(rawType);
    if (converter == null) {
      for (Entry<Class<?>, Converter<?>> c : parser.entrySet()) {
        if (c.getKey().isAssignableFrom(rawType)) {
          converter = c.getValue();
          break;
        }
      }
      if (converter != null) {
        parser.put(rawType, converter);
      }
    }
    if (converter == null) {
      converter = genericConverter;
      parser.put(rawType, converter);
    }
    return (Converter<T>) converter;
  }

}
