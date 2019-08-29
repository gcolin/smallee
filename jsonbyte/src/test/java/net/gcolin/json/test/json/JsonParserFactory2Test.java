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

package net.gcolin.json.test.json;

import net.gcolin.common.io.ByteArrayInputStream;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

/**
 * @author Gael COLIN
 * @since 1.0
 */
public class JsonParserFactory2Test {

  @Test
  public void testParserFactoryWithConfig() {
    Map<String, ?> config = new HashMap<String, Object>();
    JsonParserFactory parserFactory = Json.createParserFactory(config);
    JsonParser parser1 =
        parserFactory.createParser(new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8)));
    parser1.close();
    JsonParser parser2 = parserFactory.createParser(
        new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    parser2.close();
  }

}
