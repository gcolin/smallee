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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;

import org.junit.Assert;
import org.junit.Test;

import net.gcolin.common.collection.Collections2;

public class Collections2Test {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testToList() {
		ArrayList<String> list = new ArrayList<>();
		list.add("hello");
		list.add("world");
		Assert.assertEquals(list, Collections2.toList(list.iterator()));

		Assert.assertTrue(Collections2.toList((Iterator) null).isEmpty());

		Assert.assertEquals(Arrays.asList("world"), Collections2.toList(list.iterator(), x -> x.equals("world")));
		Assert.assertTrue(Collections2.toList(null, x -> x.equals("world")).isEmpty());

		Vector<String> vector = new Vector<String>();
		vector.add("hello");
		vector.add("world");
		Assert.assertEquals(vector, Collections2.toList(vector.elements()));
	}

	@Test
	public void testToEnumeration() {
		ArrayList<String> list = new ArrayList<>();
		list.add("hello");
		list.add("world");

		Enumeration<String> en = Collections2.toEnumeration(list.iterator(), x -> x.toUpperCase());

		Assert.assertTrue(en.hasMoreElements());
		Assert.assertEquals("HELLO", en.nextElement());
		Assert.assertTrue(en.hasMoreElements());
		Assert.assertEquals("WORLD", en.nextElement());
		Assert.assertFalse(en.hasMoreElements());

		list.add("");
		en = Collections2.toEnumeration(list.iterator(), x -> x.toUpperCase(), x -> !x.isEmpty());

		Assert.assertTrue(en.hasMoreElements());
		Assert.assertEquals("HELLO", en.nextElement());
		Assert.assertTrue(en.hasMoreElements());
		Assert.assertEquals("WORLD", en.nextElement());
		Assert.assertFalse(en.hasMoreElements());

		try {
			en.nextElement();
			Assert.fail();
		} catch (NoSuchElementException ex) {
			// ok
		}
	}

	@Test
	public void testToSet() {
		Set<String> set = new HashSet<>();
		set.add("hello");
		set.add("world");

		Assert.assertEquals(set, Collections2.toSet("hello", "world"));
		Assert.assertEquals(set, Collections2.toSet(set.iterator()));
	}

	@Test
	public void testIndexOf() {
		Integer[] arr = { 1, 2, 3 };

		Assert.assertEquals(1, Collections2.indexOf(arr, x -> Integer.valueOf(2).equals(x)));
		Assert.assertEquals(-1, Collections2.indexOf(arr, x -> Integer.valueOf(4).equals(x)));
	}
	
	@Test
	public void testWrapToSet() {
		List<String> list = new ArrayList<>();
		list.add("hello");
		list.add("world");
		
		Set<String> set = Collections2.wrapToSet(list);
		Assert.assertEquals(2, set.size());
		Assert.assertFalse(set.isEmpty());
		Assert.assertTrue(set.contains("hello"));
	}

	@Test
	public void testRemoveToArray() {
		String[] array = { "hello", "world", "!" };
		String[] array2 = Collections2.removeToArray(array, "world");
		Assert.assertEquals(2, array2.length);
		Assert.assertEquals("hello", array2[0]);
		Assert.assertEquals("!", array2[1]);

		array2 = Collections2.removeToArray(array, "hello");
		Assert.assertEquals(2, array2.length);
		Assert.assertEquals("world", array2[0]);
		Assert.assertEquals("!", array2[1]);

		array2 = Collections2.removeToArray(array, "!");
		Assert.assertEquals(2, array2.length);
		Assert.assertEquals("hello", array2[0]);
		Assert.assertEquals("world", array2[1]);

		array2 = Collections2.removeToArray(array, "");
		Assert.assertEquals(3, array2.length);
		Assert.assertEquals("hello", array2[0]);
		Assert.assertEquals("world", array2[1]);
		Assert.assertEquals("!", array2[2]);
	}

	@Test
	public void testMerge() {
		String[] all = { "hello", "world", "!" };
		Assert.assertArrayEquals(all, Collections2.merge(new String[] { "hello" }, new String[] { "world", "!" }));

		Assert.assertArrayEquals(all, Collections2.merge(new String[] { "hello", "world", "!" }, new String[0]));
		Assert.assertArrayEquals(all, Collections2.merge(new String[] { "hello", "world", "!" }, null));

		Assert.assertArrayEquals(all, Collections2.merge(new String[0], new String[] { "hello", "world", "!" }));
		Assert.assertArrayEquals(all, Collections2.merge(null, new String[] { "hello", "world", "!" }));
	}

	@Test
	public void testAddToArray() {
		String[] array = { "hello" };
		String[] newArray = Collections2.addToArray(array, "world");
		Assert.assertEquals(2, newArray.length);
		Assert.assertEquals("hello", newArray[0]);
		Assert.assertEquals("world", newArray[1]);
	}

	@Test
	public void testSafeFill() {
		Service[] array = Collections2.safeFillServiceLoaderAsArray(this.getClass().getClassLoader(), Service.class,
				new Service1());

		Assert.assertEquals(2, array.length);
		Assert.assertTrue(array[1] instanceof Service1);
		Assert.assertTrue(array[0] instanceof Service2);
	}
}
