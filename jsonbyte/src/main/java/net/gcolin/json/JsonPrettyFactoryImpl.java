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

package net.gcolin.json;

import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;

import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;

import net.gcolin.common.io.Io;

/**
 * The {@code JsonPrettyFactoryImpl} class can be all the factories of Json API.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JsonPrettyFactoryImpl
    extends JsonFactoryImpl {

  public JsonPrettyFactoryImpl(Map<String, ?> config, JsonProvider provider) {
    super(config, provider);
  }

  @Override
  public JsonGenerator createGenerator(Writer wr) {
    return JsonPrettyGeneratorImpl.take(wr);
  }

  @Override
  public JsonGenerator createGenerator(OutputStream out) {
    return JsonPrettyGeneratorImpl.take(out);
  }

  @Override
  public JsonGenerator createGenerator(OutputStream out, Charset charset) {
    return createGenerator(Io.writer(out, charset.name()));
  }

}
