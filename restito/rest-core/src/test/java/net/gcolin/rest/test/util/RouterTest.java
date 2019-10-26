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

import java.util.Collection;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import net.gcolin.rest.util.HasPath;
import net.gcolin.rest.util.Router;
import net.gcolin.rest.util.RouterResponse;

public class RouterTest {

	private static final Logger LOG = Logger.getLogger(RouterTest.class.getName());

	@Test
	public void testSameVariableName() {
		Router<Resource> router = new Router<>();
		Resource res = new Resource("/{word}/{word}");
		router.add(res);

		RouterResponse<Resource> response = router.get("/hello/world");
		Assert.assertEquals(2, response.getParams().get("word").size());
		Assert.assertEquals("hello", response.getParams().get("word").get(0));
		Assert.assertEquals("world", response.getParams().get("word").get(1));
	}

	@Test
	public void testComplex() {
		Router<Resource> router = new Router<>();
		Resource res1 = new Resource("/hello/{word}");
		Resource res2 = new Resource("/hello/{w}/world/{word}");
		router.add(res1);
		router.add(res2);

		RouterResponse<Resource> response = router.get("/hello/world");
		Assert.assertEquals(res1, response.getResult());
		Assert.assertEquals(res2, router.get("/hello/a/world/b").getResult());
	}

	@Test
	public void testSamePath() {
		try {
			Router<Resource> router = new Router<>();
			router.add(new Resource("/hello"));
			router.add(new Resource("/hello"));
			LOG.info(router.toString());
			Assert.fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException ex) {
			Assert.assertTrue(ex.getMessage().startsWith("two resources have the same path : "));
		}

		Router<Resource> router = new Router<>();
		router.add(new Resource("/hello"), 0, true);
		router.add(new Resource("/hello"), 0, true);
	}

	@Test
	public void testSamePathWithVariable() {
		try {
			Router<Resource> router = new Router<>();
			router.add(new Resource("/hello/{id}"));
			router.add(new Resource("/hello/{id}"));
			Assert.fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException ex) {
			Assert.assertTrue(ex.getMessage().startsWith("two resources have the same path : "));
		}

		Router<Resource> router = new Router<>();
		router.add(new Resource("/hello/{id}"), 0, true);
		router.add(new Resource("/hello/{id}"), 0, true);
	}

	@Ignore
	@Test
	public void testDifferentVariableName() {
		try {
			Router<Resource> router = new Router<>();
			router.add(new Resource("/hello/{id}"));
			router.add(new Resource("/hello/{cid}/plus"));
			Assert.fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException ex) {
			Assert.assertTrue(ex.getMessage().startsWith(
					"you have two resources with the same starting path but with different variable names"));
		}
	}

	@Test
	public void testBadVariable() {
		try {
			Router<Resource> router = new Router<>();
			router.add(new Resource("/hello/{id}123"));
			Assert.fail("should throw IllegalArgumentException");
		} catch (IllegalArgumentException ex) {
			Assert.assertTrue(ex.getMessage().startsWith("variable should end with /"));
		}
	}

	@Test
	public void testToString() {
		Router<Resource> router = new Router<>();
		Resource r1 = new Resource("/hello1");
		Resource r2 = new Resource("/hello2");
		router.add(r1);
		router.add(r2);
		Collection<Resource> collection = router.values();
		Assert.assertEquals(2, collection.size());
		Assert.assertTrue(collection.contains(r1));
		Assert.assertTrue(collection.contains(r2));
		Assert.assertEquals("/hello1  /hello1\n/hello2  /hello2", router.toString());
	}

	@Test
	public void test1Resource() {
		Resource res = new Resource("/hello");
		Router<Resource> router = new Router<>();
		router.add(res);
		Assert.assertNull(router.get(null));
		Assert.assertNull(router.getResource(null));
		Assert.assertNull(router.get("/hella"));
		Assert.assertNull(router.get("hello"));
		Assert.assertNull(router.get("/hell"));
		Assert.assertEquals(res, router.get("/hello").getResult());
		Assert.assertEquals(res, router.getResource("/hello"));
	}

	@Test
	public void testSimilarResource() {
		Resource res1 = new Resource("/ch");
		Resource res2 = new Resource("/ch2");
		Router<Resource> router = new Router<>();
		router.add(res1);
		router.add(res2);
		Assert.assertEquals(res1, router.get("/ch").getResult());
		Assert.assertEquals(res2, router.get("/ch2").getResult());
	}

	@Test
	public void test3Resources() {
		Resource res1 = new Resource("/hello/bob");
		Resource res2 = new Resource("/hello/world");
		Resource res3 = new Resource("/hellos/world");
		Router<Resource> router = new Router<>();
		router.add(res1);
		router.add(res2);
		router.add(res3);
		Assert.assertEquals(res1, router.get(res1.getPath()).getResult());
		Assert.assertEquals(res2, router.get(res2.getPath()).getResult());
		Assert.assertEquals(res3, router.get(res3.getPath()).getResult());
		Assert.assertEquals(res1, router.getResource(res1.getPath()));
		Assert.assertEquals(res2, router.getResource(res2.getPath()));
		Assert.assertEquals(res3, router.getResource(res3.getPath()));
	}

