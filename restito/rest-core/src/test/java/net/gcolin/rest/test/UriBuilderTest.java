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

package net.gcolin.rest.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import net.gcolin.common.lang.Strings;
import net.gcolin.rest.UriBuilderImpl;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class UriBuilderTest {

  @Test
  public void testReplaceNonAsciiQueryParam()
      throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
    URL url = new URL("http://example.com/getMyName?néme=t");
    String query = url.getQuery();

    UriBuilder builder = UriBuilder.fromPath(url.getPath()).scheme(url.getProtocol())
        .host(url.getHost()).port(url.getPort()).replaceQuery(query).fragment(url.getRef());

    // Replace QueryParam.
    String parmName = "néme";
    String value = "value";

    builder.replaceQueryParam(parmName, value);

    final URI result = builder.build();
    final URI expected = new URI("http://example.com/getMyName?néme=value");
    assertEquals(expected.toASCIIString(), result.toASCIIString());
  }

  @Test
  // See JAX_RS_SPEC-245
  public void testReplacingUserInfo() {
    final String userInfo = "foo:foo";

    URI uri;
    uri = UriBuilder.fromUri("http://foo2:foo2@localhost:8080").userInfo(userInfo).build();
    assertEquals(userInfo, uri.getRawUserInfo());

    uri = UriBuilder.fromUri("http://localhost:8080").userInfo(userInfo).build();
    assertEquals(userInfo, uri.getRawUserInfo());
  }

  // Reproducer for JERSEY-1800
  @Test
  public void testEmptyUriString() throws URISyntaxException {
    final URI uri = URI.create("");
    UriBuilder ub = new UriBuilderImpl().uri("news:comp.lang.java").uri(uri);
    assertEquals("news:", ub.toTemplate());
    // note that even though the URI is valid according to RFC 3986,
    // it is not possible to create a java.net.URI from this builder if SSP is empty

    ub = new UriBuilderImpl().uri("news:comp.lang.java").uri("");
    assertEquals("news:", ub.toTemplate());
    // note that even though the URI is valid according to RFC 3986,
    // it is not possible to create a java.net.URI from this builder if SSP is empty
  }

  @Test
  public void testToTemplate() throws URISyntaxException {
    UriBuilder ub =
        new UriBuilderImpl().uri(new URI("http://examples.jersey.java.net/")).userInfo("{T1}")
            .path("{T2}").segment("{T3}").queryParam("a", "{T4}", "v1").queryParam("b", "v2");
    assertEquals("http://{T1}@examples.jersey.java.net/{T2}/{T3}?a={T4}&a=v1&b=v2",
        ub.toTemplate());

    ub.queryParam("a", "v3").queryParam("c", "v4");
    assertEquals("http://{T1}@examples.jersey.java.net/{T2}/{T3}?a={T4}&a=v1&b=v2&a=v3&c=v4",
        ub.toTemplate());
  }

  @Test
  @Ignore
  public void testPathTemplateValueEncoding() throws URISyntaxException {
    String result;
    result = new UriBuilderImpl().uri(new URI("http://examples.jersey.java.net/")).userInfo("a/b")
        .path("a/b").segment("a/b").build().toString();
    assertEquals("http://a%2Fb@examples.jersey.java.net/a/b/a%2Fb", result);

    result = new UriBuilderImpl().uri(new URI("http://examples.jersey.java.net/")).userInfo("{T1}")
        .path("{T2}").segment("{T3}").build("a/b", "a/b", "a/b").toString();
    assertEquals("http://a%2Fb@examples.jersey.java.net/a%2Fb/a%2Fb", result);

    result = new UriBuilderImpl().uri(new URI("http://examples.jersey.java.net/")).userInfo("{T1}")
        .path("{T2}").segment("{T2}").build("a@b", "a@b").toString();
    assertEquals("http://a%40b@examples.jersey.java.net/a@b/a@b", result);

    result = new UriBuilderImpl().uri(new URI("http://examples.jersey.java.net/")).userInfo("{T}")
        .path("{T}").segment("{T}").build("a@b").toString();
    assertEquals("http://a%40b@examples.jersey.java.net/a@b/a@b", result);
  }

  @Test
  @Ignore
  public void testReplaceMatrixParamWithNull() {
    UriBuilder builder = new UriBuilderImpl().matrixParam("matrix", "param1", "param2");
    builder.replaceMatrixParam("matrix", (Object[]) null);
    assertEquals(builder.build().toString(), "");
  }

  // for completeness (added along with regression tests for JERSEY-1114)
  @Test
  public void testBuildNoSlashUri() {
    UriBuilder builder = new UriBuilderImpl().uri(URI.create("http://localhost:8080")).path("test");
    assertEquals("http://localhost:8080/test", builder.build().toString());
  }

  // regression test for JERSEY-1114
  @Test
  public void testBuildFromMapNoSlashInUri() {
    UriBuilder builder = new UriBuilderImpl().uri(URI.create("http://localhost:8080")).path("test");
    assertEquals("http://localhost:8080/test",
        builder.buildFromMap(new HashMap<String, Object>()).toString());
  }

  // regression test for JERSEY-1114
  @Test
  public void testBuildFromArrayNoSlashInUri() {
    UriBuilder builder = new UriBuilderImpl().uri(URI.create("http://localhost:8080")).path("test");
    assertEquals("http://localhost:8080/test", builder.build("testing").toString());
  }

  @Test
  @Ignore
  public void testReplaceNullMatrixParam() {
    try {
      new UriBuilderImpl().replaceMatrixParam(null, "param");
    } catch (IllegalArgumentException ex) {
      return;
    } catch (Exception ex) {
      fail("Expected IllegalArgumentException but got " + ex.toString());
    }
    fail("Expected IllegalArgumentException but no exception was thrown.");
  }

  // regression test for JERSEY-1081
  @Test
  public void testReplaceQueryParam() {
    URI uri =
        new UriBuilderImpl().path("http://localhost/").replaceQueryParam("foo", "test").build();
    assertEquals("http://localhost/?foo=test", uri.toString());
  }

  // regression test for JERSEY-1081
  @Test
  public void testReplaceQueryParamAndClone() {
    URI uri = new UriBuilderImpl().path("http://localhost/").replaceQueryParam("foo", "test")
        .clone().build();
    assertEquals("http://localhost/?foo=test", uri.toString());
  }

  // regression test for JERSEY-1341
  @Test
  public void testEmptyQueryParamValue() {
    URI uri = new UriBuilderImpl().path("http://localhost/").queryParam("test", "").build();
    assertEquals("http://localhost/?test=", uri.toString());
  }

  // regression test for JERSEY-1457
  @Test
  public void testChangeSspViaStringUriTemplate() throws Exception {
    String[] origUris = new String[] {"news:comp.lang.java", "tel:+1-816-555-1212"};
    URI[] replaceUris =
        new URI[] {new URI(null, "news.lang.java", null), new URI(null, "+1-866-555-1212", null)};
    String[] results = new String[] {"news:news.lang.java", "tel:+1-866-555-1212"};
    int idx = 0;
    while (idx < origUris.length) {
      assertEquals(results[idx], UriBuilder.fromUri(new URI(origUris[idx]))
          .uri(replaceUris[idx].toASCIIString()).build().toString());
      idx++;
    }
  }

  @Test
  public void testChangeUriStringAfterChangingOpaqueSchemeToHttp() {
    assertEquals("http://www.example.org/test", UriBuilder.fromUri("tel:+1-816-555-1212")
        .scheme("http").uri("//www.{host}.org").path("test").build("example").toString());
  }

  @Test
  public void testUriBuilderTemplatesSimple() {
    testUri("a:/path");
    testUri("a:/p");
    testUri("a:/path/x/y/z");
    testUri("a:/path/x?q=12#fragment");
    testUri("a:/p?q#f");
    testUri("a://host");
    testUri("a://host:5555/a/b");
    testUri("a://h:5/a/b");
    testUri("a:/user@host:12345"); // user@host:12345 is not authority but path
    testUri("a:/user@host:12345/a/b/c");
    testUri("a:/user@host:12345/a/b/c?aaa&bbb#ccc");
    testUri("a:/user@host.hhh.ddd.c:12345/a/b/c?aaa&bbb#ccc");
    testUri("/a");
    testUri("/a/../../b/c/d");
    testUri("//localhost:80/a/b");
    testUri("//l:8/a/b");
    testUri("a/b");
    testUri("a");
    testUri("../../s");
    testUri("mailto:test@test.com");
    testUri("http://orac@le:co@m:1234/a/b/ccc?a#fr");
    testUri("http://[::FFFF:129.144.52.38]:1234/a/b/ccc?a#fr");
    testUri("http://[FEDC:BA98:7654:3210:FEDC:BA98:7654:3210]:1234/a/b/ccc?a#fr");

  }

  @Test
  @Ignore
  public void failingTests() {
    testUri("a://#fragment"); // fails in UriBuilderImpl
    testUri("a://?query");


    // fails: opaque uris are not supported by UriTemplate
    URI uri = new UriBuilderImpl().uri("{scheme}://{mailto}").build("mailto", "email@test.ttt");
    assertEquals("mailto:email@test.ttt", uri.toString());
  }

  @Test
  public void testUriBuilderTemplates() {
    URI uri = new UriBuilderImpl().uri("http://localhost:8080/{path}").build("a/b/c");
    assertEquals("http://localhost:8080/a%2Fb%2Fc", uri.toString());

    uri = new UriBuilderImpl().uri("{scheme}://{host}").build("http", "localhost");
    assertEquals("http://localhost", uri.toString());


    uri = new UriBuilderImpl().uri("http://{host}:8080/{path}").build("l", "a/b/c");
    assertEquals("http://l:8080/a%2Fb%2Fc", uri.toString());

    uri = new UriBuilderImpl().uri("{scheme}://{host}:{port}/{path}").build("s", "h",
        Integer.valueOf(1), "a");
    assertEquals("s://h:1/a", uri.toString());

    Map<String, Object> values = new HashMap<String, Object>();
    values.put("scheme", "s");
    values.put("host", "h");
    values.put("port", 1);
    values.put("path", "p/p");
    values.put("query", "q");
    values.put("fragment", "f");

    uri = new UriBuilderImpl().uri("{scheme}://{host}:{port}/{path}?{query}#{fragment}")
        .buildFromMap(values);
    assertEquals("s://h:1/p%2Fp?q#f", uri.toString());


    uri = new UriBuilderImpl().uri("{scheme}://{host}:{port}/{path}/{path2}").build("s", "h",
        Integer.valueOf(1), "a", "b");
    assertEquals("s://h:1/a/b", uri.toString());


    uri = new UriBuilderImpl().uri("{scheme}://{host}:{port}/{path}/{path2}").build("s", "h",
        Integer.valueOf(1), "a", "b");
    assertEquals("s://h:1/a/b", uri.toString());

    uri = new UriBuilderImpl().uri("//{host}:{port}/{path}/{path2}").build("h", Integer.valueOf(1),
        "a", "b");
    assertEquals("//h:1/a/b", uri.toString());


    uri = new UriBuilderImpl().uri("/{a}/{a}/{b}").build("a", "b");
    assertEquals("/a/a/b", uri.toString());

    uri = new UriBuilderImpl().uri("/{a}/{a}/{b}?{queryParam}").build("a", "b", "query");
    assertEquals("/a/a/b?query", uri.toString());

    // partial templates
    uri = new UriBuilderImpl().uri("/{a}xx/{a}/{b}?{queryParam}").build("a", "b", "query");
    assertEquals("/axx/a/b?query", uri.toString());

    uri = new UriBuilderImpl().uri("my{scheme}://my{host}:1{port}/my{path}/my{path2}").build("s",
        "h", Integer.valueOf(1), "a", "b/c");
    assertEquals("mys://myh:11/mya/myb%2Fc", uri.toString());

    uri = new UriBuilderImpl()
        .uri("my{scheme}post://my{host}post:5{port}9/my{path}post/my{path2}post")
        .build("s", "h", Integer.valueOf(1), "a", "b");
    assertEquals("myspost://myhpost:519/myapost/mybpost", uri.toString());
  }

  @Test
  public void testUriBuilderTemplatesNotEncodedSlash() {
    URI uri = new UriBuilderImpl().uri("http://localhost:8080/{path}").build(new Object[] {"a/b/c"},
        false);
    assertEquals("http://localhost:8080/a/b/c", uri.toString());

    uri = new UriBuilderImpl().uri("http://{host}:8080/{path}").build(new Object[] {"l", "a/b/c"},
        false);
    assertEquals("http://l:8080/a/b/c", uri.toString());

    Map<String, Object> values = new HashMap<String, Object>();
    values.put("scheme", "s");
    values.put("host", "h");
    values.put("port", 1);
    values.put("path", "p/p");
    values.put("query", "q");
    values.put("fragment", "f");

    uri = new UriBuilderImpl().uri("{scheme}://{host}:{port}/{path}?{query}#{fragment}")
        .buildFromMap(values, false);
    assertEquals("s://h:1/p/p?q#f", uri.toString());
  }

  private void testUri(String input) {
    URI uri = new UriBuilderImpl().uri(input).clone().build();

    URI originalUri = URI.create(input);
    assertEquals(originalUri.getScheme(), uri.getScheme());
    assertEquals(originalUri.getHost(), uri.getHost());
    assertEquals(originalUri.getPort(), uri.getPort());
    assertEquals(originalUri.getUserInfo(), uri.getUserInfo());
    assertEquals(originalUri.getPath(), uri.getPath());
    assertEquals(originalUri.getQuery(), uri.getQuery());
    assertEquals(originalUri.getFragment(), uri.getFragment());
    assertEquals(originalUri.getRawSchemeSpecificPart(), uri.getRawSchemeSpecificPart());
    assertEquals(originalUri.isAbsolute(), uri.isAbsolute());
    assertEquals(input, uri.toString());
  }


  @org.junit.Test
  public void testOpaqueUri() {
    URI uri = UriBuilder.fromUri("mailto:a@b").build();
    Assert.assertEquals("mailto:a@b", uri.toString());
  }


  @Test
  public void testOpaqueUriReplaceSchemeSpecificPart() {
    URI uri = UriBuilder.fromUri("mailto:a@b").schemeSpecificPart("c@d").build();
    Assert.assertEquals("mailto:c@d", uri.toString());
  }

  @Test
  public void testOpaqueReplaceUri() {
    URI uri = UriBuilder.fromUri("mailto:a@b").uri(URI.create("c@d")).build();
    Assert.assertEquals("mailto:c@d", uri.toString());
  }

  @Test
  public void testReplaceScheme() {
    URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").scheme("https").build();
    Assert.assertEquals("https://localhost:8080/a/b/c", uri.toString());

    uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").scheme(null).build();
    Assert.assertEquals("//localhost:8080/a/b/c", uri.toString());

    uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").scheme(null).host(null).build();
    Assert.assertEquals("//:8080/a/b/c", uri.toString());

    uri =
        UriBuilder.fromUri("http://localhost:8080/a/b/c").scheme(null).host(null).port(-1).build();
    Assert.assertEquals("/a/b/c", uri.toString());
  }

  @Test
  public void testReplaceSchemeSpecificPart() {
    URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c")
        .schemeSpecificPart("//localhost:8080/a/b/c/d").build();
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/d"), uri);
  }

  @Test
  public void testNameAuthorityUri() {
    URI uri = UriBuilder.fromUri("http://x_y/a/b/c").build();
    Assert.assertEquals(URI.create("http://x_y/a/b/c"), uri);
  }

  @Test
  public void testReplaceNameAuthorityUriWithHost() {
    URI uri = UriBuilder.fromUri("http://x_y.com/a/b/c").host("xy.com").build();
    Assert.assertEquals(URI.create("http://xy.com/a/b/c"), uri);
  }

  @Test
  public void testReplaceNameAuthorityUriWithSsp() {
    URI uri =
        UriBuilder.fromUri("http://x_y.com/a/b/c").schemeSpecificPart("//xy.com/a/b/c").build();
    Assert.assertEquals(URI.create("http://xy.com/a/b/c"), uri);

    uri = UriBuilder.fromUri("http://x_y.com/a/b/c").schemeSpecificPart("//v_w.com/a/b/c").build();
    Assert.assertEquals(URI.create("http://v_w.com/a/b/c"), uri);
  }

  @Test
  public void testReplaceUserInfo() {
    URI uri = UriBuilder.fromUri("http://bob@localhost:8080/a/b/c").userInfo("sue").build();
    Assert.assertEquals(URI.create("http://sue@localhost:8080/a/b/c"), uri);
  }

  @Test
  public void testReplaceHost() {
    URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").host("a.com").build();
    Assert.assertEquals(URI.create("http://a.com:8080/a/b/c"), uri);

    uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").host("[::FFFF:129.144.52.38]").build();
    Assert.assertEquals(URI.create("http://[::FFFF:129.144.52.38]:8080/a/b/c"), uri);
  }

  @Test
  public void testReplacePort() {
    URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").port(9090).build();
    Assert.assertEquals(URI.create("http://localhost:9090/a/b/c"), uri);

    uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").port(-1).build();
    Assert.assertEquals(URI.create("http://localhost/a/b/c"), uri);
  }

  @Test
  public void testReplacePath() {
    URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").replacePath("/x/y/z").build();
    Assert.assertEquals(URI.create("http://localhost:8080/x/y/z"), uri);
  }

  @Test
  public void testReplacePathNull() {
    URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").replacePath(null).build();

    Assert.assertEquals(URI.create("http://localhost:8080"), uri);
  }

  @Test
  @Ignore
  public void testReplaceMatrix() {
    URI uri =
        UriBuilder.fromUri("http://localhost:8080/a/b/c;a=x;b=y").replaceMatrix("x=a;y=b").build();
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c;x=a;y=b"), uri);
  }


  @Test
  @Ignore
  public void testReplaceMatrixParamsEncoded() throws URISyntaxException {
    UriBuilder ubu =
        UriBuilder.fromUri("http://localhost/").replaceMatrix("limit=10;sql=select+*+from+users");
    ubu.replaceMatrixParam("limit", 100);

    URI uri = ubu.build();
    Assert.assertEquals(URI.create("http://localhost/;limit=100;sql=select+*+from+users"), uri);
  }

  @Test
  @Ignore
  public void testMatrixParamsWithTheSameName() {
    UriBuilder first =
        UriBuilder.fromUri("http://www.com/").replaceMatrixParam("example", "one", "two");
    first = first.path("/child");
    first = first.replaceMatrixParam("example", "another");

    Assert.assertEquals("http://www.com/;example=one;example=two/child;example=another",
        first.build().toString());
  }

  @Test
  @Ignore
  public void testMatrixParamsWithTheDifferentName() {
    UriBuilder first =
        UriBuilder.fromUri("http://www.com/").replaceMatrixParam("example", "one", "two");
    first = first.path("/child");
    first = first.replaceMatrixParam("other", "another");

    Assert.assertEquals("http://www.com/;example=one;example=two/child;other=another",
        first.build().toString());
  }

  @Test
  public void testReplaceQuery() {
    URI uri =
        UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").replaceQuery("x=a&y=b").build();
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c?x=a&y=b"), uri);
  }

  @Test
  @Ignore
  public void testBuildEncodedQuery() {
    URI uu = UriBuilder.fromPath("").queryParam("y", "1 %2B 2").build();
    Assert.assertEquals(URI.create("?y=1+%2B+2"), uu);

    // Issue 216
    uu = UriBuilder.fromPath("http://localhost:8080").path("/{x}/{y}/{z}/{x}")
        .buildFromEncoded("%xy", " ", "=");
    Assert.assertEquals(URI.create("http://localhost:8080/%25xy/%20/=/%25xy"), uu);
  }

  @Test
  public void testReplaceQueryParams() {
    UriBuilder ubu = UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y")
        .replaceQueryParam("a", "z", "zz").queryParam("c", "c");

    {
      URI uri = ubu.build();

      MultivaluedMap<String, String> qps = decodeQuery(uri);
      List<String> lista = qps.get("a");
      Assert.assertEquals(2, lista.size());
      Assert.assertEquals("z", lista.get(0));
      Assert.assertEquals("zz", lista.get(1));
      List<String> listb = qps.get("b");
      Assert.assertEquals(1, listb.size());
      Assert.assertEquals("y", listb.get(0));
      List<String> listc = qps.get("c");
      Assert.assertEquals(1, listc.size());
      Assert.assertEquals("c", listc.get(0));
    }

    {
      URI uri = ubu.replaceQueryParam("a", "_z_", "_zz_").build();

      MultivaluedMap<String, String> qps = decodeQuery(uri);
      List<String> lista = qps.get("a");
      Assert.assertEquals(2, lista.size());
      Assert.assertEquals("_z_", lista.get(0));
      Assert.assertEquals("_zz_", lista.get(1));
      List<String> listb = qps.get("b");
      Assert.assertEquals(1, listb.size());
      Assert.assertEquals("y", listb.get(0));
      List<String> listc = qps.get("c");
      Assert.assertEquals(1, listc.size());
      Assert.assertEquals("c", listc.get(0));
    }

    // issue 257 - param is removed after setting it to null
    {
      URI u1 = UriBuilder.fromPath("http://localhost:8080").queryParam("x", "10")
          .replaceQueryParam("x", (Object[]) null).build();
      Assert.assertTrue(u1.toString().equals("http://localhost:8080"));

      URI u2 = UriBuilder.fromPath("http://localhost:8080").queryParam("x", "10")
          .replaceQueryParam("x").build();
      Assert.assertTrue(u2.toString().equals("http://localhost:8080"));
    }

    // issue 257 - IllegalArgumentException
    {
      boolean caught = false;

      try {
        UriBuilder.fromPath("http://localhost:8080").queryParam("x", "10")
            .replaceQueryParam("x", "1", null, "2").build();
      } catch (IllegalArgumentException iae) {
        caught = true;
      }

      Assert.assertTrue(caught);
    }

  }

  private MultivaluedMap<String, String> decodeQuery(URI uri) {
    MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
    String query = uri.getQuery();
    if (query != null) {
      for (String part : query.split("&")) {
        String[] parts = part.split("=");
        List<String> values = map.get(parts[0]);
        if (values == null) {
          values = new ArrayList<>();
          map.put(parts[0], values);
        }
        values.add(Strings.decodeUrl(parts[1]));
      }
    }
    return map;
  }

  @Test
  public void testReplaceQueryParamsEmpty() {
    UriBuilder ubu = UriBuilder.fromUri("http://localhost:8080/a/b/c")
        .replaceQueryParam("a", "z", "zz").queryParam("c", "c");

    {
      URI uri = ubu.build();

      MultivaluedMap<String, String> qps = decodeQuery(uri);
      List<String> lista = qps.get("a");
      Assert.assertEquals(2, lista.size());
      Assert.assertEquals("z", lista.get(0));
      Assert.assertEquals("zz", lista.get(1));
      List<String> listc = qps.get("c");
      Assert.assertEquals(1, listc.size());
      Assert.assertEquals("c", listc.get(0));
    }
  }

  @Test
  public void testReplaceQueryParamsEncoded1() throws URISyntaxException {
    UriBuilder ubu = UriBuilder.fromUri(new URI("http://localhost/"))
        .replaceQuery("limit=10&sql=select+*+from+users");
    ubu.replaceQueryParam("limit", 100);

    URI uri = ubu.build();
    Assert.assertEquals(URI.create("http://localhost/?sql=select+*+from+users&limit=100"), uri);
  }

  @Test
  public void testReplaceQueryParamsEncoded2() throws URISyntaxException {
    UriBuilder ubu = UriBuilder.fromUri(new URI("http://localhost"))
        .replaceQuery("limit=10&sql=select+*+from+users");
    ubu.replaceQueryParam("limit", 100);

    URI uri = ubu.build();
    Assert.assertEquals(URI.create("http://localhost/?sql=select+*+from+users&limit=100"), uri);
  }

  @Test
  public void testReplaceQueryParamsEncoded3() throws URISyntaxException {
    UriBuilder ubu =
        UriBuilder.fromUri("http://localhost/").replaceQuery("limit=10&sql=select+*+from+users");
    ubu.replaceQueryParam("limit", 100);

    URI uri = ubu.build();
    Assert.assertEquals(URI.create("http://localhost/?sql=select+*+from+users&limit=100"), uri);
  }

  @Test
  public void testReplaceQueryParamsEncoded4() throws URISyntaxException {
    UriBuilder ubu =
        UriBuilder.fromUri("http://localhost").replaceQuery("limit=10&sql=select+*+from+users");
    ubu.replaceQueryParam("limit", 100);

    URI uri = ubu.build();
    Assert.assertEquals(URI.create("http://localhost/?sql=select+*+from+users&limit=100"), uri);
  }

  @Test
  public void testReplaceFragment() {
    URI uri =
        UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y#frag").fragment("ment").build();
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c?a=x&b=y#ment"), uri);
  }

  @Test
  public void testReplaceUri() {
    URI uu = URI.create("http://bob@localhost:8080/a/b/c?a=x&b=y#frag");

    URI uri = UriBuilder.fromUri(uu).uri(URI.create("https://bob@localhost:8080")).build();
    Assert.assertEquals(URI.create("https://bob@localhost:8080/a/b/c?a=x&b=y#frag"), uri);

    uri = UriBuilder.fromUri(uu).uri(URI.create("https://sue@localhost:8080")).build();
    Assert.assertEquals(URI.create("https://sue@localhost:8080/a/b/c?a=x&b=y#frag"), uri);

    uri = UriBuilder.fromUri(uu).uri(URI.create("https://sue@localhost:9090")).build();
    Assert.assertEquals(URI.create("https://sue@localhost:9090/a/b/c?a=x&b=y#frag"), uri);

    uri = UriBuilder.fromUri(uu).uri(URI.create("/x/y/z")).build();
    Assert.assertEquals(URI.create("http://bob@localhost:8080/x/y/z?a=x&b=y#frag"), uri);

    uri = UriBuilder.fromUri(uu).uri(URI.create("?x=a&b=y")).build();
    Assert.assertEquals(URI.create("http://bob@localhost:8080/a/b/c?x=a&b=y#frag"), uri);

    uri = UriBuilder.fromUri(uu).uri(URI.create("#ment")).build();
    Assert.assertEquals(URI.create("http://bob@localhost:8080/a/b/c?a=x&b=y#ment"), uri);
  }

  @Test
  public void testSchemeSpecificPart() {
    URI uu = URI.create("http://bob@localhost:8080/a/b/c?a=x&b=y#frag");

    URI uri =
        UriBuilder.fromUri(uu).schemeSpecificPart("//sue@remotehost:9090/x/y/z?x=a&y=b").build();
    Assert.assertEquals(URI.create("http://sue@remotehost:9090/x/y/z?x=a&y=b#frag"), uri);
  }

  @Test
  public void testAppendPath() {
    URI uri = UriBuilder.fromUri("http://localhost:8080").path("a/b/c").build();
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c"), uri);

    uri = UriBuilder.fromUri("http://localhost:8080/").path("a/b/c").build();
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c"), uri);

    uri = UriBuilder.fromUri("http://localhost:8080").path("/a/b/c").build();
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c"), uri);

    uri = UriBuilder.fromUri("http://localhost:8080/a/b/c/").path("/").build();
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/"), uri);

    uri = UriBuilder.fromUri("http://localhost:8080/a/b/c/").path("/x/y/z").build();
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z"), uri);

    uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/x/y/z").build();
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z"), uri);

    uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").path("x/y/z").build();
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z"), uri);

    uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/").build();
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/"), uri);

    uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").path("").build();
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c"), uri);

    uri = UriBuilder.fromUri("http://localhost:8080/a%20/b%20/c%20").path("/x /y /z ").build();
    Assert.assertEquals(URI.create("http://localhost:8080/a%20/b%20/c%20/x%20/y%20/z%20"), uri);
  }

  @Test
  public void testAppendSegment() {
    URI uri = UriBuilder.fromUri("http://localhost:8080").segment("a/b/c;x").build();
    Assert.assertEquals(URI.create("http://localhost:8080/a%2Fb%2Fc%3Bx"), uri);
  }

  @Test
  public void testWhitespacesInPathParams() {
    URI uri = UriBuilder.fromUri("http://localhost:80/aaa/{  par1}/").path("bbb/{  par2   }/ccc")
        .build("1param", "2param");
    assertEquals(URI.create("http://localhost:80/aaa/1param/bbb/2param/ccc"), uri);
  }

  @Test
  public void testWhitespacesInPathParamsByResolve() {
    URI uri = UriBuilder.fromUri("http://localhost:80/aaa/{  par1}/").path("bbb/{  par2   }/ccc")
        .build("1param", "2param");
    assertEquals(URI.create("http://localhost:80/aaa/1param/bbb/2param/ccc"), uri);
  }


  @Test
  public void testWhitespacesInPathParams2() {
    URI uri = UriBuilder.fromUri("http://localhost:80/aaa/{  par1}")
        .path("bbb/{  par2 : \\d*  }/ccc").build("1param", "2");
    assertEquals(URI.create("http://localhost:80/aaa/1param/bbb/2/ccc"), uri);
  }

  @Test
  public void testWhitespacesInPathParams2ByResolve() {
    URI uri =
        UriBuilder.fromUri("http://localhost:80/aaa/{  par1}").path("bbb/{  par2 : \\d*  }/ccc")
            .resolveTemplate("par1", "1param").resolveTemplate("par2", "2").build();
    assertEquals(URI.create("http://localhost:80/aaa/1param/bbb/2/ccc"), uri);
  }


  @Test
  public void testWhitespacesInQueryParams() {
    URI uri = UriBuilder.fromUri("http://localhost:80/aaa?a={      param   : \\d*  }").build("5");
    assertEquals(URI.create("http://localhost:80/aaa?a=5"), uri);
  }

  @Test
  public void testWhitespacesInQueryParamsByResolve() {
    URI uri = UriBuilder.fromUri("http://localhost:80/aaa?a={      param   : \\d*  }")
        .resolveTemplate("param", "5").build();
    assertEquals(URI.create("http://localhost:80/aaa?a=5"), uri);
  }

  @Test
  public void testRelativeFromUri() {
    URI uri = UriBuilder.fromUri("a/b/c").build();
    Assert.assertEquals(URI.create("a/b/c"), uri);

    uri = UriBuilder.fromUri("a/b/c").path("d").build();
    Assert.assertEquals(URI.create("a/b/c/d"), uri);

    uri = UriBuilder.fromUri("a/b/c/").path("d").build();
    Assert.assertEquals(URI.create("a/b/c/d"), uri);

    uri = UriBuilder.fromUri("a/b/c").path("/d").build();
    Assert.assertEquals(URI.create("a/b/c/d"), uri);

    uri = UriBuilder.fromUri("a/b/c/").path("/d").build();
    Assert.assertEquals(URI.create("a/b/c/d"), uri);

    uri = UriBuilder.fromUri("").queryParam("x", "y").build();
    Assert.assertEquals(URI.create("?x=y"), uri);

  }

  @Test
  public void testRelativefromPath() {
    URI uri = UriBuilder.fromPath("a/b/c").build();
    Assert.assertEquals(URI.create("a/b/c"), uri);

    uri = UriBuilder.fromPath("a/b/c").path("d").build();
    Assert.assertEquals(URI.create("a/b/c/d"), uri);

    uri = UriBuilder.fromPath("a/b/c/").path("d").build();
    Assert.assertEquals(URI.create("a/b/c/d"), uri);

    uri = UriBuilder.fromPath("a/b/c").path("/d").build();
    Assert.assertEquals(URI.create("a/b/c/d"), uri);

    uri = UriBuilder.fromPath("a/b/c/").path("/d").build();
    Assert.assertEquals(URI.create("a/b/c/d"), uri);

    uri = UriBuilder.fromPath("").queryParam("x", "y").build();
    Assert.assertEquals(URI.create("?x=y"), uri);
  }

  @Test
  public void testAppendQueryParams() throws URISyntaxException {
    URI uri =
        UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").queryParam("c", "z").build();
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c?a=x&b=y&c=z"), uri);

    uri =
        UriBuilder.fromUri("http://localhost:8080/a/b/c?a=x&b=y").queryParam("c= ", "z= ").build();
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c?a=x&b=y&c%3D+=z%3D+"), uri);

    uri = UriBuilder.fromUri(new URI("http://localhost:8080/")).queryParam("c", "z").build();
    Assert.assertEquals(URI.create("http://localhost:8080/?c=z"), uri);

    uri = UriBuilder.fromUri(new URI("http://localhost:8080")).queryParam("c", "z").build();
    Assert.assertEquals(URI.create("http://localhost:8080/?c=z"), uri);

    uri = UriBuilder.fromUri("http://localhost:8080/").queryParam("c", "z").build();
    Assert.assertEquals(URI.create("http://localhost:8080/?c=z"), uri);

    uri = UriBuilder.fromUri("http://localhost:8080").queryParam("c", "z").build();
    Assert.assertEquals(URI.create("http://localhost:8080/?c=z"), uri);

    try {
      UriBuilder.fromPath("http://localhost:8080").queryParam("name", "x", null).build();
    } catch (IllegalArgumentException ex) {
      Assert.assertTrue(true);
    } catch (NullPointerException ex) {
      ex.printStackTrace();
      Assert.assertTrue(false);
    }
  }

  @Test
  @Ignore
  public void testAppendMatrixParams() {
    URI uri =
        UriBuilder.fromUri("http://localhost:8080/a/b/c;a=x;b=y").matrixParam("c", "z").build();
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c;a=x;b=y;c=z"), uri);

    uri = UriBuilder.fromUri("http://localhost:8080/a/b/c;a=x;b=y").matrixParam("c=/ ;", "z=/ ;")
        .build();
    Assert.assertEquals(
        URI.create("http://localhost:8080/a/b/c;a=x;b=y;c%3D%2F%20%3B=z%3D%2F%20%3B"), uri);
  }

  @Test
  @Ignore
  public void testAppendPathAndMatrixParams() {
    URI uri = UriBuilder.fromUri("http://localhost:8080/").path("a").matrixParam("x", "foo")
        .matrixParam("y", "bar").path("b").matrixParam("x", "foo").matrixParam("y", "bar").build();
    Assert.assertEquals(URI.create("http://localhost:8080/a;x=foo;y=bar/b;x=foo;y=bar"), uri);
  }

  @Path("resource")
  class Resource {
    @Path("method")
    public @GET String get() {
      return "";
    }

    @Path("locator")
    public Object locator() {
      return null;
    }
  }

  @Test
  public void testResourceAppendPath() throws NoSuchMethodException {
    URI ub = UriBuilder.fromUri("http://localhost:8080/base").path(Resource.class).build();
    Assert.assertEquals(URI.create("http://localhost:8080/base/resource"), ub);

    ub = UriBuilder.fromUri("http://localhost:8080/base").path(Resource.class, "get").build();
    Assert.assertEquals(URI.create("http://localhost:8080/base/method"), ub);

    Method get = Resource.class.getMethod("get");
    Method locator = Resource.class.getMethod("locator");
    ub = UriBuilder.fromUri("http://localhost:8080/base").path(get).path(locator).build();
    Assert.assertEquals(URI.create("http://localhost:8080/base/method/locator"), ub);
  }

  @Path("resource/{id}")
  class ResourceWithTemplate {
    @Path("method/{id1}")
    public @GET String get() {
      return "";
    }

    @Path("locator/{id2}")
    public Object locator() {
      return null;
    }
  }

  @Test
  public void testResourceWithTemplateAppendPath() throws NoSuchMethodException {
    URI ub = UriBuilder.fromUri("http://localhost:8080/base").path(ResourceWithTemplate.class)
        .build("foo");
    Assert.assertEquals(URI.create("http://localhost:8080/base/resource/foo"), ub);

    ub = UriBuilder.fromUri("http://localhost:8080/base").path(ResourceWithTemplate.class, "get")
        .build("foo");
    Assert.assertEquals(URI.create("http://localhost:8080/base/method/foo"), ub);

    Method get = ResourceWithTemplate.class.getMethod("get");
    Method locator = ResourceWithTemplate.class.getMethod("locator");
    ub = UriBuilder.fromUri("http://localhost:8080/base").path(get).path(locator).build("foo",
        "bar");
    Assert.assertEquals(URI.create("http://localhost:8080/base/method/foo/locator/bar"), ub);
  }

  @Path("resource/{id: .+}")
  class ResourceWithTemplateRegex {
    @Path("method/{id1: .+}")
    public @GET String get() {
      return "";
    }

    @Path("locator/{id2: .+}")
    public Object locator() {
      return null;
    }
  }

  @Test
  public void testResourceWithTemplateRegexAppendPath() throws NoSuchMethodException {
    URI ub = UriBuilder.fromUri("http://localhost:8080/base").path(ResourceWithTemplateRegex.class)
        .build("foo");
    Assert.assertEquals(URI.create("http://localhost:8080/base/resource/foo"), ub);

    ub = UriBuilder.fromUri("http://localhost:8080/base")
        .path(ResourceWithTemplateRegex.class, "get").build("foo");
    Assert.assertEquals(URI.create("http://localhost:8080/base/method/foo"), ub);

    Method get = ResourceWithTemplateRegex.class.getMethod("get");
    Method locator = ResourceWithTemplateRegex.class.getMethod("locator");
    ub = UriBuilder.fromUri("http://localhost:8080/base").path(get).path(locator).build("foo",
        "bar");
    Assert.assertEquals(URI.create("http://localhost:8080/base/method/foo/locator/bar"), ub);
  }

  @Test
  public void testBuildTemplates() {
    URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/{foo}/{bar}/{baz}/{foo}")
        .build("x", "y", "z");
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z/x"), uri);

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("foo", "x");
    map.put("bar", "y");
    map.put("baz", "z");
    uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/{foo}/{bar}/{baz}/{foo}")
        .buildFromMap(map);
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z/x"), uri);
  }

  @Test
  public void testBuildTemplatesByResolve() {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("foo", "x");
    map.put("bar", "y");
    map.put("baz", "z");

    URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/{foo}/{bar}/{baz}/{foo}")
        .resolveTemplates(map).build();

    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z/x"), uri);
  }

  @Test
  public void testBuildTemplatesWithNameAuthority() {
    URI uri = UriBuilder.fromUri("http://x_y.com:8080/a/b/c").path("/{foo}/{bar}/{baz}/{foo}")
        .build("x", "y", "z");
    Assert.assertEquals(URI.create("http://x_y.com:8080/a/b/c/x/y/z/x"), uri);

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("foo", "x");
    map.put("bar", "y");
    map.put("baz", "z");
    uri = UriBuilder.fromUri("http://x_y.com:8080/a/b/c").path("/{foo}/{bar}/{baz}/{foo}")
        .buildFromMap(map);
    Assert.assertEquals(URI.create("http://x_y.com:8080/a/b/c/x/y/z/x"), uri);
  }

  @Test
  public void testBuildTemplatesWithNameAuthorityByResolve() {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("foo", "x");
    map.put("bar", "y");
    map.put("baz", "z");
    URI uri = UriBuilder.fromUri("http://x_y.com:8080/a/b/c").path("/{foo}/{bar}/{baz}/{foo}")
        .buildFromMap(map);
    Assert.assertEquals(URI.create("http://x_y.com:8080/a/b/c/x/y/z/x"), uri);
  }


  @Test
  @Ignore
  public void testBuildFromMap() {
    Map<String, Object> maps = new HashMap<String, Object>();
    maps.put("x", null);
    maps.put("y", "/path-absolute/test1");
    maps.put("z", "fred@example.com");
    maps.put("w", "path-rootless/test2");
    maps.put("u", "extra");

    boolean caught = false;

    try {
      System.out
          .println(UriBuilder.fromPath("").path("{w}/{x}/{y}/{z}/{x}").buildFromEncodedMap(maps));

    } catch (IllegalArgumentException ex) {
      caught = true;
    }

    Assert.assertTrue(caught);
  }


  @Test
  @Ignore
  public void testBuildFromMapByResolve() {
    Map<String, Object> maps = new HashMap<String, Object>();
    maps.put("x", null);
    maps.put("y", "/path-absolute/test1");
    maps.put("z", "fred@example.com");
    maps.put("w", "path-rootless/test2");
    maps.put("u", "extra");

    boolean caught = false;

    try {
      System.out.println(
          UriBuilder.fromPath("").path("{w}/{x}/{y}/{z}/{x}").resolveTemplates(maps).build());

    } catch (IllegalArgumentException ex) {
      caught = true;
    }

    Assert.assertTrue(caught);
  }

  @Test
  public void testBuildQueryTemplates() {
    URI uri =
        UriBuilder.fromUri("http://localhost:8080/a/b/c").queryParam("a", "{b}").build("=+&%xx%20");
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c?a=%3D%2B%26%25xx%2520"), uri);

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("b", "=+&%xx%20");
    uri =
        UriBuilder.fromUri("http://localhost:8080/a/b/c").queryParam("a", "{b}").buildFromMap(map);
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c?a=%3D%2B%26%25xx%2520"), uri);
  }

  @Test
  @Ignore
  public void testBuildFromEncodedQueryTemplates() {
    URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").queryParam("a", "{b}")
        .buildFromEncoded("=+&%xx%20");
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c?a=%3D%2B%26%25xx%20"), uri);

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("b", "=+&%xx%20");
    uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").queryParam("a", "{b}")
        .buildFromEncodedMap(map);
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c?a=%3D%2B%26%25xx%20"), uri);
  }

  @Test
  public void testBuildFromEncodedSlashInParamValue() {
    assertEquals("/A/B", UriBuilder.fromUri("/{param}").buildFromEncoded("A/B").toString());
  }

  @Test
  @Ignore
  public void testResolveTemplateFromEncodedQueryTemplates() {
    URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").queryParam("a", "{b}")
        .resolveTemplateFromEncoded("b", "=+&%xx%20").build();
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c?a=%3D%2B%26%25xx%20"), uri);

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("b", "=+&%xx%20");
    uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").queryParam("a", "{b}")
        .resolveTemplatesFromEncoded(map).build();
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c?a=%3D%2B%26%25xx%20"), uri);
  }

  @Test
  public void testBuildFragmentTemplates() {
    URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/{foo}/{bar}/{baz}/{foo}")
        .fragment("{foo}").build("x", "y", "z");
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z/x#x"), uri);

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("foo", "x");
    map.put("bar", "y");
    map.put("baz", "z");
    uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/{foo}/{bar}/{baz}/{foo}")
        .fragment("{foo}").buildFromMap(map);
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z/x#x"), uri);
  }


  @Test
  public void testResolveTemplateFromFragmentTemplates() {
    URI uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/{foo}/{bar}/{baz}/{foo}")
        .fragment("{foo}").resolveTemplate("foo", "x").resolveTemplate("bar", "y")
        .resolveTemplate("baz", "z").build();

    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z/x#x"), uri);

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("foo", "x");
    map.put("bar", "y");
    map.put("baz", "z");
    uri = UriBuilder.fromUri("http://localhost:8080/a/b/c").path("/{foo}/{bar}/{baz}/{foo}")
        .fragment("{foo}").resolveTemplates(map).build();
    Assert.assertEquals(URI.create("http://localhost:8080/a/b/c/x/y/z/x#x"), uri);
  }

  @Test
  public void testTemplatesDefaultPort() {
    URI uri = UriBuilder.fromUri("http://localhost/a/b/c").path("/{foo}/{bar}/{baz}/{foo}")
        .build("x", "y", "z");
    Assert.assertEquals(URI.create("http://localhost/a/b/c/x/y/z/x"), uri);

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("foo", "x");
    map.put("bar", "y");
    map.put("baz", "z");
    uri = UriBuilder.fromUri("http://localhost/a/b/c").path("/{foo}/{bar}/{baz}/{foo}")
        .buildFromMap(map);
    Assert.assertEquals(URI.create("http://localhost/a/b/c/x/y/z/x"), uri);
  }

  @Test
  public void testResolveTemplatesDefaultPort() {
    URI uri = UriBuilder.fromUri("http://localhost/a/b/c").path("/{foo}/{bar}/{baz}/{foo}")
        .resolveTemplate("foo", "x").resolveTemplate("bar", "y").resolveTemplate("baz" + "", "z")
        .build();
    Assert.assertEquals(URI.create("http://localhost/a/b/c/x/y/z/x"), uri);

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("foo", "x");
    map.put("bar", "y");
    map.put("baz", "z");
    uri = UriBuilder.fromUri("http://localhost/a/b/c").path("/{foo}/{bar}/{baz}/{foo}")
        .resolveTemplates(map).build();
    Assert.assertEquals(URI.create("http://localhost/a/b/c/x/y/z/x"), uri);
  }


  @Test
  public void testClone() {
    UriBuilder ub = UriBuilder.fromUri("http://user@localhost:8080/?query#fragment").path("a");
    URI full = ub.clone().path("b").build();
    URI base = ub.build();

    Assert.assertEquals(URI.create("http://user@localhost:8080/a?query#fragment"), base);
    Assert.assertEquals(URI.create("http://user@localhost:8080/a/b?query#fragment"), full);
  }

  @Test
  public void testIllegalArgumentException() {
    boolean caught = false;
    try {
      UriBuilder.fromPath(null);
    } catch (IllegalArgumentException ex) {
      caught = true;
    }
    Assert.assertTrue(caught);

    caught = false;
    try {
      UriBuilder.fromUri((URI) null);
    } catch (IllegalArgumentException ex) {
      caught = true;
    }
    Assert.assertTrue(caught);

    caught = false;
    try {
      UriBuilder.fromUri((String) null);
    } catch (IllegalArgumentException ex) {
      caught = true;
    }
    Assert.assertTrue(caught);
  }

  @Test
  @Ignore
  public void testUriEncoding() {
    final URI expected = URI.create("http://localhost:8080/%5E");
    assertEquals(expected, new UriBuilderImpl().uri("http://localhost:8080/^").build());
    assertEquals(expected, UriBuilder.fromUri("http://localhost:8080/^").build());
  }

  // Regression test for JERSEY-1324 fix.
  @Test
  public void testInvalidUriTemplateEncodedAsPath() {
    assertEquals(URI.create("http%20ftp%20xml//:888:888/1:8080:80"),
        new UriBuilderImpl().uri("http ftp xml//:888:888/1:8080:80").build());
  }

  @Test
  public void testPortValue() {
    boolean caught = false;
    try {
      UriBuilder.fromPath("http://localhost").port(-2);
    } catch (IllegalArgumentException ex) {
      caught = true;
    }
    Assert.assertTrue(caught);
  }

  @Test
  public void testPortSetting() throws URISyntaxException {
    URI uri;

    uri = new UriBuilderImpl().uri("http://localhost").port(8080).build();
    Assert.assertEquals(URI.create("http://localhost:8080"), uri);

    uri = new UriBuilderImpl().uri(new URI("http://localhost")).port(8080).build();
    Assert.assertEquals(URI.create("http://localhost:8080"), uri);

    uri = new UriBuilderImpl().uri("http://localhost/").port(8080).build();
    Assert.assertEquals(URI.create("http://localhost:8080/"), uri);

    uri = new UriBuilderImpl().uri(new URI("http://localhost/")).port(8080).build();
    Assert.assertEquals(URI.create("http://localhost:8080/"), uri);
  }

  @Test
  @Ignore
  public void testHostValue() {
    boolean caught = false;
    try {
      UriBuilder.fromPath("http://localhost").host("");
    } catch (IllegalArgumentException ex) {
      caught = true;
    }
    Assert.assertTrue(caught);

    URI uri = UriBuilder.fromPath("").host("abc").build();
    Assert.assertEquals(URI.create("//abc"), uri);

    uri = UriBuilder.fromPath("").host("abc").host(null).build();
    Assert.assertEquals(URI.create(""), uri);
  }


  @Test
  public void testEncodeTemplateNames() {
    URI uri =
        UriBuilder.fromPath("http://localhost:8080").path("/{a}/{b}").replaceQuery("q={c}").build();
    Assert.assertEquals(URI.create("http://localhost:8080/%7Ba%7D/%7Bb%7D?q=%7Bc%7D"), uri);
  }

  @Test
  public void resolveTemplateTest() {
    final UriBuilder uriBuilder = UriBuilder.fromPath("http://localhost:8080").path("{a}")
        .path("{b}").queryParam("query", "{q}");
    uriBuilder.resolveTemplate("a", "param-a");
    uriBuilder.resolveTemplate("q", "param-q");
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("a", "ignored-a");
    map.put("b", "param-b");
    map.put("q", "ignored-q");
    Assert.assertEquals(URI.create("http://localhost:8080/param-a/param-b?query=param-q"),
        uriBuilder.buildFromMap(map));
    uriBuilder.build();
  }


  @Test
  public void resolveTemplateFromEncodedTest() {
    final UriBuilder uriBuilder = UriBuilder.fromPath("http://localhost:8080").path("{a}")
        .path("{b}").path("{c}").queryParam("query", "{q}");
    uriBuilder.resolveTemplateFromEncoded("a", "x/y/z%3F%20");
    uriBuilder.resolveTemplateFromEncoded("q", "q?%20%26");
    uriBuilder.resolveTemplate("c", "paramc1/paramc2");
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("a", "ignored-a");
    map.put("b", "param-b/aaa");
    map.put("q", "ignored-q");
    Assert.assertEquals(
        "http://localhost:8080/x/y/z%3F%20/param-b/aaa/paramc1%2Fparamc2?query=q?%20%26",
        uriBuilder.buildFromEncodedMap(map).toString());
    uriBuilder.build();
  }

  @Test
  public void resolveTemplateWithoutEncodedTest() {
    final UriBuilder uriBuilder = UriBuilder.fromPath("http://localhost:8080").path("{a}")
        .path("{b}").path("{c}").queryParam("query", "{q}");
    uriBuilder.resolveTemplate("a", "x/y/z%3F%20");
    uriBuilder.resolveTemplate("q", "q?%20%26");
    uriBuilder.resolveTemplate("c", "paramc1/paramc2");
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("a", "ignored-a");
    map.put("b", "param-b/aaa");
    map.put("q", "ignored-q");
    Assert.assertEquals(
        "http://localhost:8080/x%2Fy%2Fz%253F%2520/param-b%2Faaa/paramc1%2Fparamc2?query=q%3F%2520%2526",
        uriBuilder.buildFromMap(map).toString());
    uriBuilder.build();
  }


  @Test
  public void resolveTemplateWithEncodedSlashTest() {
    final UriBuilder uriBuilder = UriBuilder.fromPath("http://localhost:8080").path("{a}")
        .path("{b}").queryParam("query", "{q}");
    uriBuilder.resolveTemplate("a", "param-a/withSlash", false);
    uriBuilder.resolveTemplate("b", "param-b/withEncodedSlash", true);
    uriBuilder.resolveTemplate("q", "param-q", true);
    Assert.assertEquals(
        URI.create(
            "http://localhost:8080/param-a/withSlash/param-b%2FwithEncodedSlash?query=param-q"),
        uriBuilder.build());
    uriBuilder.build();
  }

  @Test
  public void resolveTemplatesTest() {
    final UriBuilder uriBuilder = UriBuilder.fromPath("http://localhost:8080").path("{a}")
        .path("{b}").queryParam("query", "{q}");

    uriBuilder.resolveTemplate("a", "param-a");
    uriBuilder.resolveTemplate("q", "param-q");
    Map<String, Object> buildMap = new HashMap<String, Object>();
    buildMap.put("a", "ignored-a");
    buildMap.put("b", "param-b");
    buildMap.put("q", "ignored-q");
    Assert.assertEquals(URI.create("http://localhost:8080/param-a/param-b?query=param-q"),
        uriBuilder.buildFromMap(buildMap));
    uriBuilder.build();
  }

  @Test
  @Ignore
  public void resolveTemplatesFromEncodedTest() {
    final UriBuilder uriBuilder = UriBuilder.fromPath("http://localhost:8080").path("{a}")
        .path("{b}").path("{c}").queryParam("query", "{q}");

    Map<String, Object> resolveMap = new HashMap<String, Object>();
    resolveMap.put("a", "x/y/z%3F%20");
    resolveMap.put("q", "q?%20%26");
    resolveMap.put("c", "paramc1/paramc2");
    uriBuilder.resolveTemplatesFromEncoded(resolveMap);
    Map<String, Object> buildMap = new HashMap<String, Object>();
    buildMap.put("b", "param-b/aaa");
    Assert.assertEquals(
        "http://localhost:8080/x/y/z%3F%20/param-b/aaa/paramc1/paramc2?query=q?%20%26",
        uriBuilder.buildFromEncodedMap(buildMap).toString());
    uriBuilder.build();
  }

  @Test
  @Ignore
  public void resolveTemplatesFromNotEncodedTest() {
    final UriBuilder uriBuilder = UriBuilder.fromPath("http://localhost:8080").path("{a}")
        .path("{b}").path("{c}").queryParam("query", "{q}");

    Map<String, Object> resolveMap = new HashMap<String, Object>();
    resolveMap.put("a", "x/y/z%3F%20");
    resolveMap.put("q", "q?%20%26");
    resolveMap.put("c", "paramc1/paramc2");
    uriBuilder.resolveTemplates(resolveMap);
    Map<String, Object> buildMap = new HashMap<String, Object>();
    buildMap.put("b", "param-b/aaa");
    Assert.assertEquals(
        "http://localhost:8080/x%2Fy%2Fz%253F%2520/param-b%2Faaa/paramc1%2Fparamc2?query=q?%2520%2526",
        uriBuilder.buildFromMap(buildMap).toString());
    uriBuilder.build();
  }


  @Test
  public void resolveTemplatesEncodeSlash() {
    final UriBuilder uriBuilder = UriBuilder.fromPath("http://localhost:8080").path("{a}")
        .path("{b}").path("{c}").queryParam("query", "{q}");

    Map<String, Object> resolveMap = new HashMap<String, Object>();
    resolveMap.put("a", "x/y/z%3F%20");
    resolveMap.put("q", "q%20%26");
    resolveMap.put("c", "paramc1/paramc2");
    uriBuilder.resolveTemplates(resolveMap, false);
    Map<String, Object> buildMap = new HashMap<String, Object>();
    buildMap.put("b", "param-b/aaa");
    Assert.assertEquals(
        "http://localhost:8080/x/y/z%253F%2520/param-b/aaa/paramc1/paramc2?query=q%2520%2526",
        uriBuilder.buildFromMap(buildMap, false).toString());
    uriBuilder.build();
  }

  @Test
  public void resolveTemplatesWithEncodedSlashTest() {
    final UriBuilder uriBuilder = UriBuilder.fromPath("http://localhost:8080").path("{a}")
        .path("{b}").queryParam("query", "{q}");
    Map<String, Object> resolveMap = new HashMap<String, Object>();
    resolveMap.put("a", "param-a/withSlash");
    resolveMap.put("q", "param-q");
    uriBuilder.resolveTemplates(resolveMap, false);
    uriBuilder.resolveTemplate("b", "param-b/withEncodedSlash", true);
    Assert.assertEquals(
        URI.create(
            "http://localhost:8080/param-a/withSlash/param-b%2FwithEncodedSlash?query=param-q"),
        uriBuilder.build());
    uriBuilder.build();
  }

  @Test
  @Ignore
  public void resolveTemplateMultipleCall() {
    final UriBuilder uriBuilder = UriBuilder.fromPath("http://localhost:8080").path("{start}")
        .path("{a}").resolveTemplate("a", "first-a").path("{a}").resolveTemplate("a", "second-a")
        .path("{a}/{a}").resolveTemplate("a", "twice-a");

    Assert.assertEquals(
        URI.create("http://localhost:8080/start-path/first-a/second-a/twice-a/twice-a"),
        uriBuilder.build("start-path"));
  }

  @Test
  public void replaceWithEmtpySchemeFromUriTest() throws URISyntaxException {
    String uriOrig = "ftp://ftp.is.co.za/rfc/rfc1808.txt";
    URI uriReplace = new URI(null, "ftp.is.co.za", "/test/rfc1808.txt", null, null);
    URI uri = UriBuilder.fromUri(new URI(uriOrig)).uri(uriReplace).build();
    Assert.assertEquals("ftp://ftp.is.co.za/test/rfc1808.txt", uri.toString());
  }

  @Test
  public void replaceWithEmptySchemeFromStringTest() throws URISyntaxException {
    String uriOrig = "ftp://ftp.is.co.za/rfc/rfc1808.txt";
    URI uriReplace = new URI(null, "ftp.is.co.za", "/test/rfc1808.txt", null, null);

    URI uri = UriBuilder.fromUri(new URI(uriOrig)).uri(uriReplace.toASCIIString()).build();
    Assert.assertEquals("ftp://ftp.is.co.za/test/rfc1808.txt", uri.toString());
  }

  @Test
  public void replaceWithEmptyQueryFromStringTest() throws URISyntaxException {
    String uriOrig = "ftp://ftp.is.co.za/rfc/rfc1808.txt?a=1";
    URI uriReplace = new URI(null, "ftp.is.co.za", "/test/rfc1808.txt", null, null);

    URI uri = UriBuilder.fromUri(new URI(uriOrig)).uri(uriReplace.toASCIIString()).build();
    Assert.assertEquals("ftp://ftp.is.co.za/test/rfc1808.txt?a=1", uri.toString());
  }

  @Test
  public void replaceWithEmptyFragmentFromStringTest() throws URISyntaxException {
    String uriOrig = "ftp://ftp.is.co.za/rfc/rfc1808.txt#myFragment";
    URI uriReplace = new URI(null, "ftp.is.co.za", "/test/rfc1808.txt", null, null);

    URI uri = UriBuilder.fromUri(new URI(uriOrig)).uri(uriReplace.toASCIIString()).build();
    Assert.assertEquals("ftp://ftp.is.co.za/test/rfc1808.txt#myFragment", uri.toString());
  }

  @Test
  public void replaceOpaqueUriWithNonOpaqueFromStringTest() throws URISyntaxException {
    String first = "news:comp.lang.java";
    String second = "http://comp.lang.java";
    UriBuilder.fromUri(new URI(first)).uri(second);
  }

  @Test
  public void replaceOpaqueUriWithNonOpaqueFromStringTest2() throws URISyntaxException {
    String first = "news:comp.lang.java";
    String second = "http://comp.lang.java";
    UriBuilder.fromUri(new URI(first)).scheme("http").uri(second);
  }

  @Test
  public void replaceOpaqueUriWithNonOpaqueFromUriTest() throws URISyntaxException {
    String first = "news:comp.lang.java";
    String second = "http://comp.lang.java";
    UriBuilder.fromUri(new URI(first)).uri(new URI(second));
  }

  @Test
  public void testQueryParamEncoded() {
    final UriBuilder uriBuilder = UriBuilder.fromUri("http://localhost:8080/path");
    uriBuilder.queryParam("query", "%dummy23");
    Assert.assertEquals("http://localhost:8080/path?query=%25dummy23",
        uriBuilder.build().toString());
  }

  @Test
  public void testQueryParamEncoded2() {
    final UriBuilder uriBuilder = UriBuilder.fromUri("http://localhost:8080/path");
    uriBuilder.queryParam("query", "{param}");
    Assert.assertEquals("http://localhost:8080/path?query=%25dummy23",
        uriBuilder.build("%dummy23").toString());
  }

  @Test
  public void testQueryParamEncoded3() {
    final UriBuilder uriBuilder = UriBuilder.fromUri("http://localhost:8080/path");
    uriBuilder.queryParam("query", "{param}");
    Assert.assertEquals("http://localhost:8080/path?query=%2525test",
        uriBuilder.build("%25test").toString());
  }

  @Test
  public void testQueryParamEncoded4() {
    final UriBuilder uriBuilder = UriBuilder.fromUri("http://localhost:8080/path");
    uriBuilder.queryParam("query", "{param}");
    Assert.assertEquals("http://localhost:8080/path?query=%25test",
        uriBuilder.buildFromEncoded("%25test").toString());
  }
}
