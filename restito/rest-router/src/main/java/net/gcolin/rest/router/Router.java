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

package net.gcolin.rest.router;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A fast rest like router. Algorithm in O(n) (i.e. read only one time each char
 * of the path). It is not recursive for not using a lot of stack memory.
 *
 * <p>
 * By default the path must be an ASCII char but it is extensible to any range
 * of char.
 * </p>
 *
 * <p>
 * Can be used for doing a "rest like servlet", a rest implementation, a server
 * implementation.
 * </p>
 * 
 * @author gcolin
 *
 * @param <T> the resource type
 */
public class Router<T extends HasPath> {

	private List<Router<T>> children;
	private T leaf;
	private boolean pathParam;
	private String segment;
	private List<VariableNode<T>> nodes;
	private String[] pathParamNames;

	/**
	 * Add a resource.
	 * 
	 * @param resource a resource with a path
	 * @return null
	 */
	public T add(T resource) {
		return add(resource, 0, false);
	}

	/**
	 * Add a resource.
	 * 
	 * @param resource   a resource with a path
	 * @param offsetInit the offset of the path
	 * @param silent     if {@code false} the method will throw an
	 *                   illegalArgumentException on a collision else {@code true}
	 *                   the method will return the collision or null
	 * @return the collision or null
	 */
	public T add(T resource, int offsetInit, boolean silent) {
		boolean cont = true;
		Router<T> current = this;
		int offset = offsetInit;
		T result = null;
		String path = resource.getPath();
		List<String> paramNames = new ArrayList<>();
		while (cont) {
			if (path.length() == offset) {
				result = endAdd(resource, silent, current, path);
				current.pathParamNames = paramNames.toArray(new String[paramNames.size()]);
				cont = false;
			} else {
				char ch = path.charAt(offset);
				if (ch == '{') {
					current.pathParam = true;
					if (current.nodes == null) {
						current.nodes = new ArrayList<>();
					}
					offset++;
					int newOffset = offset;
					while (path.charAt(newOffset) != '}') {
						newOffset++;
					}
					String variable = path.substring(offset, newOffset);
					newOffset++;
					if (newOffset < path.length() && path.charAt(newOffset) != '/') {

						throw new IllegalArgumentException("variable should end with /");
					}
					String regExpr = null;
					int split = variable.indexOf(':');
					if (split != -1) {
						regExpr = variable.substring(split + 1).trim();
						variable = variable.substring(0, split).trim();
					}
					VariableNode<T> selectedNode = lookupNode(current, regExpr);

					if (selectedNode == null) {
						selectedNode = createNode(current, regExpr);
					}

					paramNames.add(variable);

					if (newOffset == path.length() || selectedNode.terminal) {
						if (selectedNode.leaf != null) {
							if (!silent) {
								throw new IllegalArgumentException("two resources have the same path : " + path);
							} else {
								result = selectedNode.leaf;
							}
						} else {
							selectedNode.leaf = resource;
							selectedNode.pathParamNames = paramNames.toArray(new String[paramNames.size()]);
						}
						cont = false;
					} else {
						current = selectedNode.next;
						offset = newOffset;
					}
				} else {
					if (current.children == null) {
						current.children = new ArrayList<>();
					}
					int endSegment = path.indexOf('/', offset) + 1;
					if (endSegment == 0) {
						endSegment = path.length();
					}
					String segment = path.substring(offset, endSegment);
					boolean find = false;
					for(Router<T> r: current.children) {
						if(r.segment.equals(segment)) {
							current = r;
							find = true;
							break;
						}
					}
					if(!find) {
						Router<T> router = new Router<>();
						router.segment = segment;
						current.children.add(router);
						Collections.sort(current.children, (a,b) -> b.segment.length() - a.segment.length());
						current = router;
					}
					offset += segment.length();
				}
			}
		}

		return result;

	}

	private T endAdd(T resource, boolean silent, Router<T> current, String path) {
		T result = null;
		if (current.leaf != null) {
			if (!silent) {
				throw new IllegalArgumentException("two resources have the same path : " + path);
			} else {
				result = current.leaf;
			}
		} else {
			current.leaf = resource;
		}
		return result;
	}

	private VariableNode<T> lookupNode(Router<T> current, String regExpr) {
		VariableNode<T> selectedNode = null;
		for (int i = 0, l = current.nodes.size(); i < l; i++) {
			VariableNode<T> node = current.nodes.get(i);
			if (node.regExpr == null && regExpr == null || node.regExpr != null && node.regExpr.equals(regExpr)) {
				selectedNode = node;
				break;
			}
		}
		return selectedNode;
	}

	private VariableNode<T> createNode(Router<T> current, String regExpr) {
		VariableNode<T> selectedNode;
		selectedNode = new VariableNode<>(this);
		selectedNode.regExpr = regExpr;
		if (".+".equals(regExpr)) {
			selectedNode.terminal = true;
		} else if (regExpr != null) {
			selectedNode.pattern = Pattern.compile(regExpr);
		}
		current.nodes.add(selectedNode);

		Collections.sort(current.nodes, VariableNode.COMPARATOR);
		return selectedNode;
	}

	public T getResource(String path) {
		return getResource(path, 0);
	}

	/**
	 * Get a resource without path parameters.
	 * 
	 * @param path       the finding path
	 * @param offsetInit the offset
	 * @return a resource or {@code null}
	 */
	public T getResource(String path, int offsetInit) {
		RouterResponse<T> response = get(path, offsetInit, new RouterResponse<>());
		if (response == null) {
			return null;
		}
		return response.getResult();
	}

