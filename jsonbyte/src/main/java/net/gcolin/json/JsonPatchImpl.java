package net.gcolin.json;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonPatch;
import javax.json.JsonPatchBuilder;
import javax.json.JsonPointer;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.spi.JsonProvider;

public class JsonPatchImpl implements JsonPatch {

	private JsonArray array;
	private Function<JsonStructure, JsonStructure>[] actions;

	@SuppressWarnings("unchecked")
	public JsonPatchImpl(JsonArray array, JsonProvider provider) {
		this.array = array;
		List<Function<JsonStructure, JsonStructure>> actions = new ArrayList<>();
		for (int i = 0; i < array.size(); i++) {
			JsonObject op = array.getJsonObject(i);
			String path = op.getString("path");
			JsonPointer pointer = provider.createPointer(path);
			JsonValue val = op.get("value");
			switch (op.getString("op")) {
			case "test":
				actions.add(target -> {
					if (pointer.getValue(target).equals(val)) {
						return target;
					} else {
						throw new JsonException("test fail");
					}
				});
				break;
			case "replace":
				actions.add(target -> pointer.replace(target, val));
				break;
			case "remove":
				actions.add(target -> pointer.remove(target));
				break;
			case "move": {
				if (path.startsWith(op.getString("from")) && path.length() > op.getString("from").length()) {
					actions.add(target -> {
						throw new JsonException("cannot move");
					});
				} else {
					JsonPointer from = provider.createPointer(op.getString("from"));
					actions.add(target -> {
						JsonValue v = from.getValue(target);
						JsonStructure s = from.remove(target);
						return pointer.add(s, v);
					});
				}
				break;
			}
			case "copy":
				JsonPointer from = provider.createPointer(op.getString("from"));
				actions.add(target -> {
					JsonValue v = from.getValue(target);
					return pointer.add(target, v);
				});
				break;
			case "add":
				actions.add(target -> pointer.add(target, val));
				break;
			default:
				throw new JsonException("unsupported operation " + op.getString("op"));
			}

		}
		this.actions = new Function[actions.size()];
		actions.toArray(this.actions);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends JsonStructure> T apply(T target) {
		JsonStructure t = target;
		for (int i = 0; i < actions.length; i++) {
			t = actions[i].apply(t);
		}
		return (T) t;
	}

	@Override
	public JsonArray toJsonArray() {
		return array;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((array == null) ? 0 : array.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JsonPatchImpl other = (JsonPatchImpl) obj;
		if (array == null) {
			if (other.array != null)
				return false;
		} else if (!array.equals(other.array))
			return false;
		return true;
	}

	public static JsonArray diff(JsonStructure source, JsonStructure target, JsonProvider provider) {
		return new DiffGenerator().diff(source, target, provider);
	}

	static class DiffGenerator {
		private JsonPatchBuilder builder;

		JsonArray diff(JsonStructure source, JsonStructure target, JsonProvider provider) {
			builder = provider.createPatchBuilder();
			diff("", source, target);
			return builder.build().toJsonArray();
		}

		private void diff(String path, JsonValue source, JsonValue target) {
			if (source.equals(target)) {
				return;
			}
			ValueType s = source.getValueType();
			ValueType t = target.getValueType();
			if (s == ValueType.OBJECT && t == ValueType.OBJECT) {
				diffObject(path, (JsonObject) source, (JsonObject) target);
			} else if (s == ValueType.ARRAY && t == ValueType.ARRAY) {
				diffArray(path, (JsonArray) source, (JsonArray) target);
			} else {
				builder.replace(path, target);
			}
		}

		private void diffObject(String path, JsonObject source, JsonObject target) {
			source.forEach((key, value) -> {
				if (target.containsKey(key)) {
					diff(path + '/' + key, value, target.get(key));
				} else {
					builder.remove(path + '/' + key);
				}
			});
			target.forEach((key, value) -> {
				if (!source.containsKey(key)) {
					builder.add(path + '/' + key, value);
				}
			});
		}

		/*
		 * For array element diff, find the longest common subsequence, per
		 * http://en.wikipedia.org/wiki/Longest_common_subsequence_problem .
		 * We modify the algorithm to generate a replace if possible.
		 */
		private void diffArray(String path, JsonArray source, JsonArray target) {
			/* The array c keeps track of length of the subsequence. To avoid
			 * computing the equality of array elements again, we
			 * left shift its value by 1, and use the low order bit to mark
			 * that two items are equal.
			 */
			int m = source.size();
			int n = target.size();
			int[][] c = new int[m + 1][n + 1];
			for (int i = 0; i < m + 1; i++)
				c[i][0] = 0;
			for (int i = 0; i < n + 1; i++)
				c[0][i] = 0;
			for (int i = 0; i < m; i++) {
				for (int j = 0; j < n; j++) {
					if (source.get(i).equals(target.get(j))) {
						c[i + 1][j + 1] = ((c[i][j]) & ~1) + 3;
						// 3 = (1 << 1) | 1;
					} else {
						c[i + 1][j + 1] = Math.max(c[i + 1][j], c[i][j + 1]) & ~1;
					}
				}
			}

			int i = m;
			int j = n;
			while (i > 0 || j > 0) {
				if (i == 0) {
					j--;
					builder.add(path + '/' + j, target.get(j));
				} else if (j == 0) {
					i--;
					builder.remove(path + '/' + i);
				} else if ((c[i][j] & 1) == 1) {
					i--;
					j--;
				} else {
					int f = c[i][j - 1] >> 1;
					int g = c[i - 1][j] >> 1;
					if (f > g) {
						j--;
						builder.add(path + '/' + j, target.get(j));
					} else if (f < g) {
						i--;
						builder.remove(path + '/' + i);
					} else { // f == g) {
						i--;
						j--;
						diff(path + '/' + i, source.get(i), target.get(j));
					}
				}
			}
		}
	}

}
