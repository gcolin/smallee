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
package net.gcolin.mustache.internal;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Gaël COLIN
 * @since 1.0
 */
public class DomScope implements Scope {

	private org.w3c.dom.Node node;

	public DomScope(Node node) {
		this.node = node;
	}

	@Override
	public Object get() {
		return node.getTextContent();
	}

	@Override
	public Object get(String name) {
		if (name == null) {
			return null;
		}
		org.w3c.dom.Node attr = node.getAttributes().getNamedItem(name);
		if (attr != null) {
			return attr.getNodeValue();
		}
		List<Object> result = new ArrayList<>();
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			org.w3c.dom.Node child = children.item(i);
			if (name.equals(child.getNodeName())) {
				NodeList schildren = child.getChildNodes();
				boolean found = false;
				for (int j = 0; j < schildren.getLength(); j++) {
					if (schildren.item(j).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
						result.add(child);
						found = true;
						break;
					}
				}
				if (!found) {
					result.add(child.getTextContent());
				}
			}
		}
		if (result.isEmpty()) {
			return null;
		} else if (result.size() == 1) {
			return result.get(0);
		}
		return result;
	}

	@Override
	public boolean has(String name) {
		return get(name) != null;
	}

}
