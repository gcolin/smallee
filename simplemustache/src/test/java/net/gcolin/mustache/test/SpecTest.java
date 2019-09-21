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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.junit.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

import net.gcolin.mustache.Mustache;
import net.gcolin.mustache.MustacheException;
import net.gcolin.mustache.StringFunction;
import net.gcolin.mustache.WrapperFunction;

/**
 * @author Gaël COLIN
 * @since 1.0
 */
public class SpecTest {

	private Logger log = Logger.getLogger(SpecTest.class.getName());

	@Test
	public void comments() throws IOException {
		test("comments");
	}

	@Test
	public void delimiters() throws IOException {
		test("delimiters");
	}

	@Test
	public void interpolation() throws IOException {
		test("interpolation");
	}

	@Test
	public void inverted() throws IOException {
		test("inverted");
	}

	@Test
	public void sections() throws IOException {
		test("sections");
	}

	@Test
	public void partials() throws IOException {
		test("partials");
	}

	@Test
	public void lambdas() throws IOException {
		test("lambdas");
	}

	@SuppressWarnings("unchecked")
	private void test(String name) throws IOException {
		log.info("--> " + name);

		Yaml yaml = new Yaml(new CodeConstructor());
		Map<String, Object> map = null;
		InputStream in = null;
		try {
			in = SpecTest.class.getClassLoader().getResourceAsStream("spec/" + name + ".yml");
			map = (Map<String, Object>) yaml.load(in);
		} finally {
			if (in != null) {
				in.close();
			}
		}
		int idx = 0;
		Collection<Map<String, Object>> all = (Collection<Map<String, Object>>) map.get("tests");
		for (Map<String, Object> test : all) {
			log.info("[" + (++idx) + "] " + (String) test.get("name"));
			Assert.assertEquals((String) test.get("name") + " : " + (String) test.get("desc"), test.get("expected"),
					Mustache.render((String) test.get("template"), test.get("data"),
							(Map<String, String>) test.get("partials")));
		}
	}

	class CodeConstructor extends Constructor {

		public CodeConstructor() {
			this.yamlConstructors.put(new Tag("!code"), new ConstructCode());
		}

		private class ConstructCode extends AbstractConstruct {

			public Object construct(Node node) {
				MappingNode nn = (MappingNode) node;
				int idx = 0;
				for (NodeTuple nt : nn.getValue()) {
					ScalarNode sn = (ScalarNode) nt.getKeyNode();
					if ("js".equals(sn.getValue())) {
						final ScalarNode vv = (ScalarNode) nt.getValueNode();
						ScriptEngineManager factory = new ScriptEngineManager();
						final ScriptEngine engine = factory.getEngineByName("javascript");
						try {
							return new JsFunction(vv.getValue(), "fun" + idx, engine);
						} catch (Exception ex) {
							throw new MustacheException(ex);
						}
					}
					idx++;
				}
				return null;
			}
		}
	}

	private static class JsFunction implements WrapperFunction {

		private String name;
		private ScriptEngine engine;

		public JsFunction(String code, String name, ScriptEngine engine) throws ScriptException {
			this.name = name;
			this.engine = engine;
			SimpleBindings bindings = new SimpleBindings();
			bindings.put(name, engine.eval(code));
			engine.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
		}

		@Override
		public Object apply(String text, StringFunction render) {
			try {
				SimpleBindings bindings = new SimpleBindings();
				bindings.put("text", text);
				bindings.put("render", render.getClass().getMethod("apply", String.class));
				Object obj = engine.eval(name + "(text, render)", bindings);
				if (obj instanceof Double && (Double) obj % 1 == 0) {
					return ((Double) obj).intValue();
				}
				return obj;
			} catch (NoSuchMethodException | ScriptException ex) {
				throw new MustacheException(ex);
			}
		}

	}
}
