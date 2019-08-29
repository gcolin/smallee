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

import net.gcolin.common.io.Io;

import java.io.Reader;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;

/**
 * The {@code JsonReaderImpl} class reads a Json from a Reader.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JsonReaderImpl implements JsonReader {

	private Reader source;

	public JsonReaderImpl(Reader reader) {
		source = reader;
	}

	@Override
	public JsonStructure read() {
		return (JsonStructure) readValue();
	}

	@Override
	public JsonObject readObject() {
		return (JsonObject) read();
	}

	@Override
	public JsonArray readArray() {
		return (JsonArray) read();
	}

	@Override
	public void close() {
		Io.close(source);
	}

	@Override
	public JsonValue readValue() {
		if (source == null) {
			throw new IllegalStateException("already read");
		}
		JsonValue out = null;
		JsonReader internal = null;
		JsonParser parser = null;
		try {
			parser = JsonParserImpl.take(source);
			internal = new JsonParserReaderImpl(parser);
			out = internal.readValue();
		} finally {
			Io.close(internal);
			source = null;
			Io.close(parser);
			parser = null;
		}
		return out;
	}

}
