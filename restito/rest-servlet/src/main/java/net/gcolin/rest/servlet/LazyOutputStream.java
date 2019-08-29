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
import java.io.OutputStream;

/**
 * An outputStream initialized if needed only.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class LazyOutputStream extends OutputStream {

  private IoSupplier<OutputStream> delegateSupplier;
  private OutputStream delegate;

  public LazyOutputStream(IoSupplier<OutputStream> delegateSupplier) {
    this.delegateSupplier = delegateSupplier;
  }

  private OutputStream getDelegate() throws IOException {
    if (delegate == null) {
      delegate = delegateSupplier.get();
    }
    return delegate;
  }

  @Override
  public void write(int val) throws IOException {
    getDelegate().write(val);
  }

  @Override
  public void write(byte[] data) throws IOException {
    getDelegate().write(data);
  }

  @Override
  public void write(byte[] data, int off, int len) throws IOException {
    getDelegate().write(data, off, len);
  }

  @Override
  public void flush() throws IOException {
    getDelegate().flush();
  }

  @Override
  public void close() throws IOException {
    getDelegate().close();
  }

}
