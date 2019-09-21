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

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import net.gcolin.mustache.Mustache;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * @author Gaël COLIN
 * @since 1.0
 */
public class DomTest {

	@Test
	public void test() throws Exception {
		InputStream in = null;
		try {
			in = this.getClass().getClassLoader().getResourceAsStream("domtest.xml");
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
			String out = Mustache.render("<h1>{{groupId}}:{{artifactId}}:{{versioning.release}}</h1>",
					doc.getDocumentElement());
			Assert.assertEquals("<h1>net.gcolin:common:1.0</h1>", out);
			out = Mustache.render("{{attr}}", doc.getDocumentElement());
			Assert.assertEquals("hello", out);
			out = Mustache.render("{{#versioning.versions.version}}{{.}}{{/versioning.versions.version}}",
					doc.getDocumentElement());
			Assert.assertEquals("1.02.0", out);
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

}
