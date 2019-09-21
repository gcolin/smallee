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

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: spullara Date: 1/17/12 Time: 8:17 PM To
 * change this template use File | Settings | File Templates.
 */
public class ComplexObject {
	public String header = "Colors";
	public List<Color> item = Arrays.asList(new Color("red", true, "#Red"), new Color("green", false, "#Green"),
			new Color("blue", false, "#Blue"));

	public boolean list() {
		return item.size() != 0;
	}

	public boolean empty() {
		return item.size() == 0;
	}

	public static class Color {
		boolean link() {
			return !current;
		}

		Color(String name, boolean current, String url) {
			this.name = name;
			this.current = current;
			this.url = url;
		}

		public String name;
		public boolean current;
		public String url;
	}
}
