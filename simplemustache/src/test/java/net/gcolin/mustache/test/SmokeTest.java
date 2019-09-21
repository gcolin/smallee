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

import net.gcolin.mustache.CompiledMustache;
import net.gcolin.mustache.Mustache;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.gcolin.mustache.StringFunction;
import net.gcolin.mustache.WrapperFunction;

/**
 * @author Gaël COLIN
 * @since 1.0
 */
@SuppressWarnings("unused")
public class SmokeTest {

	@Test
	public void testTypicalMap() throws IOException {
		Map<String, Object> scope = new HashMap<String, Object>();
		scope.put("name", "Chris");
		scope.put("value", 10000);
		scope.put("taxed_value", 10000 - (10000 * 0.4));
		scope.put("in_ca", true);

		Assert.assertEquals(Util.fileContents("simple.txt"), Mustache.render(Util.fileContents("simple.html"), scope));
	}

	@Test
	public void testTypicalField() throws IOException {
		Object scope = new Object() {
			public String name = "Chris";
			public int value = 10000;
			public double taxed_value = 10000 - (10000 * 0.4);
			public boolean in_ca = true;
		};

		Assert.assertEquals(Util.fileContents("simple.txt"), Mustache.render(Util.fileContents("simple.html"), scope));
	}

	@Test
	public void testTypicalGetter() throws IOException {
		Object scope = new Object() {
			public String name() {
				return "Chris";
			}

			public int value() {
				return 10000;
			}

			public double taxed_value() {
				return 10000 - (10000 * 0.4);
			}

			public boolean in_ca() {
				return true;
			}
		};

		Assert.assertEquals(Util.fileContents("simple.txt"), Mustache.render(Util.fileContents("simple.html"), scope));
	}

	@Test
	public void testTypicalGetter2() throws IOException {
		Object scope = new Object() {
			public String getName() {
				return "Chris";
			}

			public int getValue() {
				return 10000;
			}

			public double getTaxed_value() {
				return 10000 - (10000 * 0.4);
			}

			public boolean isIn_ca() {
				return true;
			}
		};

		Assert.assertEquals(Util.fileContents("simple.txt"), Mustache.render(Util.fileContents("simple.html"), scope));
	}

	@Test
	public void testUnescape() throws IOException {
		Map<String, Object> scope = new HashMap<String, Object>();
		scope.put("name", "Chris");
		scope.put("company", "<b>GitHub</b>");

		Assert.assertEquals(Util.fileContents("unescape.txt"),
				Mustache.render(Util.fileContents("unescape.html"), scope));
	}

	@Test
	public void testSectionBool() throws IOException {
		Map<String, Object> scope = new HashMap<String, Object>();
		scope.put("person", false);

		Assert.assertEquals(Util.fileContents("sectionBool.txt"),
				Mustache.render(Util.fileContents("sectionBool.html"), scope));
	}

	public static class Repo {
		public String name;

		public Repo(String name) {
			this.name = name;
		}
	}

	@Test
	public void testNonEmptyList() throws IOException {
		Map<String, Object> scope = new HashMap<String, Object>();
		scope.put("repo", Arrays.asList(new Repo("resque"), new Repo("hub"), new Repo("rip")));

		Assert.assertEquals(Util.fileContents("sectionNonEmptyList.txt"),
				Mustache.render(Util.fileContents("sectionNonEmptyList.html"), scope));
	}

	@Test
	public void testLambda() throws IOException {
		Object scope = new Object() {
			public String name = "Willy";
			public WrapperFunction wrapped = new WrapperFunction() {
				@Override
				public Object apply(String text, StringFunction fun) {
					return "<b>" + fun.apply(text) + "</b>";
				}
			};
		};
		Assert.assertEquals(Util.fileContents("sectionLambda.txt"),
				Mustache.render(Util.fileContents("sectionLambda.html"), scope));
	}

	@Test
	public void testSectionNonFalse() throws IOException {
		Map<String, Object> scope = new HashMap<String, Object>();
		scope.put("person?", new Object() {
			public String getName() {
				return "Jon";
			}
		});

		Assert.assertEquals(Util.fileContents("sectionNonFalse.txt"),
				Mustache.render(Util.fileContents("sectionNonFalse.html"), scope));
	}

	@Test
	public void testSectionInverted() throws IOException {
		Map<String, Object> scope = new HashMap<String, Object>();
		scope.put("person?", Collections.EMPTY_LIST);

		Assert.assertEquals(Util.fileContents("sectionInverted.txt"),
				Mustache.render(Util.fileContents("sectionInverted.html"), scope));
	}

	@Test
	public void testComment() throws IOException {
		Assert.assertEquals(Util.fileContents("comment.txt"), Mustache.render(Util.fileContents("comment.html"), null));
	}

	@Test
	public void testDelimiter() throws IOException {
		Assert.assertEquals("{{##", Mustache.render("{{=## ##=}}{{##={{ }}=####", null));
	}

	@SuppressWarnings("serial")
	@Test
	public void testPartial() throws IOException {
		Map<String, Object> scope = new HashMap<String, Object>();
		scope.put("names", Arrays.asList(new Repo("hub")));

		CompiledMustache mustache = Mustache.compile("<h2>Names</h2>{{#names}}{{> user}}{{/names}}",
				new HashMap<String, String>() {
					{
						put("user", "<strong>{{name}}</strong>");
					}
				});
		Assert.assertEquals("<h2>Names</h2><strong>hub</strong>", mustache.render(scope));
	}

	@Test
	public void testEmpty() throws IOException {
		Assert.assertEquals("", Mustache.render("", null));
	}
}
