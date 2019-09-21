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
import java.util.Queue;

import net.gcolin.common.collection.ArrayQueue;
import net.gcolin.mustache.MustacheContext;
import net.gcolin.mustache.MustacheException;

/**
 * Mustache file parser.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class Compiler {

	public static final String DEFAULT_SM = "{{";
	public static final String DEFAULT_EM = "}}";
	private static final int PRAGMA_BLOCK = 0x1;
	private static final int PRAGMA_FILTER = 0x2;

	private Compiler() {
	}

	/**
	 * Compile a mustache template into a node.
	 * 
	 * @param template  mustache template
	 * @param templates partials
	 * @return a Node
	 */
	public static Node compile(String template, MustacheContext templates) {
		return compile(template, templates, new CompileContext());
	}

	/**
	 * Compile a mustache template into a node.
	 * 
	 * @param template  mustache template
	 * @param templates partials
	 * @param context   compilation context
	 * @return a Node
	 */
	public static Node compile(String template, MustacheContext templates, CompileContext context) {
		int element = 0;
		StringBuilder str = new StringBuilder();
		StringBuilder str2 = new StringBuilder();
		Queue<List<Node>> currentNodes = new ArrayQueue<>();
		Queue<Section> sectionNames = new ArrayQueue<>();
		currentNodes.offer(new ArrayList<Node>());
		boolean in = false;
		boolean hasIn = false;
		boolean hasTxt = false;
		String start = null;
		int line = 1;
		int pragma = 0;

		for (int j = 0; j < template.length(); j++) {
			char ch = template.charAt(j);
			if (ch == '\n') {
				start = null;
				line++;

				if (!in) {
					String val = str.toString().trim();
					List<Node> nodes = currentNodes.peek();
					if (hasIn && !nodes.isEmpty() && nodes.get(nodes.size() - 1) instanceof VarNode) {
						appendStart(currentNodes, str.append(ch).toString());
					} else if ((hasIn && hasTxt) || !val.isEmpty() || (val.isEmpty() && !hasIn && !hasTxt)) {
						str2.append(str);
						str2.append(ch);
					}

					str.setLength(0);
				}
				hasIn = hasTxt = false;
				continue;
			}
			if (in) {
				if (context.em.charAt(element) == ch) {
					element++;
					if (context.em.length() == element) {
						element = 0;
						in = false;
						if (str.length() == 0) {
							throw new MustacheException("Empty mustache at line " + line);
						}

						switch (str.charAt(0)) {
						case '=':
							if (str.length() < 5 || str.charAt(str.length() - 1) != '=') {
								throw new MustacheException("Invalid delimiter at line " + line);
							}
							// change sm and em
							updateContextLimit(str, context);
							break;
						case '%':
							pragma |= addPragma(str.substring(1).trim());
							break;
						case '#':
							// start section
							currentNodes.offer(new ArrayList<Node>());
							sectionNames.offer(new Section(str.substring(1).trim(), start, line, SectionType.SECTION));
							break;
						case '^':
							// start section
							currentNodes.offer(new ArrayList<Node>());
							sectionNames
									.offer(new Section(str.substring(1).trim(), start, line, SectionType.SECTION_NOT));
							break;
						case '<':
							// composition
							assertPragma(pragma, PRAGMA_BLOCK);
							currentNodes.offer(new ArrayList<Node>());
							sectionNames.offer(new Section(str.substring(1).trim(), start, line, SectionType.COMPOSE));
							break;
						case '$':
							// define/insert
							assertPragma(pragma, PRAGMA_BLOCK);
							currentNodes.offer(new ArrayList<Node>());
							sectionNames.offer(new Section(str.substring(1).trim(), start, line, SectionType.DEFINE));
							break;
						case '/':
							// end section
							String section = str.substring(1).trim();
							if (sectionNames.isEmpty() || !section.equals(sectionNames.peek().getName())) {
								throw new MustacheException("Failed to close '" + section + "' tag at line " + line);
							}
							Section sec = sectionNames.poll();

							List<Node> innerNodes = currentNodes.poll();
							if (sec.line == line && sec.start != null && sec.start.length() > 0) {
								currentNodes.peek().add(new StringNode(sec.start));
							}
							if (sec.line != line && sec.start != null && !sec.start.equals(start)) {
								if (!sec.start.isEmpty()
										&& (currentNodes.size() > 1 || !currentNodes.peek().isEmpty())) {
									currentNodes.peek().add(new StringNode(sec.start));
								}
								if (start != null && !start.isEmpty()) {
									innerNodes.add(new StringNode(start));
								}
							}
							if (sec.type == SectionType.SECTION) {
								currentNodes.peek().add(buildSectionNode(sec.name, toSingle(innerNodes), false,
										context.clone(), (pragma & PRAGMA_FILTER) == PRAGMA_FILTER));
							} else if (sec.type == SectionType.SECTION_NOT) {
								currentNodes.peek().add(buildSectionNode(sec.name, toSingle(innerNodes), true,
										context.clone(), (pragma & PRAGMA_FILTER) == PRAGMA_FILTER));
							} else if (sec.type == SectionType.COMPOSE) {
								List<Node> defineNodes = new ArrayList<>();
								for (Node innerNode : innerNodes) {
									if (innerNode.getClass() == DefineNode.class) {
										defineNodes.add(innerNode);
									}
								}
								currentNodes.peek().add(new ComposeNode(section, toSingle(defineNodes)));
							} else if (sec.type == SectionType.DEFINE) {
								currentNodes.peek().add(new DefineNode(section, toSingle(innerNodes), sec.start));
							}

							break;
						case '>':
							// partial
							appendStart(currentNodes, start);
							String part = str.substring(1).trim();
							if (templates.has(part)) {
								currentNodes.peek().add(new PartialNode(part, start));
							}
							break;
						case '!':
							// comment
							break;
						case '&':
							// unescape
							appendStart(currentNodes, start);
							currentNodes.peek().add(buildVarNode(false, str.substring(1).trim(),
									(pragma & PRAGMA_FILTER) == PRAGMA_FILTER));
							break;
						case '{':
							// unescape
							appendStart(currentNodes, start);
							if (str.charAt(str.length() - 1) != '}') {
								ch = template.charAt(j + 1);
								if (ch == '}') {
									str.append(ch);
									j++;
								}
							}

							if (str.length() < 3 || str.charAt(str.length() - 1) != '}') {
								throw new MustacheException("Invalid escape at line " + line);
							}
							currentNodes.peek().add(buildVarNode(false, str.substring(1, str.length() - 1).trim(),
									(pragma & PRAGMA_FILTER) == PRAGMA_FILTER));
							break;
						default:
							appendStart(currentNodes, start);
							currentNodes.peek().add(buildVarNode(true, str.toString().trim(),
									(pragma & PRAGMA_FILTER) == PRAGMA_FILTER));
						}
						hasIn = true;
						str.setLength(0);
					}
				} else {
					if (element > 0) {
						for (int i = 0; i < element; i++) {
							str.append(context.em.charAt(element));
						}
						element = 0;
					}
					str.append(ch);
				}
			} else {
				if (context.sm.charAt(element) == ch) {
					element++;
					if (context.sm.length() == element) {
						element = 0;
						in = true;
						start = str.toString();
						String val = !hasIn ? start.trim() : start;
						if (!val.isEmpty()) {
							str2.append(str);
							hasTxt = true;
							start = "";
						}
						str.setLength(0);
						if (str2.length() > 0) {
							currentNodes.peek().add(new StringNode(str2.toString()));
						}
						str2.setLength(0);
					}
				} else {
					if (element > 0) {
						for (int i = 0; i < element; i++) {
							str.append(context.sm.charAt(element));
						}
						element = 0;
					}
					str.append(ch);
				}
			}
		}

		if (!sectionNames.isEmpty() || in) {
			throw new MustacheException("Incomplete mustache template");
		}

		str2.append(str);
		if (str2.length() > 0) {
			currentNodes.peek().add(new StringNode(str2.toString()));
		} else if (!currentNodes.peek().isEmpty()) {
			List<Node> nodes = currentNodes.peek();
			Node last = nodes.get(nodes.size() - 1);
			while (last instanceof SectionNode) {
				Node body = ((SectionNode) last).getBody();
				if (body == null) {
					break;
				} else {
					if (body instanceof CompositeNode) {
						last = ((CompositeNode) body).getNodes()[((CompositeNode) body).getNodes().length - 1];
					} else {
						last = body;
					}
					if (last instanceof StringNode) {
						String nodeValue = last.toString();
						int idx = nodeValue.length();
						for (; idx > 0; idx--) {
							char ch = nodeValue.charAt(idx - 1);
							if (ch != ' ' && ch != '\t') {
								break;
							}
						}
						((StringNode) last).setString(nodeValue.substring(0, idx));
					}
				}
			}
		}

		Node single = toSingle(currentNodes.poll());
		return single == null ? new StringNode("") : single;
	}

	private static void assertPragma(int pragma, int expect) {
		if ((pragma & expect) != expect) {
			throw new MustacheException("missing pragma tag");
		}

	}

	private static int addPragma(String pragma) {
		if ("BLOCKS".equals(pragma)) {
			return PRAGMA_BLOCK;
		}
		if ("FILTERS".equals(pragma)) {
			return PRAGMA_FILTER;
		}
		return 0;
	}

	protected static void appendStart(Queue<List<Node>> currentNodes, String start) {
		if (start != null && !start.isEmpty()) {
			currentNodes.peek().add(new StringNode(start));
		}
	}

	private static Node buildVarNode(boolean escaped, String name, boolean filter) {
		String[] filters = null;
		if (filter && name.contains("|")) {
			filters = extractFilters(name);
			name = name.substring(0, name.indexOf('|')).trim();
		}

		if (".".equals(name)) {
			if (escaped) {
				return new VarNode(name, filters);
			} else {
				return new UnescapeVarNode(name, filters);
			}
		} else if (name.indexOf('.') != -1) {
			if (escaped) {
				return new DotNameVarNode(name, filters);
			} else {
				return new UnescapeDotNameVarNode(name, filters);
			}
		} else {
			if (escaped) {
				return new NameVarNode(name, filters);
			} else {
				return new UnescapeNameVarNode(name, filters);
			}
		}
	}

	protected static String[] extractFilters(String name) {
		String[] filters;
		int prec = name.indexOf('|') + 1;
		List<String> filtersList = new ArrayList<>();
		while (prec < name.length()) {
			int split = name.indexOf('|', prec);
			if (split == -1) {
				split = name.length();
			}
			filtersList.add(name.substring(prec, split).trim());
			prec = split + 1;
		}
		filters = filtersList.toArray(new String[filtersList.size()]);
		return filters;
	}

	private static Node buildSectionNode(String name, Node body, boolean not, CompileContext context, boolean filter) {
		String[] filters = null;
		if (filter && name.contains("|")) {
			filters = extractFilters(name);
			name = name.substring(0, name.indexOf('|')).trim();
		}
		if (".".equals(name)) {
			return new SectionNode(name, body, not, context.clone(), filters);
		} else if (name.indexOf('.') != -1) {
			return new DotNameSectionNode(name, body, not, context.clone(), filters);
		} else {
			return new NameSectionNode(name, body, not, context.clone(), filters);
		}
	}

	private static Node toSingle(List<Node> list) {
		if (list.isEmpty()) {
			return null;
		}
		// concat string if possible
		StringNode sn = null;
		for (int i = 0; i < list.size(); i++) {
			Node current = list.get(i);
			if (current instanceof StringNode) {
				if (sn == null) {
					sn = (StringNode) current;
				} else {
					sn.setString(sn.toString() + current.toString());
					list.remove(i);
					i--;
				}
			} else {
				sn = null;
			}
		}
		if (list.size() == 1) {
			return list.get(0);
		}
		return new CompositeNode(list.toArray(new Node[list.size()]));
	}

	private static void updateContextLimit(StringBuilder str, CompileContext context) {

		int split = str.length() / 2 - 1;
		context.sm = str.substring(1, split + 1).trim();
		context.em = str.substring(str.length() - 1 - split, str.length() - 1).trim();
	}

	static class CompileContext implements Cloneable {
		String em = DEFAULT_EM;
		String sm = DEFAULT_SM;

		@Override
		public CompileContext clone() {
			try {
				return (CompileContext) super.clone();
			} catch (CloneNotSupportedException ex) {
				throw new MustacheException(ex);
			}
		}
	}

	static enum SectionType {
		SECTION, SECTION_NOT, COMPOSE, DEFINE
	}

	static class Section {
		String name;
		String start;
		int line;
		SectionType type;

		public Section(String name, String start, int line, SectionType type) {
			super();
			this.type = type;
			this.name = name;
			this.start = start;
			this.line = line;
		}

		public String getName() {
			return name.indexOf('|') == -1 ? name : name.substring(0, name.indexOf('|')).trim();
		}

	}
}
