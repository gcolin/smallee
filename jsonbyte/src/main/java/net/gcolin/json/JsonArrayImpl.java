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

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 * The {@code JsonArrayImpl} class represents a JsonArray.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JsonArrayImpl extends AbstractList<JsonValue> implements JsonArray {

	private JsonValue[] data;

	public JsonArrayImpl(JsonValue[] data) {
		this.data = data;
	}

	public JsonValue[] getData() {
		return data;
	}
	
	public void setData(JsonValue[] data) {
		this.data = data;
	}

	@Override
	public ValueType getValueType() {
		return ValueType.ARRAY;
	}

	@Override
	public JsonObject getJsonObject(int paramInt) {
		return (JsonObject) data[paramInt];
	}

	@Override
	public JsonArray getJsonArray(int paramInt) {
		return (JsonArray) data[paramInt];
	}

	@Override
	public JsonNumber getJsonNumber(int paramInt) {
		return (JsonNumber) data[paramInt];
	}

	@Override
	public JsonString getJsonString(int paramInt) {
		return (JsonString) data[paramInt];
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends JsonValue> List<T> getValuesAs(Class<T> paramClass) {
		return (List<T>) Arrays.asList(data);
	}

	@Override
	public String getString(int paramInt) {
		return getJsonString(paramInt).getString();
	}

	@Override
	public String getString(int paramInt, String paramString) {
		if (paramInt >= size()) {
			return paramString;
		}
		JsonValue val = get(paramInt);
		return val instanceof JsonString ? ((JsonString) val).getString() : paramString;
	}

	@Override
	public int getInt(int index) {
		return getJsonNumber(index).intValue();
	}

	@Override
	public int getInt(int index, int defaultValue) {
		if (index >= size()) {
			return defaultValue;
		}
		JsonValue val = get(index);
		return val instanceof JsonNumber ? ((JsonNumber) val).intValue() : defaultValue;
	}

	@Override
	public boolean getBoolean(int paramInt) {
		JsonValue val = get(paramInt);
		if (val == TRUE) {
			return true;
		} else if (val == FALSE) {
			return false;
		}
		throw new ClassCastException("the element is not a boolean");
	}

	@Override
	public boolean getBoolean(int paramInt, boolean paramBoolean) {
		if (paramInt >= size()) {
			return paramBoolean;
		}
		JsonValue val = get(paramInt);
		if (val == TRUE) {
			return true;
		} else if (val == FALSE) {
			return false;
		}
		return paramBoolean;
	}

	@Override
	public boolean isNull(int paramInt) {
		return data[paramInt] == NULL;
	}

	@Override
	public JsonValue get(int index) {
		return data[index];
	}

	@Override
	public int size() {
		return data.length;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(data);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		JsonArrayImpl other = (JsonArrayImpl) obj;
		if (!Arrays.equals(data, other.data))
			return false;
		return true;
	}

}
