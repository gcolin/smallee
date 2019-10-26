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

package net.gcolin.common.test;

import net.gcolin.common.collection.ArraySet;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;

public class ArraySetTest {

	@Test
	public void emptyTest() {
		ArraySet<String> set = new ArraySet<>(new String[] {});
		Assert.assertTrue(set.isEmpty());
		Assert.assertEquals(0, set.size());
		Assert.assertFalse(set.iterator().hasNext());
	}

	@Test
	public void simpleTest() {
		ArraySet<String> set = new ArraySet<>(new String[] { "hello", "world" });
		Assert.assertFalse(set.isEmpty());
		Assert.assertEquals(2, set.size());
		Assert.assertTrue(set.contains("hello"));
		Assert.assertFalse(set.contains("w"));
		Assert.assertTrue(set.containsAll(Arrays.asList("hello", "world")));
		Assert.assertFalse(set.containsAll(Arrays.asList("hello2", "world")));
		try {
			Iterator<String> it = set.iterator();
			Assert.assertTrue(it.hasNext());
			Assert.assertEquals("hello", it.next());
			Assert.assertEquals("world", it.next());
			Assert.assertFalse(it.hasNext());
			set.iterator().next();
			Assert.fail();
		} catch (Exception e) {
			// must fail
		}
		Assert.assertArrayEquals(new String[] { "hello", "world" }, set.toArray());
		Assert.assertArrayEquals(new String[] { "hello", "world" }, set.toArray(new String[2]));
	}

	@Test
	public void retainsAllTest() {
		ArraySet<String> set = new ArraySet<>(new String[] { "hello", "world" });
		set.retainAll(Arrays.asList("hello"));
		Assert.assertEquals(1, set.size());
		Assert.assertEquals("[hello]", set.toString());
		set.retainAll(Arrays.asList("hello2"));
		Assert.assertEquals(1, set.size());
		Assert.assertEquals("[hello]", set.toString());
	}

	@Test
	public void addTest() {
		ArraySet<String> set = new ArraySet<>(new String[] {});
		set.add("hello");
		Assert.assertEquals(1, set.size());
		Assert.assertEquals("[hello]", set.toString());
		set.add("world");
		Assert.assertEquals(2, set.size());
		Assert.assertEquals("[hello, world]", set.toString());
	}

	@Test
	public void addAllTest() {
		ArraySet<String> set = new ArraySet<>(new String[] {});
		set.addAll(Arrays.asList("hello", "world"));
		set.addAll(Arrays.asList("hello", "world"));
		Assert.assertEquals(2, set.size());
		Assert.assertEquals("[hello, world]", set.toString());
	}

	@Test
	public void removeAllTest() {
		ArraySet<String> set = new ArraySet<>(new String[] { "hello", "world" });
		set.removeAll(Arrays.asList("hello", "world"));
		set.removeAll(Arrays.asList("hello", "world"));
		Assert.assertEquals(0, set.size());
		Assert.assertEquals("[]", set.toString());
	}

	@Test
	public void clearTest() {
		ArraySet<String> set = new ArraySet<>(new String[] { "hello", "world" });
		set.clear();
		Assert.assertEquals(0, set.size());
		Assert.assertEquals("[]", set.toString());
	}

}
