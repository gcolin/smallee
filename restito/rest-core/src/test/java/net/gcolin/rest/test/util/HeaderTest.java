package net.gcolin.rest.test.util;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import net.gcolin.rest.util.Header;
import net.gcolin.rest.util.Headers;

public class HeaderTest {

	@Test
	public void getHeaderParametersTest() {
		Map<String, String> headers = Headers.getParameters("hello");
		Assert.assertEquals(0, headers.size());

		headers = Headers.getParameters("hello;name=value");
		Assert.assertEquals(1, headers.size());
		Assert.assertEquals("value", headers.get("name"));

		headers = Headers.getParameters("hello;name=\"value\"");
		Assert.assertEquals(1, headers.size());
		Assert.assertEquals("value", headers.get("name"));
	}

	@Test
	public void testHeaders() {
		List<Header> list = Headers.parse("fr,en,es");
		Assert.assertEquals(3, list.size());
		Assert.assertEquals("fr", list.get(0).getValue());
		Assert.assertEquals("en", list.get(1).getValue());
		Assert.assertEquals("es", list.get(2).getValue());

		list = Headers.parse("en;q=0.9,fr,es;q=0.8;p=q");
		Assert.assertEquals(3, list.size());
		Assert.assertEquals("fr", list.get(0).getValue());
		Assert.assertEquals("en", list.get(1).getValue());
		Assert.assertEquals("es", list.get(2).getValue());

		list = Headers.parse(" fr , en  ,  es ");
		Assert.assertEquals(3, list.size());
		Assert.assertEquals("fr", list.get(0).getValue());
		Assert.assertEquals("en", list.get(1).getValue());
		Assert.assertEquals("es", list.get(2).getValue());

		list = Headers.parse("fr,en;q=0.9,es;q=0.7");
		Assert.assertEquals(3, list.size());
		Assert.assertEquals("fr", list.get(0).getValue());
		Assert.assertEquals("en", list.get(1).getValue());
		Assert.assertEquals("es", list.get(2).getValue());

		Assert.assertEquals(1.0f, list.get(0).getSort(), 0.1f);
		Assert.assertEquals(0.9f, list.get(1).getSort(), 0.1f);
		Assert.assertEquals(0.7f, list.get(2).getSort(), 0.1f);

		list = Headers.parse("fr;w=hello,en;p=world;a b=pop");
		Assert.assertEquals(2, list.size());
		Assert.assertEquals("hello", list.get(0).getParameters().get("w"));
		Assert.assertEquals("world", list.get(1).getParameters().get("p"));
		Assert.assertEquals("pop", list.get(1).getParameters().get("a b"));
	}

}
