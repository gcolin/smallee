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

package net.gcolin.rest.test.util;

import net.gcolin.rest.LinkBuilder;
import net.gcolin.rest.UriBuilderImpl;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.Link.Builder;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class LinkBuilderTest {

  @Test
  public void linkTest() throws URISyntaxException {
    Builder lb = new LinkBuilder().link("<compact.css> ; title=\"compact\"; type=\"text/css\"");
    Link link = lb.build();
    Assert.assertEquals(new URI("compact.css"), link.getUri());
    Assert.assertEquals("compact", link.getTitle());
    Assert.assertEquals("text/css", link.getType());
  }

  @Test
  public void uriTest() throws URISyntaxException {
    Link link = new LinkBuilder().uri("compact.css").build();
    Assert.assertEquals(new URI("compact.css"), link.getUri());
    link = new LinkBuilder().uri(new URI("compact.css")).build();
    Assert.assertEquals(new URI("compact.css"), link.getUri());
    Assert.assertEquals(link.getUri(), link.getUriBuilder().build());
    Assert.assertEquals("<compact.css> ", link.toString());
    Assert.assertTrue(link.equals(link));
    Assert.assertFalse(link.equals(null));
    Assert.assertFalse(link.equals(new LinkBuilder().uri("compact2.css").build()));
    Assert.assertFalse(link.equals(new LinkBuilder().uri("compact.css").rel("hello").build()));
  }

  @Test
  public void baseuriTest() throws URISyntaxException {
    Link link = new LinkBuilder().uri("compact.css").baseUri("http://netgiv.local/").build();
    Assert.assertEquals(new URI("http://netgiv.local/compact.css"), link.getUri());

    link = new LinkBuilder().uri("http://hello.local/compact.css").baseUri("http://netgiv.local/")
        .build();
    Assert.assertEquals(new URI("http://hello.local/compact.css"), link.getUri());
  }

  @Test
  public void uriBuilderTest() throws URISyntaxException {
    Link link = new LinkBuilder().uriBuilder(new UriBuilderImpl().uri("compact.css")).build();
    Assert.assertEquals(new URI("compact.css"), link.getUri());
  }

  @Test
  public void relTest() throws URISyntaxException {
    Link link = new LinkBuilder().uri("compact.css").rel("hello").build();
    Assert.assertEquals(new URI("compact.css"), link.getUri());
    Assert.assertEquals("hello", link.getRel());
  }

  @Test
  public void titleTest() throws URISyntaxException {
    Link link = new LinkBuilder().uri("compact.css").title("hello").build();
    Assert.assertEquals(new URI("compact.css"), link.getUri());
    Assert.assertEquals("hello", link.getTitle());
  }

  @Test
  public void typeTest() throws URISyntaxException {
    Link link = new LinkBuilder().uri("compact.css").type("hello").build();
    Assert.assertEquals(new URI("compact.css"), link.getUri());
    Assert.assertEquals("hello", link.getType());
  }

  @Test
  public void paramTest() throws URISyntaxException {
    Link link = new LinkBuilder().uri("compact.css").param("hello", "world").build();
    Assert.assertEquals(new URI("compact.css"), link.getUri());
    Assert.assertEquals("world", link.getParams().get("hello"));
  }

  @Test
  public void buildTest() throws URISyntaxException {
    Link link = new LinkBuilder().uri("/api/{name}").build("hello");
    Assert.assertEquals(new URI("/api/hello"), link.getUri());
  }

  @Test
  public void buildRelativizedTest() throws URISyntaxException {
    Link link = new LinkBuilder().uri("http://hello.local/api/{name}")
        .buildRelativized(new URI("http://hello.local/"), "hello");
    Assert.assertEquals(new URI("api/hello"), link.getUri());
  }

}
