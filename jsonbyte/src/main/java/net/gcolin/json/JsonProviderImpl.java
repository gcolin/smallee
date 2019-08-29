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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonMergePatch;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonPatch;
import javax.json.JsonPatchBuilder;
import javax.json.JsonPointer;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

/**
 * The {@code JsonProviderImpl} class is the entry point of this Json implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JsonProviderImpl extends JsonProvider {

	private JsonFactoryImpl factory = new JsonFactoryImpl(null, this);

	@Override
	public JsonParser createParser(Reader paramReader) {
		return factory.createParser(paramReader);
	}

	@Override
	public JsonParser createParser(InputStream in) {
		return factory.createParser(in);
	}

	@Override
	public JsonParserFactory createParserFactory(Map<String, ?> paramMap) {
		return createFactory(paramMap);
	}

	@Override
	public JsonGenerator createGenerator(Writer paramWriter) {
		return factory.createGenerator(paramWriter);
	}

	@Override
	public JsonGenerator createGenerator(OutputStream paramOutputStream) {
		return factory.createGenerator(paramOutputStream);
	}

	@Override
	public JsonGeneratorFactory createGeneratorFactory(Map<String, ?> paramMap) {
		return createFactory(paramMap);
	}

	@Override
	public JsonReader createReader(Reader reader) {
		return factory.createReader(reader);
	}

	@Override
	public JsonReader createReader(InputStream in) {
		return factory.createReader(in);
	}

	@Override
	public JsonWriter createWriter(Writer writer) {
		return factory.createWriter(writer);
	}

	@Override
	public JsonWriter createWriter(OutputStream out) {
		return factory.createWriter(out);
	}

	@Override
	public JsonWriterFactory createWriterFactory(Map<String, ?> paramMap) {
		return createFactory(paramMap);
	}

	@Override
	public JsonReaderFactory createReaderFactory(Map<String, ?> paramMap) {
		return createFactory(paramMap);
	}

	@Override
	public JsonObjectBuilder createObjectBuilder() {
		return factory.createObjectBuilder();
	}

	@Override
	public JsonArrayBuilder createArrayBuilder() {
		return factory.createArrayBuilder();
	}

	@Override
	public JsonBuilderFactory createBuilderFactory(Map<String, ?> paramMap) {
		return createFactory(paramMap);
	}

	private JsonFactoryImpl createFactory(Map<String, ?> config) {
		Map<String, Object> cfg = null;

		if (config != null) {
			cfg = new HashMap<>();
			if (config.containsKey(JsonGenerator.PRETTY_PRINTING)) {
				cfg.put(JsonGenerator.PRETTY_PRINTING, config.get(JsonGenerator.PRETTY_PRINTING));
			}
		}
		if (config != null && config.containsKey(JsonGenerator.PRETTY_PRINTING)) {
			return new JsonPrettyFactoryImpl(cfg, this);
		} else {
			return new JsonFactoryImpl(cfg, this);
		}
	}

	@Override
	public JsonPointer createPointer(String jsonPointer) {
		return new JsonPointerImpl(jsonPointer, this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public JsonArrayBuilder createArrayBuilder(Collection<?> collection) {
		JsonArrayBuilder builder = new JsonArrayBuilderImpl();
		for (Object o : collection) {
			if (o instanceof String) {
				builder.add((String) o);
			} else if (o instanceof Long) {
				builder.add((Long) o);
			} else if (o instanceof Integer) {
				builder.add((Integer) o);
			} else if (o instanceof Double) {
				builder.add((Double) o);
			} else if (o instanceof Float) {
				builder.add((Float) o);
			} else if (o instanceof Boolean) {
				builder.add((Boolean) o);
			} else if (o instanceof BigDecimal) {
				builder.add((BigDecimal) o);
			} else if (o instanceof BigInteger) {
				builder.add((BigInteger) o);
			} else if (o instanceof Short) {
				builder.add((Short) o);
			} else if (o instanceof Byte) {
				builder.add((Byte) o);
			} else if (o instanceof Character) {
				builder.add((Character) o);
			} else if (o instanceof JsonValue) {
				builder.add((JsonValue) o);
			} else if (o instanceof JsonArrayBuilder) {
				builder.add((JsonArrayBuilder) o);
			} else if (o instanceof JsonObjectBuilder) {
				builder.add((JsonObjectBuilder) o);
			} else if (Map.class.isAssignableFrom(o.getClass())) {
				builder.add(createObjectBuilder((Map<String, Object>) o));
			} else if (Collection.class.isAssignableFrom(o.getClass())) {
				builder.add(createArrayBuilder((List<Object>) o));
			}
		}
		return builder;
	}

	@Override
	public JsonArrayBuilder createArrayBuilder(JsonArray array) {
		JsonArrayBuilderImpl builder = new JsonArrayBuilderImpl();
		for (JsonValue val : array) {
			builder.add(val);
		}
		return builder;
	}

	@Override
	public JsonNumber createValue(BigDecimal value) {
		return new BigDecimalJsonNumber(value);
	}

	@Override
	public JsonNumber createValue(BigInteger value) {
		return new BigIntegerJsonNumber(value);
	}

	@Override
	public JsonNumber createValue(double value) {
		return new BigDecimalJsonNumber(new BigDecimal(String.valueOf(value)));
	}

	@Override
	public JsonNumber createValue(int value) {
		return new IntegerJsonNumber(value);
	}

	@Override
	public JsonNumber createValue(long value) {
		return new LongJsonNumber(value);
	}

	@Override
	public JsonString createValue(String value) {
		return new JsonStringImpl(value);
	}

	@Override
	public JsonPatchBuilder createPatchBuilder() {
		return new JsonPatchBuilderImpl(this);
	}

	@Override
	public JsonPatchBuilder createPatchBuilder(JsonArray array) {
		return new JsonPatchBuilderImpl(this, array);
	}

	@Override
	public JsonPatch createDiff(JsonStructure source, JsonStructure target) {
		return createPatchBuilder(JsonPatchImpl.diff(source, target, this)).build();
	}

	@Override
	public JsonObjectBuilder createObjectBuilder(JsonObject object) {
		return new JsonObjectBuilderImpl(object);
	}

	@SuppressWarnings("unchecked")
	@Override
	public JsonObjectBuilder createObjectBuilder(Map<String, Object> map) {
		JsonObjectBuilder obj = new JsonObjectBuilderImpl();
		for (Entry<String, Object> entry : map.entrySet()) {
			if (entry.getValue() == null) {
				obj.addNull(entry.getKey());
			} else {
				Class<?> clazz = entry.getValue().getClass();
				if (clazz == String.class) {
					obj.add(entry.getKey(), (String) entry.getValue());
				} else if (clazz == BigDecimal.class) {
					obj.add(entry.getKey(), (BigDecimal) entry.getValue());
				} else if (clazz == BigInteger.class) {
					obj.add(entry.getKey(), (BigInteger) entry.getValue());
				} else if (clazz == Integer.class) {
					obj.add(entry.getKey(), (Integer) entry.getValue());
				} else if (clazz == Boolean.class) {
					obj.add(entry.getKey(), (Boolean) entry.getValue());
				} else if (clazz == Double.class) {
					obj.add(entry.getKey(), (Double) entry.getValue());
				} else if (clazz == Float.class) {
					obj.add(entry.getKey(), (Float) entry.getValue());
				} else if (clazz == Long.class) {
					obj.add(entry.getKey(), (Long) entry.getValue());
				} else if (clazz == Short.class) {
					obj.add(entry.getKey(), (Short) entry.getValue());
				} else if (clazz == Character.class) {
					obj.add(entry.getKey(), (Character) entry.getValue());
				} else if (clazz == JsonArrayBuilder.class) {
					obj.add(entry.getKey(), (JsonArrayBuilder) entry.getValue());
				} else if (clazz == JsonObjectBuilder.class) {
					obj.add(entry.getKey(), (JsonObjectBuilder) entry.getValue());
				} else if (clazz == JsonValue.class) {
					obj.add(entry.getKey(), (JsonValue) entry.getValue());
				} else if (Map.class.isAssignableFrom(clazz)) {
					obj.add(entry.getKey(), createObjectBuilder((Map<String, Object>) entry.getValue()));
				} else if (Collection.class.isAssignableFrom(clazz)) {
					obj.add(entry.getKey(), createArrayBuilder((List<Object>) entry.getValue()));
				}
			}
		}
		return obj;
	}

	@Override
	public JsonMergePatch createMergeDiff(JsonValue source, JsonValue target) {
		return new JsonMergePatchImpl(JsonMergePatchImpl.diff(source, target));
	}

	@Override
	public JsonPatch createPatch(JsonArray array) {
		return createPatchBuilder(array).build();
	}

	@Override
	public JsonMergePatch createMergePatch(JsonValue patch) {
		return new JsonMergePatchImpl(patch);
	}

}
