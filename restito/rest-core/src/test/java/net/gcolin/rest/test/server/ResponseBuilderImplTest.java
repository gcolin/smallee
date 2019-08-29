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

package net.gcolin.rest.test.server;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import net.gcolin.rest.server.AbstractResource;
import net.gcolin.rest.server.Contexts;
import net.gcolin.rest.server.ServerInvocationContext;
import net.gcolin.rest.server.ServerResponse;
import net.gcolin.rest.util.HttpHeader;
import net.gcolin.rest.util.lb.DateHeaderParamConverter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.net.URI;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;
import javax.ws.rs.ext.RuntimeDelegate;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ResponseBuilderImplTest {

  ServerInvocationContext context;

  @Before
  public void before() {
    context = Mockito.mock(ServerInvocationContext.class);
    Contexts.instance().set(context);
  }

  @Test
  public void cacheControlTest() {
    ResponseBuilder rb = newBuilder();
    CacheControl cc = new CacheControl();
    Response rr = rb.cacheControl(cc).build();
    assertEquals(
        RuntimeDelegate.getInstance().createHeaderDelegate(CacheControl.class).toString(cc),
        rr.getHeaderString(HttpHeader.CACHE_CONTROL));
  }

  @Test
  public void statusTest() {
    ResponseBuilder rb = newBuilder();
    Response rr = rb.status(200).build();
    assertEquals(200, rr.getStatus());
    assertEquals(Status.OK, rr.getStatusInfo());

    rb = newBuilder();
    rr = rb.status(299).build();
    assertEquals(299, rr.getStatus());

    rb = newBuilder();
    try {
      rb.status((StatusType) null);
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      // ok
    }

    rr = rb.status(new StatusType() {

      @Override
      public int getStatusCode() {
        return 299;
      }

      @Override
      public String getReasonPhrase() {
        return "";
      }

      @Override
      public Family getFamily() {
        return Family.OTHER;
      }
    }).build();
    assertEquals(299, rr.getStatus());

    rb = newBuilder();
    rr = rb.status(Status.ACCEPTED).build();
    assertEquals(202, rr.getStatus());
    assertEquals(Status.ACCEPTED, rr.getStatusInfo());
  }

  @Retention(RUNTIME)
  public static @interface Anntation1 {

  }

  @Retention(RUNTIME)
  public static @interface Anntation2 {

  }

  @Anntation1
  public static class A {

  }

  @Anntation2
  public static class B {

  }

  @Test
  public void entityTest() {
    AbstractResource resource = Mockito.mock(AbstractResource.class);
    Mockito.when(context.getResource()).thenReturn(resource);
    Mockito.when(resource.getAnnotations()).thenReturn(A.class.getAnnotations());

    ResponseBuilder rb = newBuilder();
    String entity = "hello";
    Response rr = rb.entity(entity).build();
    assertEquals(entity, rr.getEntity());
    Annotation[] aa = ((ServerResponse) rr).getAllAnnotations();
    assertEquals(1, aa.length);
    assertEquals(Anntation1.class, aa[0].annotationType());

    rb = newBuilder();
    rr = rb.entity(entity, B.class.getAnnotations()).build();
    assertEquals(entity, rr.getEntity());
    aa = ((ServerResponse) rr).getAllAnnotations();
    assertEquals(2, aa.length);
    assertEquals(Anntation1.class, aa[0].annotationType());
    assertEquals(Anntation2.class, aa[1].annotationType());

    Mockito.when(resource.getAnnotations()).thenReturn(new Annotation[0]);

    rb = newBuilder();
    rr = rb.entity(entity).build();
    assertEquals(entity, rr.getEntity());
    aa = ((ServerResponse) rr).getAllAnnotations();
    assertEquals(0, aa.length);

    rb = newBuilder();
    rr = rb.entity(entity, B.class.getAnnotations()).build();
    assertEquals(entity, rr.getEntity());
    aa = ((ServerResponse) rr).getAllAnnotations();
    assertEquals(1, aa.length);
    assertEquals(Anntation2.class, aa[0].annotationType());
  }

  @Test
  public void cloneTest() {
    ResponseBuilder rb = newBuilder();
    ResponseBuilder r2 = rb.clone();
    assertFalse(rb == r2);
  }

  @Test
  public void allowTest() {
    ResponseBuilder rb = newBuilder();
    rb.allow("GET");
    Set<String> set = rb.build().getAllowedMethods();
    assertEquals(1, set.size());
    assertTrue(set.contains("GET"));

    rb = newBuilder();
    rb.allow("GET").allow((String[]) null);
    set = rb.build().getAllowedMethods();
    assertTrue(set.isEmpty());

    rb = newBuilder();
    rb.allow("GET").allow((String) null);
    set = rb.build().getAllowedMethods();
    assertTrue(set.isEmpty());

    rb = newBuilder();
    rb.allow("GET").allow(new HashSet<>());
    set = rb.build().getAllowedMethods();
    assertTrue(set.isEmpty());

    rb = newBuilder();
    rb.allow("GET").allow((Set<String>) null);
    set = rb.build().getAllowedMethods();
    assertTrue(set.isEmpty());

    rb = newBuilder();
    rb.allow();
    set = rb.build().getAllowedMethods();
    assertTrue(set.isEmpty());

    rb = newBuilder();
    rb.allow(new HashSet<>(Arrays.asList("GET", "POST")));
    set = rb.build().getAllowedMethods();
    assertEquals(2, set.size());
    assertTrue(set.contains("GET"));
    assertTrue(set.contains("POST"));
  }

  @Test
  public void encodingTest() {
    ResponseBuilder rb = newBuilder();
    rb.encoding("utf8");
    assertEquals("utf8", rb.build().getHeaderString(HttpHeaders.CONTENT_ENCODING));

    rb = newBuilder();
    rb.encoding("utf8").encoding(null);
    assertNull(rb.build().getHeaderString(HttpHeaders.CONTENT_ENCODING));
  }

  @Test
  public void headerTest() {
    ResponseBuilder rb = newBuilder();
    rb.header("hello", "world");
    assertEquals("world", rb.build().getHeaderString("hello"));

    rb = newBuilder();
    rb.header("hello", "world");
    rb.header("hello", "world2");
    assertEquals("world, world2", rb.build().getHeaderString("hello"));

    rb = newBuilder();
    rb.header("hello", "world");
    rb.header("hello", null);
    assertNull(rb.build().getHeaderString("hello"));
  }

  @Test
  public void replaceAllTest() {
    ResponseBuilder rb = newBuilder();
    rb.header("hello", "world");
    rb.replaceAll(new MultivaluedHashMap<>());
    assertNull(rb.build().getHeaderString("hello"));

    rb = newBuilder();
    MultivaluedHashMap<String, Object> map = new MultivaluedHashMap<>();
    map.add("hello", "world");
    map.add("hello", "world2");
    rb.replaceAll(map).replaceAll(null);
    assertEquals("world, world2", rb.build().getHeaderString("hello"));
  }

  @Test
  public void languageTest() {
    ResponseBuilder rb = newBuilder();
    rb.language(Locale.CANADA);
    assertEquals(Locale.CANADA, rb.build().getLanguage());

    rb = newBuilder();
    rb.language(Locale.CANADA).language((Locale) null);
    assertNull(rb.build().getLanguage());

    rb = newBuilder();
    rb.language("fr");
    assertEquals(Locale.FRENCH, rb.build().getLanguage());

    rb = newBuilder();
    rb.language("fr").language((String) null);
    assertNull(rb.build().getLanguage());
  }

  @Test
  public void typeTest() {
    ResponseBuilder rb = newBuilder();
    rb.type(MediaType.APPLICATION_ATOM_XML_TYPE);
    assertEquals(MediaType.APPLICATION_ATOM_XML_TYPE, rb.build().getMediaType());

    rb = newBuilder();
    rb.type(MediaType.APPLICATION_ATOM_XML_TYPE).type((MediaType) null);
    assertNull(rb.build().getMediaType());

    rb = newBuilder();
    rb.type(MediaType.APPLICATION_ATOM_XML);
    assertEquals(MediaType.APPLICATION_ATOM_XML_TYPE, rb.build().getMediaType());

    rb = newBuilder();
    rb.type(MediaType.APPLICATION_ATOM_XML).type((String) null);
    assertNull(rb.build().getMediaType());
  }

  @Test
  public void variantTest() {
    Variant var = new Variant(MediaType.APPLICATION_ATOM_XML_TYPE, Locale.CHINA, "gzip");
    ResponseBuilder rb = newBuilder();
    rb.variant(var);
    Response rr = rb.build();
    assertEquals(MediaType.APPLICATION_ATOM_XML_TYPE, rr.getMediaType());
    assertEquals(Locale.CHINA, rr.getLanguage());
    assertEquals("gzip", rr.getHeaderString(HttpHeaders.CONTENT_ENCODING));

    rb = newBuilder();
    rb.variant(var).variant(null);
    rr = rb.build();
    assertNull(rr.getMediaType());
    assertNull(rr.getLanguage());
    assertNull(rr.getHeaderString(HttpHeaders.CONTENT_ENCODING));
  }

  @Test
  public void contentlocationTest() {
    ResponseBuilder rb = newBuilder();
    rb.contentLocation(URI.create("http://hello.local"));
    assertEquals("http://hello.local", rb.build().getHeaderString(HttpHeaders.CONTENT_LOCATION));

    rb = newBuilder();
    rb.contentLocation(URI.create("hello.txt")).contentLocation(null);
    assertNull(rb.build().getHeaderString(HttpHeaders.CONTENT_LOCATION));

  }

  @Test
  public void locationTest() {
    ResponseBuilder rb = newBuilder();
    rb.location(URI.create("http://hello.local"));
    assertEquals("http://hello.local", rb.build().getHeaderString(HttpHeaders.LOCATION));

    rb = newBuilder();
    rb.location(URI.create("http://hello.local")).location(null);
    assertNull(rb.build().getHeaderString(HttpHeaders.LOCATION));

    UriInfo info = Mockito.mock(UriInfo.class);
    Mockito.when(info.getBaseUri()).thenReturn(URI.create("http://hello.local"));
    Mockito.when(context.getUriInfo()).thenReturn(info);
    rb = newBuilder();
    rb.location(URI.create("/index.html"));
    assertEquals("http://hello.local/index.html", rb.build().getHeaderString(HttpHeaders.LOCATION));
  }

  @Test
  public void cookieTest() {
    NewCookie c1 = new NewCookie("a", "hello");
    ResponseBuilder rb = newBuilder();
    rb.cookie(c1);
    Map<String, NewCookie> cookies = rb.build().getCookies();
    assertEquals(1, cookies.size());
    assertEquals(c1, cookies.get("a"));

    rb = newBuilder();
    NewCookie c2 = new NewCookie("b", "world");
    rb.cookie(c1, c2);
    cookies = rb.build().getCookies();
    assertEquals(2, cookies.size());
    assertEquals(c1, cookies.get("a"));
    assertEquals(c2, cookies.get("b"));

    rb = newBuilder();
    rb.cookie(c1, c2).cookie((NewCookie[]) null);
    cookies = rb.build().getCookies();
    assertTrue(cookies.isEmpty());
  }

  @Test
  public void expiresTest() {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MILLISECOND, 0);
    Date date = cal.getTime();

    ResponseBuilder rb = newBuilder();
    rb.expires(date);
    assertEquals(date,
        new DateHeaderParamConverter().fromString(rb.build().getHeaderString(HttpHeaders.EXPIRES)));

    rb = newBuilder();
    rb.expires(date).expires(null);
    assertNull(rb.build().getHeaderString(HttpHeaders.EXPIRES));
  }

  @Test
  public void lastModifiedTest() {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.MILLISECOND, 0);
    Date date = cal.getTime();

    ResponseBuilder rb = newBuilder();
    rb.lastModified(date);
    assertEquals(date, new DateHeaderParamConverter()
        .fromString(rb.build().getHeaderString(HttpHeaders.LAST_MODIFIED)));

    rb = newBuilder();
    rb.lastModified(date).lastModified(null);
    assertNull(rb.build().getHeaderString(HttpHeaders.LAST_MODIFIED));
  }

  @Test
  public void tagTest() {
    EntityTag tag = new EntityTag("strong");
    EntityTag tag2 = new EntityTag("weak", true);
    ResponseBuilder rb = newBuilder();
    rb.tag(tag2);
    assertEquals(tag2, rb.build().getEntityTag());

    rb = newBuilder();
    rb.tag("strong");
    assertEquals(tag, rb.build().getEntityTag());

    rb = newBuilder();
    rb.tag("strong").tag((String) null);
    assertNull(rb.build().getEntityTag());
  }

  @Test
  public void linkTest() {
    Link l1 = Link.fromUri("hello.txt").rel("a").build();
    ResponseBuilder rb = newBuilder();
    rb.link("hello.txt", "a");
    assertEquals(l1, rb.build().getLink("a"));

    rb = newBuilder();
    rb.link(URI.create("hello.txt"), "a");
    assertEquals(l1, rb.build().getLink("a"));

    rb = newBuilder();
    Link l2 = Link.fromUri("hello2.txt").rel("b").build();
    rb.links(l1, l2);
    assertEquals(l1, rb.build().getLink("a"));
    assertEquals(l2, rb.build().getLink("b"));

    rb = newBuilder();
    rb.links((Link[]) null);
    assertTrue(rb.build().getLinks().isEmpty());
  }

  @Test
  public void variantsTest() {
    Variant var = new Variant(MediaType.APPLICATION_ATOM_XML_TYPE, Locale.CHINA, null);
    
    ResponseBuilder rb = newBuilder();
    assertNull(rb.variants(var).build().getHeaderString(HttpHeaders.VARY));

    rb = newBuilder();
    Variant var1 = new Variant(MediaType.APPLICATION_JSON_TYPE, Locale.CHINA, null);
    String vary = rb.variants(var, var1).build().getHeaderString(HttpHeaders.VARY);
    assertEquals("Accept", vary);

    rb = newBuilder();
    Variant var2 = new Variant(MediaType.APPLICATION_ATOM_XML_TYPE, Locale.FRENCH, "deflate");
    vary = rb.variants(var, var1, var2).build().getHeaderString(HttpHeaders.VARY);
    assertEquals("Accept,Accept-Language,Accept-Encoding", vary);

    rb = newBuilder();
    vary =
        rb.variants(var, var1).variants((Variant[]) null).build().getHeaderString(HttpHeaders.VARY);
    assertNull(vary);

    rb = newBuilder();
    vary = rb.variants(var, var1).variants().build().getHeaderString(HttpHeaders.VARY);
    assertEquals("Accept", vary);
  }

  private ResponseBuilder newBuilder() {
    return RuntimeDelegate.getInstance().createResponseBuilder();
  }

}
