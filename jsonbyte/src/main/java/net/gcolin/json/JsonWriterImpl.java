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

import java.io.Writer;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerationException;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

import net.gcolin.common.io.Io;

/**
 * The {@code JsonWriterImpl} class writes a Json
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JsonWriterImpl implements JsonWriter {

	private JsonGenerator jg;
	private Writer source;

	/**
	 * Create a JsonWriter.
	 * 
	 * @param source a writer
	 * @param factory a JsonFactory
	 */
	public JsonWriterImpl(Writer source, JsonGeneratorFactory factory) {
		this.source = source;
		if (source == null) {
			throw new JsonGenerationException("no source");
		}
		jg = factory.createGenerator(source);
	}

	@Override
	public void writeArray(JsonArray val) {
		write(val);
	}

	@Override
	public void writeObject(JsonObject val) {
		write(val);
	}

	@Override
	public void write(JsonStructure val) {
		if (jg == null) {
			throw new IllegalStateException(JsonGeneratorImpl.CANNOT_WRITE_ON_MORE_JSON_TEXT);
		}
		jg.write(val);
		Io.close(jg);
		jg = null;
	}

	@Override
	public void write(JsonValue value) {
		switch (value.getValueType()) {
		case OBJECT:
			writeObject((JsonObject) value);
			return;
		case ARRAY:
			writeArray((JsonArray) value);
			return;
		default:
			if (jg == null) {
				throw new IllegalStateException(JsonGeneratorImpl.CANNOT_WRITE_ON_MORE_JSON_TEXT);
			}
			jg.write(value);
			Io.close(jg);
			jg = null;
		}
	}

	@Override
	public void close() {
		Io.close(source);
		jg = null;
	}
}
