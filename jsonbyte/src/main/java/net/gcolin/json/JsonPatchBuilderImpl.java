package net.gcolin.json;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonPatch;
import javax.json.JsonPatchBuilder;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;

public class JsonPatchBuilderImpl implements JsonPatchBuilder {

  private JsonArrayBuilder builder;
  private JsonProvider jsonProvider;
  
  public JsonPatchBuilderImpl(JsonProvider jsonProvider) {
    builder = jsonProvider.createArrayBuilder();
    this.jsonProvider = jsonProvider;
  }
  
  public JsonPatchBuilderImpl(JsonProvider jsonProvider, JsonArray array) {
    builder = jsonProvider.createArrayBuilder(array);
    this.jsonProvider = jsonProvider;
  }
  
  @Override
  public JsonPatchBuilder add(String path, JsonValue value) {
    builder.add(jsonProvider.createObjectBuilder().add("op", "add").add("path", path).add("value", value));
    return this;
  }

  @Override
  public JsonPatchBuilder add(String path, String value) {
    builder.add(jsonProvider.createObjectBuilder().add("op", "add").add("path", path).add("value", value));
    return this;
  }

  @Override
  public JsonPatchBuilder add(String path, int value) {
    builder.add(jsonProvider.createObjectBuilder().add("op", "add").add("path", path).add("value", value));
    return this;
  }

  @Override
  public JsonPatchBuilder add(String path, boolean value) {
    builder.add(jsonProvider.createObjectBuilder().add("op", "add").add("path", path).add("value", value));
    return this;
  }

  @Override
  public JsonPatchBuilder remove(String path) {
    builder.add(jsonProvider.createObjectBuilder().add("op", "remove").add("path", path));
    return this;
  }

  @Override
  public JsonPatchBuilder replace(String path, JsonValue value) {
    builder.add(jsonProvider.createObjectBuilder().add("op", "replace").add("path", path).add("value", value));
    return this;
  }

  @Override
  public JsonPatchBuilder replace(String path, String value) {
    builder.add(jsonProvider.createObjectBuilder().add("op", "replace").add("path", path).add("value", value));
    return this;
  }

  @Override
  public JsonPatchBuilder replace(String path, int value) {
    builder.add(jsonProvider.createObjectBuilder().add("op", "replace").add("path", path).add("value", value));
    return this;
  }

  @Override
  public JsonPatchBuilder replace(String path, boolean value) {
    builder.add(jsonProvider.createObjectBuilder().add("op", "replace").add("path", path).add("value", value));
    return this;
  }

  @Override
  public JsonPatchBuilder move(String path, String from) {
    builder.add(jsonProvider.createObjectBuilder().add("op", "move").add("from", path).add("path", from));
    return this;
  }

  @Override
  public JsonPatchBuilder copy(String path, String from) {
    builder.add(jsonProvider.createObjectBuilder().add("op", "copy").add("from", from).add("path", path));
    return this;
  }

  @Override
  public JsonPatchBuilder test(String path, JsonValue value) {
    builder.add(jsonProvider.createObjectBuilder().add("op", "test").add("path", path).add("value", value));
    return this;
  }

  @Override
  public JsonPatchBuilder test(String path, String value) {
    builder.add(jsonProvider.createObjectBuilder().add("op", "test").add("path", path).add("value", value));
    return this;
  }

  @Override
  public JsonPatchBuilder test(String path, int value) {
    builder.add(jsonProvider.createObjectBuilder().add("op", "test").add("path", path).add("value", value));
    return this;
  }

  @Override
  public JsonPatchBuilder test(String path, boolean value) {
    builder.add(jsonProvider.createObjectBuilder().add("op", "test").add("path", path).add("value", value));
    return this;
  }

  @Override
  public JsonPatch build() {
    return new JsonPatchImpl(builder.build(), jsonProvider);
  }

}
