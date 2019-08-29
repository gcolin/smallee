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

package net.gcolin.rest.test.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.gcolin.common.io.ByteArrayInputStream;
import net.gcolin.rest.client.ClientFeatureBuilder;
import net.gcolin.rest.client.ClientResponse;
import net.gcolin.rest.provider.SimpleProviders;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response.Status;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ClientResponseTest {

  ClientResponse response;
  HttpURLConnection connection;
  ClientFeatureBuilder builder;

  /**
   * Initialize test.
   * 
   * @throws IOException if an error occurs.
   */
  @Before
  public void before() throws IOException {
    connection = mock(HttpURLConnection.class);
    builder = mock(ClientFeatureBuilder.class);
    response = new ClientResponse(connection, builder);
  }

  @Test
  public void statusTest() throws IOException {
    when(connection.getResponseCode()).thenReturn(200);
    response = new ClientResponse(connection, builder);
    assertEquals(200, response.getStatus());
    assertEquals(Status.OK, response.getStatusInfo());
  }

  @Test
  public void getEntityTest() throws IOException {
    preparegetEntity();

    Object entity = response.getEntity();
    assertTrue(entity instanceof InputStream);
    String str = response.readEntity(String.class);

    assertEquals("hello", str);
    assertTrue(str == response.getEntity());
  }

  @Test
  public void readGenericTest() throws IOException {
    preparegetEntity();

    Object entity = response.getEntity();
    assertTrue(response.hasEntity());
    assertTrue(entity instanceof InputStream);
    String str = response.readEntity(new GenericType<String>() {});

    assertEquals("hello", str);
    assertTrue(str == response.getEntity());
  }

  public static class CloseableByteArrayInputStream extends java.io.ByteArrayInputStream {

    private boolean closed;

    public CloseableByteArrayInputStream(byte[] buf) {
      super(buf);
    }

    @Override
    public void close() throws IOException {
      closed = true;
      super.close();
    }

    public boolean isClosed() {
      return closed;
    }

  }

  private void preparegetEntity() throws IOException {
    Map<String, List<String>> headers = new HashMap<>();
    headers.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.TEXT_PLAIN));
    headers.put(HttpHeaders.CONTENT_LENGTH, Arrays.asList("5"));
    headers.put(HttpHeaders.CONTENT_LANGUAGE, Arrays.asList("fr"));
    when(connection.getHeaderFields()).thenReturn(headers);
    InputStream in = new CloseableByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8));
    when(connection.getInputStream()).thenReturn(in);
    SimpleProviders providers = new SimpleProviders(RuntimeType.SERVER);
    providers.load();
    when(builder.getProviders()).thenReturn(providers);
    response = new ClientResponse(connection, builder);
  }

  @Test
  public void bufferEntityTest() throws IOException {
    preparegetEntity();
    response.bufferEntity();

    Object entity = response.getEntity();
    assertTrue(response.hasEntity());
    assertTrue(entity instanceof InputStream);
    assertTrue(entity instanceof ByteArrayInputStream);
    CloseableByteArrayInputStream in = (CloseableByteArrayInputStream) connection.getInputStream();
    assertTrue(in.isClosed());
    String str = response.readEntity(new GenericType<String>() {});

    assertEquals("hello", str);
    assertTrue(str == response.getEntity());
  }

  @Test
  @Ignore
  public void closeTest() throws IOException {
    preparegetEntity();
    CloseableByteArrayInputStream in = (CloseableByteArrayInputStream) connection.getInputStream();
    response.close();
    assertTrue(in.isClosed());
  }

  @Test
  public void getMediaTypeTest() throws IOException {
    preparegetEntity();
    assertEquals(MediaType.TEXT_PLAIN_TYPE, response.getMediaType());
  }

  @Test
  public void getLanguageTest() throws IOException {
    preparegetEntity();
    assertEquals(Locale.FRENCH, response.getLanguage());
  }

  @Test
  public void getLengthTest() throws IOException {
    preparegetEntity();
    assertEquals(5, response.getLength());
  }

  @Test
  public void getAllowedMethodsTest() throws IOException {
    Map<String, List<String>> headers = new HashMap<>();
    headers.put(HttpHeaders.ALLOW, Arrays.asList("GET"));
    when(connection.getHeaderFields()).thenReturn(headers);
    response = new ClientResponse(connection, builder);
    assertTrue(response.getAllowedMethods().containsAll(Arrays.asList("GET")));
    assertTrue(response.getAllowedMethods() == response.getAllowedMethods());

    before();
    headers = new HashMap<>();
    when(connection.getHeaderFields()).thenReturn(headers);
    assertTrue(response.getAllowedMethods().isEmpty());
  }

  @Test
  public void getCookiesTest() throws IOException {
    preparegetEntity();
    assertTrue(response.getCookies().isEmpty());

    before();
    preparegetEntity();
    connection.getHeaderFields().put(HttpHeaders.SET_COOKIE,
        Arrays.asList(
            "REMEMBER=b5a3e046-c49c-45d8-a846-b601f85a5148;Path=/;"
                + "Domain=.netgiv.local;Expires=Tue, 10 Nov 2015 16:09:15 GMT",
            "S=2edd9b6a-1670-4c25-baef-8c07d014b477;Path=/;"
                + "Domain=.netgiv.local;Expires=Tue, 27 Oct 2015 16:39:15 GMT"));
    response = new ClientResponse(connection, builder);
    Map<String, NewCookie> cc = response.getCookies();
    assertTrue(response.getCookies() == response.getCookies());
    assertEquals(2, cc.size());
    NewCookie session = cc.get("S");
    NewCookie remember = cc.get("REMEMBER");

    assertNotNull(session);
    assertNotNull(remember);

    assertEquals("S", session.getName());
    assertEquals("REMEMBER", remember.getName());

    assertEquals("2edd9b6a-1670-4c25-baef-8c07d014b477", session.getValue());
    assertEquals("b5a3e046-c49c-45d8-a846-b601f85a5148", remember.getValue());

    assertEquals("/", session.getPath());
    assertEquals("/", remember.getPath());

    assertEquals(".netgiv.local", session.getDomain());
    assertEquals(".netgiv.local", remember.getDomain());

    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    cal.set(2015, 10, 10, 16, 9, 15);
    cal.set(Calendar.MILLISECOND, 0);

    assertEquals(cal.getTime(), remember.getExpiry());

    cal.set(2015, 9, 27, 16, 39, 15);
    assertEquals(cal.getTime(), session.getExpiry());
  }

  @Test
  public void getEntityTagTest() throws IOException {
    preparegetEntity();
    connection.getHeaderFields().put(HttpHeaders.ETAG, Arrays.asList("W/12345"));
    response = new ClientResponse(connection, builder);
    assertEquals(new EntityTag("12345", true), response.getEntityTag());
    assertTrue(response.getEntityTag() == response.getEntityTag());
  }

  @Test
  public void getDateTest() throws IOException {
    preparegetEntity();
    connection.getHeaderFields().put(HttpHeaders.DATE,
        Arrays.asList("Tue, 10 Nov 2015 16:09:15 GMT"));
    response = new ClientResponse(connection, builder);
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    cal.set(2015, 10, 10, 16, 9, 15);
    cal.set(Calendar.MILLISECOND, 0);
    assertEquals(cal.getTime(), response.getDate());
  }

  @Test
  public void getLastModifiedTest() throws IOException {
    preparegetEntity();
    connection.getHeaderFields().put(HttpHeaders.LAST_MODIFIED,
        Arrays.asList("Tue, 10 Nov 2015 16:09:15 GMT"));
    response = new ClientResponse(connection, builder);
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    cal.set(2015, 10, 10, 16, 9, 15);
    cal.set(Calendar.MILLISECOND, 0);
    assertEquals(cal.getTime(), response.getLastModified());
  }

  @Test
  public void getLocationTest() throws IOException, URISyntaxException {
    preparegetEntity();
    connection.getHeaderFields().put(HttpHeaders.LOCATION, Arrays.asList("http://hello.local"));
    response = new ClientResponse(connection, builder);
    assertEquals(new URI("http://hello.local"), response.getLocation());
  }

  @Test
  public void linksTest() throws IOException, URISyntaxException {
    preparegetEntity();
    assertTrue(response.getLinks().isEmpty());

    before();
    preparegetEntity();
    connection.getHeaderFields().put(HttpHeaders.LINK, Arrays
        .asList("<compact.css>; rel=\"stylesheet\"", "<full.css>; rel=\"alternate stylesheet\""));
    response = new ClientResponse(connection, builder);
    Set<Link> links = response.getLinks();
    assertEquals(2, links.size());
    assertTrue(response.hasLink("stylesheet"));
    assertTrue(response.hasLink("alternate stylesheet"));
    assertFalse(response.hasLink("alternate2 stylesheet"));
    assertTrue(links.contains(Link.fromUri("compact.css").rel("stylesheet").build()));
    assertTrue(links.contains(Link.fromUri("full.css").rel("alternate stylesheet").build()));

    assertNull(response.getLink("alternate2 stylesheet"));
    assertNull(response.getLinkBuilder("alternate2 stylesheet"));

    assertEquals(Link.fromUri("compact.css").rel("stylesheet").build(),
        response.getLink("stylesheet"));
    assertEquals(Link.fromUri("full.css").rel("alternate stylesheet").build(),
        response.getLink("alternate stylesheet"));
    assertEquals(Link.fromUri("compact.css").rel("sh").build(),
        response.getLinkBuilder("stylesheet").rel("sh").build());
  }

  @Test
  public void stringHeadersTest() throws IOException, URISyntaxException {
    Map<String, List<String>> headers = new HashMap<>();
    when(connection.getHeaderFields()).thenReturn(headers);
    connection.getHeaderFields().put("custom", Arrays.asList("hello", "world"));
    connection.getHeaderFields().put("custom2", Arrays.asList("hello"));
    response = new ClientResponse(connection, builder);
    assertTrue(response.getStringHeaders().containsKey("custom"));
    assertEquals("hello, world", response.getHeaderString("custom"));
    assertEquals("hello", response.getHeaderString("custom2"));
    assertNull(response.getHeaderString("hello"));
  }

}
