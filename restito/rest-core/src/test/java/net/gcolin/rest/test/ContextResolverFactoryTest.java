/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU General Public License
 * Version 2 only ("GPL") or the Common Development and Distribution License("CDDL") (collectively,
 * the "License"). You may not use this file except in compliance with the License. You can obtain a
 * copy of the License at http://glassfish.java.net/public/CDDL+GPL_1_1.html or
 * packager/legal/LICENSE.txt. See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each file and include the
 * License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception: Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License file that accompanied
 * this code.
 *
 * Modifications: If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s): If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include this software in
 * this distribution under the [CDDL or GPL Version 2] license." If you don't indicate a single
 * choice of license, a recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its licensees as
 * provided above. However, if you add GPL Version 2 code and therefore, elected the GPL Version 2
 * license, then the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package net.gcolin.rest.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import net.gcolin.common.lang.Pair;
import net.gcolin.rest.Environment;
import net.gcolin.rest.FastMediaType;
import net.gcolin.rest.provider.SimpleProviders;
import net.gcolin.rest.server.Contexts;
import net.gcolin.rest.server.ServerInvocationContext;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.Produces;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * Context resolvers factory unit test.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ContextResolverFactoryTest {

  @Provider
  private static class CustomStringResolver implements ContextResolver<String> {

    public static final String VALUE = "foof";

    @Override
    public String getContext(Class<?> type) {
      return VALUE;
    }
  }

  @Provider
  @Produces("application/*")
  private static class CustomIntegerResolverA implements ContextResolver<Integer> {

    public static final int VALUE = 1001;

    @Override
    public Integer getContext(Class<?> type) {
      return VALUE;
    }
  }

  @Provider
  @Produces("application/json")
  private static class CustomIntegerResolverB implements ContextResolver<Integer> {

    public static final int VALUE = 2002;

    @Override
    public Integer getContext(Class<?> type) {
      return VALUE;
    }
  }

  @Provider
  @Produces("application/json")
  private static class CustomIntegerResolverC implements ContextResolver<Integer> {

    public static final int VALUE = 3003;

    @Override
    public Integer getContext(Class<?> type) {
      return VALUE;
    }
  }

  @SuppressWarnings("rawtypes")
  public static class CustomMessageBodyWriter implements MessageBodyWriter<Pair> {

    @Context
    private ContextResolver<String> context;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
        MediaType mediaType) {
      return type == Pair.class;
    }

    @Override
    public long getSize(Pair pair, Class<?> type, Type genericType, Annotation[] annotations,
        MediaType mediaType) {
      return -1;
    }

    @Override
    public void writeTo(Pair pair, Class<?> type, Type genericType, Annotation[] annotations,
        MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
        throws IOException, WebApplicationException {
      entityStream.write(context.getContext(Pair.class).getBytes(StandardCharsets.UTF_8));
    }

  }

  public static class CustomMessageBodyWriterB implements MessageBodyWriter<Point> {

    @Context
    private ContextResolver<Integer> context;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
        MediaType mediaType) {
      return type == Point.class;
    }

    @Override
    public long getSize(Point point, Class<?> type, Type genericType, Annotation[] annotations,
        MediaType mediaType) {
      return -1;
    }

    @Override
    public void writeTo(Point point, Class<?> type, Type genericType, Annotation[] annotations,
        MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
        throws IOException, WebApplicationException {
      entityStream
          .write(String.valueOf(context.getContext(Point.class)).getBytes(StandardCharsets.UTF_8));
    }

  }

  @Test
  public void testResolve() throws WebApplicationException, IOException {
    SimpleProviders crf = new SimpleProviders(RuntimeType.SERVER);
    crf.load();
    crf.add(new CustomStringResolver(), String.class);
    crf.add(new CustomIntegerResolverA(), Integer.class);
    crf.add(new CustomIntegerResolverB(), Integer.class);
    crf.add(new CustomIntegerResolverC(), Integer.class);
    crf.add(new CustomMessageBodyWriter());
    crf.add(new CustomMessageBodyWriterB());
    crf.flush(new Environment());

    assertEquals(CustomStringResolver.VALUE,
        crf.getContextResolver(String.class, MediaType.WILDCARD_TYPE).getContext(String.class));
    assertEquals(CustomStringResolver.VALUE,
        crf.getContextResolver(String.class, MediaType.TEXT_PLAIN_TYPE).getContext(String.class));
    assertEquals(CustomStringResolver.VALUE,
        crf.getContextResolver(String.class, null).getContext(String.class));

    assertEquals(CustomIntegerResolverA.VALUE,
        crf.getContextResolver(Integer.class, MediaType.APPLICATION_XML_TYPE)
            .getContext(Integer.class).intValue());
    assertEquals(CustomIntegerResolverA.VALUE,
        crf.getContextResolver(Integer.class, MediaType.valueOf("application/*"))
            .getContext(Integer.class).intValue());

    // Test that resolver "B" is shadowed by a custom resolver "C"
    assertEquals(CustomIntegerResolverC.VALUE,
        crf.getContextResolver(Integer.class, MediaType.APPLICATION_JSON_TYPE)
            .getContext(Integer.class).intValue());

    // Test that there is no matching provider
    assertNull(crf.getContextResolver(Integer.class, MediaType.TEXT_PLAIN_TYPE));

    MessageBodyWriter<?> mw = crf.getMessageBodyWriter(Pair.class, null, null, null);
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    mw.writeTo(null, null, null, null, null, null, bout);
    assertEquals(CustomStringResolver.VALUE,
        new String(bout.toByteArray(), StandardCharsets.UTF_8));

    ServerInvocationContext ctx = new ServerInvocationContext(null);
    ctx.setProduce(FastMediaType.valueOf("application/xml"));
    Contexts.instance().set(ctx);

    mw = crf.getMessageBodyWriter(Point.class, null, null, null);
    bout = new ByteArrayOutputStream();
    mw.writeTo(null, null, null, null, null, null, bout);
    assertEquals(String.valueOf(CustomIntegerResolverA.VALUE),
        new String(bout.toByteArray(), StandardCharsets.UTF_8));

    ctx.setProduce(FastMediaType.valueOf("application/json"));

    mw = crf.getMessageBodyWriter(Point.class, null, null, null);
    bout = new ByteArrayOutputStream();
    mw.writeTo(null, null, null, null, null, null, bout);
    assertEquals(String.valueOf(CustomIntegerResolverC.VALUE),
        new String(bout.toByteArray(), StandardCharsets.UTF_8));
  }
}