	/**
	 * Get a resource.
	 * 
	 * @param path the finding path
	 * @return a resource or {@code null}
	 */
	public RouterResponse<T> get(String path) {
		return get(path, 0, new RouterResponse<>());
	}

	/**
	 * Get a resource.
	 * 
	 * @param path       the finding path
	 * @param offsetInit the offset
	 * @param response   the response to fill
	 * @return a resource or {@code null}
	 */
	public RouterResponse<T> get(String path, int offsetInit, RouterResponse<T> response) {
		if (path == null) {
			return null;
		}
		boolean cont = true;
		Router<T> current = this;
		int offset = offsetInit;
		List<String> pathParamValues = new ArrayList<>();
		String[] currentPathParamNames = null;
		while (cont) {
			if (path.length() == offset) {
				cont = false;
				currentPathParamNames = current.pathParamNames;
				response.setResult(current.leaf);
			} else {
				Router<T> router = null;
				if (current.children != null) {
					for (int i = 0; i < current.children.size() && router == null; i++) {
						String cseg = current.children.get(i).segment;
						if(cseg.length() + offset > path.length()) {
							continue;
						}
						boolean ok = true;
						for (int j = 0; j < cseg.length(); j++) {
							if (cseg.charAt(j) != path.charAt(offset + j)) {
								ok = false;
								break;
							}
						}
						if (ok) {
							router = current.children.get(i);
						}
					}
				}
				if (router != null) {
					current = router;
					offset += router.segment.length();
				} else if (current.pathParam) {
					int newOffset = offset;
					while (path.length() != newOffset && path.charAt(newOffset) != '/') {
						newOffset++;
					}
					String value = path.substring(offset, newOffset);
					VariableNode<T> node;
					if (current.nodes.size() == 1) {
						node = current.nodes.get(0);
						if (node.pattern != null && !node.pattern.matcher(value).matches()) {
							cont = false;
						}
					} else {
						node = findNode(current, value);
					}
					if (node == null) {
						cont = false;
					} else if (cont) {
						if (node.terminal) {
							newOffset = path.length();
							value = path.substring(offset, newOffset);
						}
						if (path.length() == newOffset) {
							pathParamValues.add(value);
							cont = false;
							currentPathParamNames = node.pathParamNames;
							response.setResult(node.leaf);
						} else {
							pathParamValues.add(value);
							if (node.next == null) {
								cont = false;
							} else {
								offset = newOffset;
								current = node.next;
							}
						}
					}
				} else {
					cont = false;
				}
			}
		}
		if (response.getResult() == null) {
			return null;
		} else {
			if (!pathParamValues.isEmpty() && currentPathParamNames != null) {
				Map<String, List<String>> map = new HashMap<>();
				for (int i = 0; i < currentPathParamNames.length; i++) {
					List<String> list = map.get(currentPathParamNames[i]);
					if (list == null) {
						list = new ArrayList<>(1);
						map.put(currentPathParamNames[i], list);
					}
					list.add(pathParamValues.get(i));
				}
				response.setParams(map);
			}
			return response;
		}
	}

	private VariableNode<T> findNode(Router<T> current, String value) {
		for (int i = 0, l = current.nodes.size(); i < l; i++) {
			VariableNode<T> node = current.nodes.get(i);
			if (node.pattern == null || node.pattern.matcher(value).matches()) {
				return node;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return values().stream().map(x -> x.getPath() + "  " + x).collect(Collectors.joining("\n"));
	}

	/**
	 * Get all the resources.
	 * 
	 * @return all the resources of the router
	 */
	public Collection<T> values() {
		Collection<T> values = new ArrayList<>();
		final Router<T> root = this;
		Router<T> current = this;
		int index;

		boolean newdir;

		final Deque<Integer> pile = new ArrayDeque<>();
		final Deque<Router<T>> rp = new ArrayDeque<>();
		pile.offerLast(0);
		while (true) {
			// get children
			List<Router<T>> cchildren = getChildrens(current);

			index = pile.pollLast();
			// they may not have a new directory
			newdir = false;
			// explore the children
			if (index < cchildren.size()) {
				// can go deeper
				newdir = true;
				pile.offerLast(index + 1);
				pile.offerLast(0);
			}
			// go deeper
			if (newdir) {
				rp.offerLast(current);
				current = cchildren.get(index);

			} else {
				if (current.leaf != null) {
					values.add(current.leaf);
				}
				if (current.nodes != null) {
					for (VariableNode<T> v : current.nodes) {
						if (v.leaf != null) {
							values.add(v.leaf);
						}
					}
				}
				// back
				if (!current.equals(root)) {
					current = rp.pollLast();
				} else {
					// all files viewed
					break;
				}
			}
		}
		return values;
	}

	private List<Router<T>> getChildrens(Router<T> current) {
		List<Router<T>> routeChildren = new ArrayList<>();
		if (current.children != null) {
			for (Router<T> r : current.children) {
				if (r != null) {
					routeChildren.add(r);
				}
			}
		}
		if (current.nodes != null) {
			for (VariableNode<T> v : current.nodes) {
				if (v.next != null) {
					routeChildren.add(v.next);
				}
			}
		}
		return routeChildren;
	}

	private static class VariableNode<T extends HasPath> {
		String regExpr;
		boolean terminal;
		Pattern pattern;
		Router<T> next;
		T leaf;
		String[] pathParamNames;
		static Comparator<VariableNode<?>> COMPARATOR = (a1, a2) -> {
			if (a1.regExpr == null) {
				return 1;
			}
			if (a2.regExpr == null) {
				return -1;
			}
			return a1.regExpr.compareTo(a2.regExpr);
		};

		public VariableNode(Router<T> parent) {
			next = new Router<>();
		}
	}

}
