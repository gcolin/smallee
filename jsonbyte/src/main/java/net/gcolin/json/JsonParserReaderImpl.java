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

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;

/**
 * The {@code JsonParserReaderImpl} class reads a Json from a JsonParser.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JsonParserReaderImpl implements JsonReader {
	
	private JsonParser parser;

	public JsonParserReaderImpl(JsonParser parser) {
		this.parser = parser;
	}

	@Override
	public JsonStructure read() {
		JsonValue out = readValue();
		if (out instanceof JsonStructure) {
			return (JsonStructure) out;
		}
		throw new IllegalArgumentException("the json is not an array or an object");
	}

	@Override
	public JsonValue readValue() {
		parser.next();
		return parser.getValue();
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
		parser.close();
	}

}
