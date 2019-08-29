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

package net.gcolin.rest.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * An HttpServletResponse with written detection.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class HttpResponseObserver extends HttpServletResponseWrapper {

  private boolean hasWritten;

  public HttpResponseObserver(HttpServletResponse response) {
    super(response);
  }

  public boolean isHasWritten() {
    return hasWritten;
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    return new PrintWriterObserver(super.getWriter());
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    return new ServletOutputStreamObserver(super.getOutputStream());
  }

  private class ServletOutputStreamObserver extends ServletOutputStream {

    private ServletOutputStream delegate;

    public ServletOutputStreamObserver(ServletOutputStream delegate) {
      this.delegate = delegate;
    }

    @Override
    public boolean isReady() {
      return delegate.isReady();
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
      delegate.setWriteListener(writeListener);
    }

    @Override
    public void write(int bt) throws IOException {
      hasWritten = true;
      delegate.write(bt);
    }

    @Override
    public void write(byte[] ba, int off, int len) throws IOException {
      hasWritten = true;
      delegate.write(ba, off, len);
    }

  }

  private class PrintWriterObserver extends PrintWriter {

    public PrintWriterObserver(Writer out) {
      super(out);
    }

    @Override
    public void write(char[] buf, int off, int len) {
      hasWritten = true;
      super.write(buf, off, len);
    }

    @Override
    public void write(String str, int off, int len) {
      hasWritten = true;
      super.write(str, off, len);
    }

    @Override
    public void write(int ch) {
      hasWritten = true;
      super.write(ch);
    }

  }
}
