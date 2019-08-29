package net.gcolin.json;

import java.util.List;
import java.util.Map;

import javax.json.JsonValue;
import javax.json.stream.JsonParser;

public class ReaderContext {

	JsonParser parser;
	List<JsonValue> values;
	Map<String, JsonValue> object;
	String key;
	
	public ReaderContext(JsonParser parser) {
		this.parser = parser;
	}
	
}
