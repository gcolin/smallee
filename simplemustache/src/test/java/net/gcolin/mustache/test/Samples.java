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

import net.gcolin.mustache.ClassLoaderPartialFinder;
import net.gcolin.mustache.CompiledMustache;
import net.gcolin.mustache.FilePartialFinder;
import net.gcolin.mustache.Mustache;
import net.gcolin.mustache.UrlPartialFinder;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import net.gcolin.mustache.StringFunction;
import net.gcolin.mustache.UrlFunction;
import net.gcolin.mustache.WrapperFunction;

/**
 * Some samples.
 *
 * @author Gaël COLIN
 * @since 1.0
 */
public class Samples {

	public static class Version {

		public String name = "1.0";
	}

	public static class Obj {

		public String name = "simple mustache";
		public Version version = new Version();
		WrapperFunction upper = new WrapperFunction() {
			@Override
			public Object apply(String text, StringFunction render) {
				return render.apply(text).toUpperCase();
			}
		};
	}

	/**
	 * A very simple sample.
	 */
	public void withoutPartial() {
		CompiledMustache compiled = Mustache.compile("Hello {{name}} {{#version}}{{name}}{{/version}}!");
		System.out.println(compiled.render(new Obj()));
	}

	/**
	 * Use a partial finder based on the classpath.
	 */
	public void withPartialInClasspath() {
		CompiledMustache compiled = Mustache.compile("Hello {{>printname}}",
				new ClassLoaderPartialFinder(".txt", this.getClass().getClassLoader(), ""));
		System.out.println(compiled.render(new Obj()));
	}

	/**
	 * Use a partial finder based on a directory.
	 */
	public void withPartialInFile() {
		CompiledMustache compiled = Mustache.compile("Hello {{>printname}}",
				new FilePartialFinder(".txt", Paths.get("src/test/resources")));
		System.out.println(compiled.render(new Obj()));
	}

	/**
	 * Use a partial finder based on URL.
	 */
	public void withPartialInUrl() {
		CompiledMustache compiled = Mustache.compile("Hello {{>printname}}",
				new UrlPartialFinder(".txt", new UrlFunction() {
					@Override
					public URL get(String name) {
						try {
							return new File("src/test/resources/" + name).toURI().toURL();
						} catch (Exception ex) {
							throw new RuntimeException(ex);
						}
					}
				}, ""));
		System.out.println(compiled.render(new Obj()));
	}

	/**
	 * Use the upper function defined in Obj.
	 */
	public void withFunction() {
		CompiledMustache compiled = Mustache.compile("Hello {{#upper}}{{name}}{{/upper}}!");
		System.out.println(compiled.render(new Obj()));
	}

	/**
	 * Run all samples.
	 *
	 * @param args args
	 */
	public static void main(String[] args) {
		new Samples().withoutPartial();
		new Samples().withPartialInClasspath();
		new Samples().withPartialInFile();
		new Samples().withPartialInUrl();
		new Samples().withFunction();
	}

}
