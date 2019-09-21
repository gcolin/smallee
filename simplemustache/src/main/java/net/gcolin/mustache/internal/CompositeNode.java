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

/**
 * Mustache multiple node in one.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class CompositeNode extends Node {

	private Node[] nodes;

	public CompositeNode(Node[] nodes) {
		this.nodes = nodes;
	}

	@Override
	public void write(Context ctx) {
		for (int i = 0; i < nodes.length; i++) {
			nodes[i].write(ctx);
		}
	}

	public Node[] getNodes() {
		return nodes;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < nodes.length; i++) {
			str.append(nodes[i].toString());
		}
		return str.toString();
	}

}
