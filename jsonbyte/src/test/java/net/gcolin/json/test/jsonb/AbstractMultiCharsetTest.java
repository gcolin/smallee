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

package net.gcolin.json.test.jsonb;

import net.gcolin.common.io.ByteArrayOutputStream;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

/**
 * Charset tests.
 * 
 * @author Gael COLIN
 */
@RunWith(Parameterized.class)
public abstract class AbstractMultiCharsetTest {

  @Parameters
  public static List<Object[]> values() {
    return Arrays.asList(new Object[][] {{StandardCharsets.UTF_8}, {StandardCharsets.ISO_8859_1},
        {StandardCharsets.UTF_16}, {StandardCharsets.UTF_16LE}});
  }

  @Parameter
  public Charset charset;

  protected <T> T test0(Class<T> type, T obj, String expected) {
    JsonbConfig config = new JsonbConfig();
    config.withEncoding(charset.displayName());
    Jsonb jsonb = JsonbBuilder.create(config);
    String result = jsonb.toJson(obj);
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    jsonb.toJson(obj, bout);
    String str = new String(bout.toByteArray(), charset);
    bout.release();
    Assert.assertEquals(str, result);
    if (expected != null) {
      Assert.assertEquals(expected, result);
    }
    return jsonb.fromJson(result, type);
  }

}
