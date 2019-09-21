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
package net.gcolin.mustache.test;

import net.gcolin.mustache.ClassLoaderPartialFinder;
import net.gcolin.mustache.Mustache;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.gcolin.mustache.FilterFunction;

/**
 * @author Gaël COLIN
 * @since 1.0
 */
public class PragmaTest {

	@SuppressWarnings("serial")
	@Test
	public void composition() throws IOException {
		Assert.assertEquals(Util.fileContents("pragmablock.txt"),
				Mustache.render("{{>sub}}", new HashMap<String, Object>() {
					{
						put("username", "Bob");
					}
				}, new ClassLoaderPartialFinder("mustache", PragmaTest.class.getClassLoader(), "")));
	}

	@Test
	public void filter1() {
		Map<String, Object> scope = new HashMap<String, Object>();
		FilterFunction up = new FilterFunction() {
			@Override
			public Object apply(Object argument) {
				return argument.toString().toUpperCase();
			}
		};
		scope.put("filter", up);
		scope.put("msg", "hello");

		Assert.assertEquals("HELLO", Mustache.render("{{%FILTERS}}{{msg | filter}}", scope));
	}

	@Test
	public void filter1Section() {
		Map<String, Object> scope = new HashMap<String, Object>();
		FilterFunction up = new FilterFunction() {
			@Override
			@SuppressWarnings("unchecked")
			public Object apply(Object argument) {
				Collections.sort((List<Integer>) argument);
				return argument;
			}
		};
		scope.put("sort", up);
		scope.put("list", Arrays.asList(2, 4, 1));

		Assert.assertEquals("124", Mustache.render("{{%FILTERS}}{{#list | sort}}{{.}}{{/list}}", scope));
	}

	@Test
	public void filter1Order() {
		Map<String, Object> scope = new HashMap<String, Object>();
		FilterFunction f1 = new FilterFunction() {
			@Override
			public Object apply(Object argument) {
				return argument + "1";
			}
		};
		FilterFunction f2 = new FilterFunction() {
			@Override
			public Object apply(Object argument) {
				return argument + "2";
			}
		};
		FilterFunction f3 = new FilterFunction() {
			@Override
			public Object apply(Object argument) {
				return argument + "3";
			}
		};
		scope.put("filter1", f1);
		scope.put("filter2", f2);
		scope.put("filter3", f3);
		scope.put("msg", "hello");

		Assert.assertEquals("hello123",
				Mustache.render("{{%FILTERS}}{{#msg | filter1 | filter2 | filter3}}{{.}}{{/msg}}", scope));
	}

	@Test
	public void filterorder() {
		Map<String, Object> scope = new HashMap<String, Object>();
		FilterFunction f1 = new FilterFunction() {
			@Override
			public Object apply(Object argument) {
				return argument + "1";
			}
		};
		FilterFunction f2 = new FilterFunction() {
			@Override
			public Object apply(Object argument) {
				return argument + "2";
			}
		};
		FilterFunction f3 = new FilterFunction() {
			@Override
			public Object apply(Object argument) {
				return argument + "3";
			}
		};
		scope.put("filter1", f1);
		scope.put("filter2", f2);
		scope.put("filter3", f3);
		scope.put("msg", "hello");

		Assert.assertEquals("hello123", Mustache.render("{{%FILTERS}}{{msg | filter1 | filter2 | filter3}}", scope));
	}
}
