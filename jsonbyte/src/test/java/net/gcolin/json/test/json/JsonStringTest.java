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
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU General Public License
 * Version 2 only ("GPL") or the Common Development and Distribution License("CDDL") (collectively,
 * the "License"). You may not use this file except in compliance with the License. You can obtain a
 * copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html or
 * packager/legal/LICENSE.txt. See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each file and include the
 * License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception: Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License file that accompanied
 * this code.
 *
 * Modifications: If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s): If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include this software in
 * this distribution under the [CDDL or GPL Version 2] license." If you don't indicate a single
 * choice of license, a recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its licensees as
 * provided above. However, if you add GPL Version 2 code and therefore, elected the GPL Version 2
 * license, then the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package net.gcolin.json.test.json;


import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.json.JsonString;

/**
 * JSON String tests.
 * 
 * @author Jitendra Kotamraju
 */
public class JsonStringTest {

  @Test
  public void testToString() throws Exception {
    escapedString("");
    escapedString("abc");
    escapedString("abc\f");
    escapedString("abc\na");
    escapedString("abc\tabc");
    escapedString("abc\n\tabc");
    escapedString("abc\n\tabc\r");
    escapedString("\n\tabc\r");
    escapedString("\bab\tb\rc\\\"\ftesting1234");
    escapedString("\f\babcdef\tb\rc\\\"\ftesting1234");
    escapedString("abc\"\\/abc");
  }

  @Test
  public void testEquals() throws Exception {
    JsonArray exp = Json.createArrayBuilder().add("hello").add("hello").add("h").add(true).build();
    JsonString s1 = exp.getJsonString(0);
    JsonString s2 = exp.getJsonString(1);
    JsonString s3 = exp.getJsonString(2);
    Assert.assertEquals(s1.hashCode(), s2.hashCode());
    Assert.assertEquals(s1, s2);
    Assert.assertNotEquals(s1, s3);
    Assert.assertNotEquals(s1, exp.get(3));
    Assert.assertEquals(s1.getChars(), s2.getChars());
    Assert.assertFalse(s1.equals(null));
    Assert.assertTrue(s1.equals(s1));
  }

  void escapedString(String str) throws Exception {
    JsonArray exp = Json.createArrayBuilder().add(str).build();
    String parseStr = "[" + exp.get(0).toString() + "]";
    JsonReader jr = Json.createReader(new StringReader(parseStr));
    JsonArray got = jr.readArray();
    Assert.assertEquals(exp, got);
    jr.close();
  }

}
