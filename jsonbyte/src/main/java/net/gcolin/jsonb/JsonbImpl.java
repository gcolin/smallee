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

package net.gcolin.jsonb;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbConfig;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.SerializationContext;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.json.stream.JsonParser;

import net.gcolin.common.io.Io;
import net.gcolin.common.io.StringReader;
import net.gcolin.common.io.StringWriter;
import net.gcolin.common.reflect.Reflect;
import net.gcolin.json.JsonGeneratorImpl;
import net.gcolin.jsonb.build.JContext;
import net.gcolin.jsonb.build.JNode;
import net.gcolin.jsonb.build.JNodeBuilder;

/**
 * The {@code JsonbImpl} implements {@code Jsonb}.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
@SuppressWarnings({"unchecked"})
public class JsonbImpl implements Jsonb, SerializationContext, DeserializationContext {

  private JsonProvider jsonProvider;
  private JNodeBuilder builder;
  private JsonGeneratorFactory generatorFactory;
  private Charset charset = StandardCharsets.UTF_8;

  /**
   * Create a JsonbImpl.
   * 
   * @param jsonProvider provider
   * @param config configuration
   */
  public JsonbImpl(JsonProvider jsonProvider, JsonbConfig config) {
    this.jsonProvider = jsonProvider;
    builder = new JNodeBuilder(config);

    Optional<Object> encoding = config.getProperty(JsonbConfig.ENCODING);
    if (encoding.isPresent()) {
      charset = Charset.forName((String) encoding.get());
    }

    Map<String, Object> jsonpConfig = new HashMap<>();
    Optional<Object> formatting = config.getProperty(JsonbConfig.FORMATTING);
    if (formatting.isPresent() && (Boolean) formatting.get()) {
      jsonpConfig.put(JsonGenerator.PRETTY_PRINTING, true);
    }
    generatorFactory = jsonProvider.createGeneratorFactory(jsonpConfig);
  }

  @Override
  public void close() throws Exception {
	  // nothing.
  }

  @Override
  public <T> T fromJson(String str, Class<T> type) {
    return fromJson(new StringReader(str), type);
  }

  @Override
  public <T> T fromJson(String str, Type runtimeType) {
    return fromJson(new StringReader(str), runtimeType);
  }

  @Override
  public <T> T fromJson(Reader reader, Class<T> type) {
    JsonParser parser = jsonProvider.createParser(reader);
    return deserialize(type, parser);
  }

  @Override
  public <T> T fromJson(Reader reader, Type runtimeType) {
    JsonParser parser = jsonProvider.createParser(reader);
    return deserialize(runtimeType, parser);
  }

  @Override
  public <T> T fromJson(InputStream stream, Class<T> type) {
    JsonParser parser = jsonProvider.createParser(stream);
    return deserialize(type, parser);
  }

  @Override
  public <T> T fromJson(InputStream stream, Type runtimeType) {
    JsonParser parser = jsonProvider.createParser(stream);
    return deserialize(runtimeType, parser);
  }

  @Override
  public String toJson(Object object) {
    StringWriter sw = new StringWriter();
    JsonGenerator jg = generatorFactory.createGenerator(sw);
    try {
      serialize(object, jg);
      jg.flush();
      return sw.toString();
    } finally {
      Io.close(sw);
    }
  }

  @Override
  public String toJson(Object object, Type runtimeType) {
    return toJson(object);
  }

  @Override
  public void toJson(Object object, Writer writer) {
    JsonGenerator jg = generatorFactory.createGenerator(writer);
    serialize(object, jg);
    jg.flush();
  }

  @Override
  public void toJson(Object object, Type runtimeType, Writer writer) {
    toJson(object, writer);
  }

  @Override
  public void toJson(Object object, OutputStream stream) {
    JsonGenerator jg = generatorFactory.createGenerator(stream, charset);
    serialize(object, jg);
    jg.flush();
  }

  @Override
  public void toJson(Object object, Type runtimeType, OutputStream stream) {
    toJson(object, stream);
  }

  @Override
  public <T> T deserialize(Class<T> clazz, JsonParser parser) {
    return deserialize((Type) clazz, parser);
  }

  @Override
  public <T> T deserialize(Type type, JsonParser parser) {
    JNode node = builder.build(null, (Class<Object>) Reflect.toClass(type), type, null, null, new JContext());
    return (T) node.getDeserializer().deserialize(parser.next(), null, parser, this, type);
  }

  @Override
  public <T> void serialize(String key, T object, JsonGenerator generator) {
    Class<?> clazz = object.getClass();
    JNode node = builder.build(null, (Class<Object>) clazz, clazz, null, null, new JContext());
    node.getSerializer().serialize(key, object, generator, this);
  }

  @Override
  public <T> void serialize(T object, JsonGenerator generator) {
    Class<?> clazz = object.getClass();
    JNode node = builder.build(null, (Class<Object>) clazz, clazz, null, null, new JContext());
    if (generator instanceof JsonGeneratorImpl) {
      node.getSerializer().serialize(object, (JsonGeneratorImpl) generator, this);
    } else {
      node.getSerializer().serialize(object, generator, this);
    }
  }

}
