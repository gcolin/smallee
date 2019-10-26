package net.gcolin.json;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonPointer;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;

public class JsonPointerImpl implements JsonPointer {

	private JsonPointer delegate;
	private JsonPointer[] find;
	private Pattern decimal = Pattern.compile("\\d+");

	public JsonPointerImpl(String path, JsonProvider provider) {
		if (path == null) {
			throw new JsonException("path cannot be null or empty");
		}

		if (path.isEmpty()) {
			find = new JsonPointer[0];
			delegate = new Root();
			return;
		}

		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		
		String[] split = path.split("\\/");
		find = new JsonPointer[split.length - 1];
		for (int i = 0; i < split.length; i++) {
			String p = split[i];
			p = p.replaceAll("~1", "/");
			p = p.replaceAll("~0", "~");
			JsonPointer pointer;
			if (decimal.matcher(p).matches()) {
				pointer = new FindIndex(p, provider, Integer.parseInt(p));
			} else if ("-".equals(p)) {
				pointer = new FindIndex(p, provider, -1);
			} else {
				pointer = new FindKey(p, provider);
			}
			if (i == split.length - 1) {
				delegate = pointer;
			} else {
				find[i] = pointer;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends JsonStructure> T execute(T target, JsonValue value,
			BiFunction<JsonStructure, JsonValue, JsonStructure> operation) {
		JsonStructure[] subs = new JsonStructure[find.length];
		for (int i = 0; i < find.length; i++) {
			JsonValue val = find[i].getValue(i == 0 ? target : subs[i - 1]);
			if (val instanceof JsonStructure) {
				subs[i] = (JsonStructure) val;
			} else {
				throw new JsonException("cannot execute");
			}
		}

		JsonStructure t = subs.length == 0 ? target : subs[subs.length - 1];
		JsonStructure replaced = operation.apply(t, value);

		for (int i = find.length - 1; i >= 0; i--) {
			JsonStructure sub = i == 0 ? target : subs[i - 1];
			replaced = find[i].replace(sub, replaced);
		}
		return (T) replaced;
	}

	@Override
	public <T extends JsonStructure> T add(T target, JsonValue value) {
		return execute(target, value, (s, v) -> delegate.add(s, v));
	}

	@Override
	public <T extends JsonStructure> T remove(T target) {
		return execute(target, null, (s, v) -> delegate.remove(s));
	}

	@Override
	public <T extends JsonStructure> T replace(T target, JsonValue value) {
		return execute(target, value, (s, v) -> delegate.replace(s, v));
	}

	private JsonStructure find0(JsonStructure target) {
		JsonStructure sub = target;
		for (int i = 0; i < find.length; i++) {
			sub = (JsonStructure) find[i].getValue(sub);
		}
		return sub;
	}

	@Override
	public boolean containsValue(JsonStructure target) {
		return delegate.containsValue(find0(target));
	}

	@Override
	public JsonValue getValue(JsonStructure target) {
		JsonValue val = delegate.getValue(find0(target));
		if (val == null) {
			throw new JsonException("cannot find value");
		}
		return delegate.getValue(find0(target));
	}

	private static class Root implements JsonPointer {

		@Override
		public <T extends JsonStructure> T add(T target, JsonValue value) {
			throw new JsonException("root");
		}

		@Override
		public <T extends JsonStructure> T remove(T target) {
			throw new JsonException("root");
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends JsonStructure> T replace(T target, JsonValue value) {
			if(value instanceof JsonStructure) {
				return (T) value;
			} else {
				throw new JsonException("expected a JsonStructure");
			}			
		}

		@Override
		public boolean containsValue(JsonStructure target) {
			return target != null;
		}

		@Override
		public JsonValue getValue(JsonStructure target) {
			return target;
		}

	}

	private static class FindKey implements JsonPointer {

		private String key;
		protected JsonProvider provider;

		public FindKey(String key, JsonProvider provider) {
			this.key = key;
			this.provider = provider;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends JsonStructure> T add(T target, JsonValue value) {
			if (target instanceof JsonObject) {
				return (T) provider.createObjectBuilder((JsonObject) target).add(key, value).build();
			} else {
				throw new JsonException("cannot add");
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends JsonStructure> T remove(T target) {
			if (containsValue(target)) {
				return (T) provider.createObjectBuilder((JsonObject) target).remove(key).build();
			} else {
				throw new JsonException("cannot remove");
			}
		}

		@Override
		public <T extends JsonStructure> T replace(T target, JsonValue value) {
			if (containsValue(target)) {
				return add(target, value);
			} else {
				throw new JsonException("cannot replace");
			}
		}

		@Override
		public boolean containsValue(JsonStructure target) {
			return target instanceof JsonObject && ((JsonObject) target).get(key) != null;
		}

		@Override
		public JsonValue getValue(JsonStructure target) {
			return target instanceof JsonObject ? ((JsonObject) target).get(key) : null;
		}
	}

	private static class FindIndex extends FindKey {

		private int index;

		public FindIndex(String key, JsonProvider provider, int index) {
			super(key, provider);
			this.index = index;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends JsonStructure> T add(T target, JsonValue value) {
			if (target instanceof JsonArray && ((JsonArray) target).size() > index) {
				JsonArray array = (JsonArray) target;
				int idx = index < 0 ? array.size() + index + 1 : index;
				return (T) provider.createArrayBuilder(array).add(idx, value).build();
			}
			return super.add(target, value);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends JsonStructure> T remove(T target) {
			if (target instanceof JsonArray && ((JsonArray) target).size() > index) {
				JsonArray array = (JsonArray) target;
				int idx = index < 0 ? array.size() + index : index;
				return (T) provider.createArrayBuilder(array).remove(idx).build();
			}
			if (target instanceof JsonObject && ((JsonObject) target).size() > index) {
				JsonObject obj = (JsonObject) target;
				int idx = 0;
				JsonObjectBuilder objBuilder = provider.createObjectBuilder(obj);
				Set<String> set = obj.keySet();
				int idxStop = index < 0 ? set.size() + index : index;
				for (String key : set) {
					if (idx == idxStop) {
						objBuilder.remove(key);
						return (T) objBuilder.build();
					}
					idx++;
				}
			}
			return super.remove(target);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends JsonStructure> T replace(T target, JsonValue value) {
			if (target instanceof JsonArray && ((JsonArray) target).size() > index) {
				JsonArrayImpl array = (JsonArrayImpl) target;
				int idx = index < 0 ? array.size() + index : index;
				return (T) provider.createArrayBuilder(array).set(idx, value).build();
			}
			if (target instanceof JsonObject && ((JsonObject) target).size() > index) {
				JsonObjectImpl obj = (JsonObjectImpl) target;
				int idx = 0;
				JsonObjectBuilder objBuilder = provider.createObjectBuilder(obj);
				Set<String> set = obj.keySet();
				int idxStop = index < 0 ? set.size() + index : index;
				for (String key : set) {
					if (idx == idxStop) {
						objBuilder.add(key, value);
						return target;
					}
					idx++;
				}
			}
			return super.replace(target, value);
		}

		@Override
		public boolean containsValue(JsonStructure target) {
			if (target instanceof JsonArray && ((JsonArray) target).size() > index) {
				JsonArray array = (JsonArray) target;
				int idx = index < 0 ? array.size() + index : index;
				return array.get(idx) != null;
			}
			if (target instanceof JsonObject && ((JsonObject) target).size() > index) {
				JsonObject obj = (JsonObject) target;
				int idx = 0;
				Collection<JsonValue> set = obj.values();
				int idxStop = index < 0 ? set.size() + index : index;
				for (JsonValue val : set) {
					if (idx == idxStop) {
						return val != null;
					}
					idx++;
				}
			}
			return super.containsValue(target);
		}

		@Override
		public JsonValue getValue(JsonStructure target) {
			if (target instanceof JsonArray && ((JsonArray) target).size() > index) {
				JsonArrayImpl array = (JsonArrayImpl) target;
				int idx = index < 0 ? array.size() + index : index;
				return array.get(idx);
			}
			if (target instanceof JsonObject && ((JsonObject) target).size() > index) {
				JsonObject obj = (JsonObject) target;
				int idx = 0;
				Collection<JsonValue> set = obj.values();
				int idxStop = index < 0 ? set.size() + index : index;
				for (JsonValue val : set) {
					if (idx == idxStop) {
						return val;
					}
					idx++;
				}
			}
			return super.getValue(target);
		}

	}

}