	@Test
	public void testVariable() {
		Resource res1 = new Resource("/hello/{name}");
		Resource res2 = new Resource("/hello/{name}/2");
		Resource res3 = new Resource("/hello/2");
		Router<Resource> router = new Router<>();
		router.add(res1);
		router.add(res2);
		router.add(res3);
		RouterResponse<Resource> r1 = router.get("/hello/world");
		Assert.assertEquals(res1, r1.getResult());
		Assert.assertEquals("world", r1.getParams().get("name").get(0));

		RouterResponse<Resource> r2 = router.get("/hello/world2/2");
		Assert.assertEquals(res2, r2.getResult());
		Assert.assertEquals("world2", r2.getParams().get("name").get(0));

		Assert.assertEquals(res3, router.get("/hello/2").getResult());
	}

	@Test
	public void testRegExpr() {
		Resource res1 = new Resource("/hello/{nb:\\d+}");
		Resource res2 = new Resource("/hello/{name}");
		Resource res3 = new Resource("/hello/{n : [a-zA-Z]+}");
		Resource res4 = new Resource("/terminal/{n:.+}");
		Resource res5 = new Resource("/world/{n:\\d+}");
		Router<Resource> router = new Router<>();
		router.add(res1);
		router.add(res2);
		router.add(res3);
		router.add(res4);
		router.add(res5);

		Collection<Resource> collection = router.values();
		Assert.assertEquals(5, collection.size());
		Assert.assertTrue(collection.contains(res1));
		Assert.assertTrue(collection.contains(res2));
		Assert.assertTrue(collection.contains(res3));
		Assert.assertTrue(collection.contains(res4));
		Assert.assertTrue(collection.contains(res5));

		RouterResponse<Resource> r1 = router.get("/hello/123");
		Assert.assertEquals(res1, r1.getResult());
		Assert.assertEquals("123", r1.getParams().get("nb").get(0));

		RouterResponse<Resource> r2 = router.get("/hello/gael123");
		Assert.assertEquals(res2, r2.getResult());
		Assert.assertEquals("gael123", r2.getParams().get("name").get(0));

		RouterResponse<Resource> r3 = router.get("/hello/gael");
		Assert.assertEquals(res3, r3.getResult());
		Assert.assertEquals("gael", r3.getParams().get("n").get(0));

		RouterResponse<Resource> r4 = router.get("/terminal/123/456");
		Assert.assertEquals(res4, r4.getResult());
		Assert.assertEquals(res4, router.getResource("/terminal/123/456"));
		Assert.assertEquals("123/456", r4.getParams().get("n").get(0));
		r4 = router.get("/terminal/123456");
		Assert.assertEquals(res4, r4.getResult());
		Assert.assertEquals("123456", r4.getParams().get("n").get(0));

		RouterResponse<Resource> r5 = router.get("/world/abc");
		Assert.assertNull(r5);
	}

	@Test
	public void testutf16Resource() {
		Resource res = new Resource("/ǽȁ");
		Router<Resource> router = new Router<>(0xffff);
		router.add(res);
		Assert.assertNull(router.get("/Є"));
		Assert.assertNull(router.get("/Љ"));
		Assert.assertEquals(res, router.get("/ǽȁ").getResult());
		Assert.assertEquals(res, router.getResource("/ǽȁ"));
	}

	@Test
	public void testWildcard() {
		Router<Resource> router = new Router<>();
		Resource res = new Resource("/hello/{pathinfo:.+}");
		router.add(res);

		RouterResponse<Resource> response = router.get("/hello/world");
		Assert.assertEquals(res, response.getResult());
		Assert.assertEquals(res, router.getResource("/hello/world"));
	}

	@Test
	public void testWildcard2() {
		Router<Resource> router = new Router<>();
		Resource res = new Resource("/hello/{pathinfo:.+}");
		router.add(res);
		Resource res2 = new Resource("/hello/admin/{pathinfo:.+}");
		router.add(res2);
		Resource res3 = new Resource("/hello/person/{pathinfo:.+}");
		router.add(res3);

		Assert.assertEquals(res, router.getResource("/hello/world"));
		Assert.assertEquals(res2, router.getResource("/hello/admin/world"));
		Assert.assertEquals(res3, router.getResource("/hello/person/world"));
	}

	@Test
	public void testparentFirst() {
		Router<Resource> router = new Router<>();
		Resource res = new Resource("config/application/new");
		router.add(res);
		Resource res2 = new Resource("config/application/{id:\\d+}");
		router.add(res2);
		Resource res3 = new Resource("config/application");
		router.add(res3);

		Assert.assertEquals(res3, router.getResource("config/application"));
	}

	public static class Resource implements HasPath {
		private String path;

		public Resource(String path) {
			this.path = path;
		}

		@Override
		public String getPath() {
			return path;
		}

		@Override
		public String toString() {
			return path;
		}
	}
}
