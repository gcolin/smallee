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

package net.gcolin.rest.test.provider;

import net.gcolin.rest.provider.StreamingOutputMessageBodyWriter;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class StreamingOutputMessageBodyWriterTest {

  @Test
  public void getSizeTest() {
    Assert.assertEquals(-1, new StreamingOutputMessageBodyWriter().getSize(null,
        StreamingOutput.class, StreamingOutput.class, null, MediaType.TEXT_PLAIN_TYPE));
  }

  @Test
  public void isWritableTest() {
    Assert.assertTrue(new StreamingOutputMessageBodyWriter().isWriteable(StreamingOutput.class,
        StreamingOutput.class, null, null));
    Assert.assertFalse(
        new StreamingOutputMessageBodyWriter().isWriteable(String.class, String.class, null, null));
  }

  @Test
  public void writeToTest() throws IOException {
    StreamingOutput sout = new StreamingOutput() {

      @Override
      public void write(OutputStream output) throws IOException, WebApplicationException {
        output.write("hello".getBytes(StandardCharsets.UTF_8));
      }
    };
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    new StreamingOutputMessageBodyWriter().writeTo(sout, StreamingOutput.class,
        StreamingOutput.class, null, null, null, bout);
    Assert.assertEquals("hello", new String(bout.toByteArray(), StandardCharsets.UTF_8));
  }

}
