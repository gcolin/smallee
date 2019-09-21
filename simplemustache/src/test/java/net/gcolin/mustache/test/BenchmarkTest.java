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

import org.junit.Ignore;
import org.junit.Test;

import com.samskivert.mustache.Template;

import net.gcolin.common.io.NullWriter;
import net.gcolin.mustache.CompiledMustache;
import net.gcolin.mustache.Mustache;

/**
 * Compare compilation with interpreter.
 * <p/>
 * User: sam Date: 5/14/11 Time: 9:28 PM
 */
@Ignore
public class BenchmarkTest {
	private static final int TIME = 1000;

	@Test
	public void testCompiler() throws IOException {
		System.out.println("complex.html compilations per second:");
		for (int i = 0; i < 3; i++) {
			{
				long start = System.currentTimeMillis();
				int total = 0;
				String content = Util.fileContents("complex.html");
				while (true) {
					Mustache.compile(content);
					total++;
					if (System.currentTimeMillis() - start > TIME) {
						break;
					}
				}
				System.out.println("Result: " + total * 1000 / TIME);
			}
		}
		System.out.println("complex.html compilations per second (jmustache):");
		for (int i = 0; i < 3; i++) {
			{
				long start = System.currentTimeMillis();
				int total = 0;
				String content = Util.fileContents("complex.html");
				while (true) {
					com.samskivert.mustache.Mustache.compiler().compile(content);
					total++;
					if (System.currentTimeMillis() - start > TIME) {
						break;
					}
				}
				System.out.println("Result: " + total * 1000 / TIME);
			}
		}
	}

	@Test
	public void testComplex() throws IOException {
		System.out.println("complex.html evaluations per millisecond:");
		for (int i = 0; i < 3; i++) {
			{
				String content = Util.fileContents("complex.html");
				CompiledMustache mustache = Mustache.compile(content);
				ComplexObject complexObject = new ComplexObject();
				mustache.render(complexObject);
				long start = System.currentTimeMillis();
				int total = 0;
				while (true) {
					mustache.render(complexObject, new NullWriter());
					total++;
					if (System.currentTimeMillis() - start > TIME) {
						break;
					}
				}
				System.out.println("Serial: " + total / TIME);
			}
		}
		System.out.println("complex.html evaluations per millisecond (jmustache):");
		for (int i = 0; i < 3; i++) {
			{
				String content = Util.fileContents("complex.html");
				Template mustache = com.samskivert.mustache.Mustache.compiler().compile(content);
				ComplexObject complexObject = new ComplexObject();
				mustache.execute(complexObject);
				long start = System.currentTimeMillis();
				int total = 0;
				while (true) {
					mustache.execute(complexObject, new NullWriter());
					total++;
					if (System.currentTimeMillis() - start > TIME) {
						break;
					}
				}
				System.out.println("Serial: " + total / TIME);
			}
		}
	}

	@Test
	public void testComplexFlapping() throws IOException {
		System.out.println("complex.html evaluations with 3 different objects per millisecond:");
		for (int i = 0; i < 3; i++) {
			{
				String content = Util.fileContents("complex.html");
				CompiledMustache mustache = Mustache.compile(content);
				ComplexObject complexObject = new ComplexObject();
				ComplexObject complexObject2 = new ComplexObject() {
				};
				ComplexObject complexObject3 = new ComplexObject() {
				};
				mustache.render(complexObject, new NullWriter());
				long start = System.currentTimeMillis();
				int total = 0;
				while (true) {
					mustache.render(total % 3 == 0 ? complexObject : total % 3 == 1 ? complexObject2 : complexObject3,
							new NullWriter());
					total++;
					if (System.currentTimeMillis() - start > TIME) {
						break;
					}
				}
				System.out.println("Serial: " + total / TIME);
			}
		}
		System.out.println("complex.html evaluations with 3 different objects per millisecond (jmustache):");
		for (int i = 0; i < 3; i++) {
			{
				String content = Util.fileContents("complex.html");
				Template mustache = com.samskivert.mustache.Mustache.compiler().compile(content);
				ComplexObject complexObject = new ComplexObject();
				ComplexObject complexObject2 = new ComplexObject() {
				};
				ComplexObject complexObject3 = new ComplexObject() {
				};
				mustache.execute(complexObject, new NullWriter());
				long start = System.currentTimeMillis();
				int total = 0;
				while (true) {
					mustache.execute(total % 3 == 0 ? complexObject : total % 3 == 1 ? complexObject2 : complexObject3,
							new NullWriter());
					total++;
					if (System.currentTimeMillis() - start > TIME) {
						break;
					}
				}
				System.out.println("Serial: " + total / TIME);
			}
		}
	}

}
