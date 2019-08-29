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

package net.gcolin.rest.ext.gzip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

/**
 * Compress/Uncompress in GZIP if needed and if possible.
 * 
 * <p>For using it, add GZipInterceptor.class in you Application 
 * and place GZip annotation on method or class.</p>
 * 
 * 
 * @author Gaël COLIN
 * @since 1.0
 * @see Application
 */
@GZip
public class GZipInterceptor implements WriterInterceptor, ReaderInterceptor {

  private static final String GZIP = "gzip";
  
  @Context
  private Supplier<HttpHeaders> requestHeaders;

  @Override
  public void aroundWriteTo(WriterInterceptorContext ctx) throws IOException {
    String encoding = requestHeaders.get().getHeaderString(HttpHeaders.ACCEPT_ENCODING);
    if (encoding != null && encoding.contains(GZIP)) {
      OutputStream old = ctx.getOutputStream();
      GZIPOutputStream gzipOutputStream = new GZIPOutputStream(old);
      ctx.setOutputStream(gzipOutputStream);
      ctx.getHeaders().putSingle(HttpHeaders.CONTENT_ENCODING, GZIP);
      try {
        ctx.proceed();
      } finally {
        ctx.setOutputStream(old);
        gzipOutputStream.finish();
        gzipOutputStream.close();
      }
    } else {
      ctx.proceed();
    }
  }

  @Override
  public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException {
    String contentEncoding = context.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);
    if (contentEncoding != null && contentEncoding.contains(GZIP)) {
      InputStream old = context.getInputStream();
      context.setInputStream(new GZIPInputStream(old));
      context.getHeaders().remove(HttpHeaders.CONTENT_ENCODING);
      try {
        return context.proceed();
      } finally {
        context.setInputStream(old);
      }
    } else {
      return context.proceed();
    }
  }

}
