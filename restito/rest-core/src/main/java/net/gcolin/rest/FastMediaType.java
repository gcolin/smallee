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

import net.gcolin.common.lang.Strings;
import net.gcolin.rest.util.lb.MediaTypeParamConverter;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

/**
 * A sub class of MediaType which perform valueOf and isCompatible faster.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class FastMediaType extends MediaType {

  private static final int WILDCARD_INDEX = -1;
  private static Map<String, Integer> map = new ConcurrentHashMap<>();
  private static Map<String, FastMediaType> cache = new ConcurrentHashMap<>();
  private static final AtomicInteger INDEX = new AtomicInteger(0);
  private static int idseq = 0;
  private String mediaType;
  private int type;
  private int subtype;
  private int id;
  private Charset charset;
  private static final HeaderDelegate<MediaType> HD =
      RuntimeDelegate.getInstance().createHeaderDelegate(MediaType.class);

  static {
    map.put("*", WILDCARD_INDEX);
  }

  /**
   * Create a FastMediaType.
   * 
   * @param mediaType the media type in text representation
   * @param type the type
   * @param subtype the sub type
   * @param parameters the parameters of the media type
   */
  public FastMediaType(String mediaType, String type, String subtype,
      Map<String, String> parameters) {
    super(type, subtype, parameters);
    this.type = find(type);
    this.subtype = find(subtype);
    this.mediaType = mediaType;
  }

  /**
   * Create an iterator that parse the media type as needed.
   * 
   * @param mediaTypes many media types
   * @return an iterator of FastMediaType
   */
  public static Iterator<FastMediaType> iterator(final String mediaTypes) {
    if (mediaTypes == null) {
      return null;
    }
    return new MediaTypeIterator(mediaTypes);
  }

  /**
   * Get a FastMediaType from a text representation.
   * 
   * @param mediaType a text representation
   * @return a FastMediaType
   */
  public static FastMediaType valueOf(String mediaType) {
    FastMediaType mt = cache.get(mediaType);
    if (mt == null) {
      int paramSplit = mediaType.indexOf(';');
      // don't cache parameters
      if (paramSplit == -1) {
        mt = (FastMediaType) HD.fromString(mediaType);
        mt.id = idseq++;
        cache.put(mediaType, mt);
      } else {
        String basemediaType = mediaType.substring(0, paramSplit).trim();
        return valueOf(basemediaType).withParameters(mediaType, paramSplit);
      }
    }
    return mt;
  }

  /**
   * Convert a MediaType to a FastMediaType.
   * 
   * @param mediaType a standard media type
   * @return a FastMediaType
   */
  public static FastMediaType valueOf(MediaType mediaType) {
    if (mediaType == null) {
      return null;
    }
    if (mediaType instanceof FastMediaType) {
      return (FastMediaType) mediaType;
    }
    return valueOf(HD.toString(mediaType));
  }

  /**
   * Create a new media type base on the current with others parameters.
   * 
   * @param value a text representation
   * @param paramSplit the offset of the parameter
   * @return another FastMediaType with the given parameters
   */
  public FastMediaType withParameters(String value, int paramSplit) {
    FastMediaType mt = new FastMediaType(value, getType(), getSubtype(),
        MediaTypeParamConverter.getParameters(paramSplit, value));
    mt.id = id;
    return mt;
  }

  private int find(String representation) {
    Integer idx = map.get(representation);
    if (idx == null) {
      map.put(representation, INDEX.get());
      idx = INDEX.getAndIncrement();
    }
    return idx;
  }

  /**
   * A fast comparison with another FastMediaType.
   * 
   * @param other another FastMediaType
   * @return {@code true} if compatible
   */
  public boolean isCompatible(FastMediaType other) {
    return other != null
        && (isBothWildCard(other) || isBothSubTypeWildcard(other) || isBothSame(other));

  }

  private boolean isBothWildCard(FastMediaType other) {
    return type == WILDCARD_INDEX || other.type == WILDCARD_INDEX;
  }

  private boolean isBothSame(FastMediaType other) {
    return type == other.type && this.subtype == other.subtype;
  }

  private boolean isBothSubTypeWildcard(FastMediaType other) {
    return type == other.type && (subtype == WILDCARD_INDEX || other.subtype == WILDCARD_INDEX);
  }

  @Override
  public String toString() {
    return mediaType;
  }

  @Override
  public boolean isWildcardSubtype() {
    return subtype == WILDCARD_INDEX;
  }

  @Override
  public boolean isWildcardType() {
    return type == WILDCARD_INDEX;
  }

  public boolean isWildcard() {
    return type == WILDCARD_INDEX;
  }

  public int getId() {
    return id;
  }

  /**
   * Get the Charset of the media type.
   * 
   * @return a Charset
   */
  public Charset getCharset() {
    if (charset == null) {
      String name = getParameters().get("charset");
      charset = Charset.forName((name == null) ? "UTF8" : name);
    }
    return charset;
  }

  private static class MediaTypeIterator implements Iterator<FastMediaType> {

    private int len;
    private int index = 0;
    private String mediaTypes;

    public MediaTypeIterator(String mediaTypes) {
      this.mediaTypes = mediaTypes;
      len = mediaTypes.length();
    }

    @Override
    public boolean hasNext() {
      return len - index > 0;
    }

    @Override
    public FastMediaType next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      int idx = mediaTypes.indexOf(',', index);
      String part;
      if (idx == WILDCARD_INDEX) {
        part = Strings.substringTrimed(mediaTypes, index, mediaTypes.length());
        index = len;
      } else {
        part = Strings.substringTrimed(mediaTypes, index, idx);
        index = idx + 1;
      }
      return FastMediaType.valueOf(part);
    }
  }
}
