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

import net.gcolin.common.io.FastInputStreamReader;
import net.gcolin.common.io.FastOutputStreamWriter;
import net.gcolin.common.io.Io;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

/**
 * The {@code JsonFactoryImpl} class can be all the factories of Json API.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JsonFactoryImpl
		implements JsonGeneratorFactory, JsonWriterFactory, JsonBuilderFactory, JsonParserFactory, JsonReaderFactory {

	private Map<String, ?> config;
	private JsonProvider provider;

	public JsonFactoryImpl(Map<String, ?> config, JsonProvider provider) {
		this.config = config;
		this.provider = provider;
	}

	@Override
	public JsonGenerator createGenerator(Writer wr) {
		return JsonGeneratorImpl.take(wr);
	}

	@Override
	public JsonGenerator createGenerator(OutputStream out) {
		return createGenerator(out, StandardCharsets.UTF_8);
	}

	@Override
	public JsonGenerator createGenerator(OutputStream out, Charset charset) {
		return createGenerator(Io.writer(out, charset.name()));
	}

	@Override
	public JsonWriter createWriter(Writer wr) {
		return new JsonWriterImpl(wr, this);
	}

	@Override
	public JsonWriter createWriter(OutputStream out) {
		return createWriter(new FastOutputStreamWriter(out, StandardCharsets.UTF_8.name()));
	}

	@Override
	public JsonWriter createWriter(OutputStream out, Charset charset) {
		return createWriter(Io.writer(out, charset.name()));
	}

	@Override
	public JsonObjectBuilder createObjectBuilder() {
		return new JsonObjectBuilderImpl();
	}

	@Override
	public JsonArrayBuilder createArrayBuilder() {
		return new JsonArrayBuilderImpl();
	}

	@Override
	public JsonParser createParser(Reader paramReader) {
		return JsonParserImpl.take(paramReader);
	}

	@Override
	public JsonParser createParser(InputStream in) {
		return JsonParserImpl.take(in);
	}

	@Override
	public JsonParser createParser(JsonObject paramJsonObject) {
		return new StructureJsonParser(paramJsonObject);
	}

	@Override
	public JsonParser createParser(JsonArray paramJsonArray) {
		return new StructureJsonParser(paramJsonArray);
	}

	@Override
	public JsonParser createParser(InputStream in, Charset charset) {
		return JsonParserImpl.take(Io.reader(in, charset.name()));
	}

	@Override
	public JsonReader createReader(Reader reader) {
		return new JsonReaderImpl(reader);
	}

	@Override
	public JsonReader createReader(InputStream in) {
		return new JsonReaderImpl(new FastInputStreamReader(in));
	}

	@Override
	public JsonReader createReader(InputStream in, Charset charset) {
		return new JsonReaderImpl(Io.reader(in, charset.name()));
	}

	@Override
	public Map<String, ?> getConfigInUse() {
		return config;
	}

	@Override
	public JsonArrayBuilder createArrayBuilder(Collection<?> collection) {
		return provider.createArrayBuilder(collection);
	}
	
	@Override
	public JsonArrayBuilder createArrayBuilder(JsonArray array) {
		return provider.createArrayBuilder(array);
	}
	
	@Override
	public JsonObjectBuilder createObjectBuilder(JsonObject object) {
		return provider.createObjectBuilder(object);
	}
	
	@Override
	public JsonObjectBuilder createObjectBuilder(Map<String, Object> object) {
		return provider.createObjectBuilder(object);
	}

}
