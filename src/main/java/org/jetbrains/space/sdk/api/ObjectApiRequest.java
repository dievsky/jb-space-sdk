package org.jetbrains.space.sdk.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.space.sdk.fields.DatatypeStructure;
import org.jetbrains.space.sdk.fields.FieldSpecs;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class ObjectApiRequest<T> implements ApiRequest<T> {

  private final @NotNull SpaceService spaceService;
  private final @NotNull String endpoint;
  private final @NotNull String method;
  private final @NotNull Type type;
  private final @NotNull Map<String, Object> parameterMap;
  private final @NotNull FieldSpecs specs;

  ObjectApiRequest(@NotNull SpaceService spaceService, @NotNull String endpoint, @NotNull String method,
                   @NotNull Type type, @NotNull DatatypeStructure structure) {
    this.spaceService = spaceService;
    this.endpoint = endpoint;
    this.method = method;
    this.type = type;

    parameterMap = new HashMap<>();
    specs = new FieldSpecs(true, new HashMap<>(), structure);
  }

  @Override
  public @NotNull ApiRequest<T> addParameter(@NotNull String key, @NotNull String value) {
    if (key.startsWith("$")) {
      throw new IllegalArgumentException("special parameter " + key + " can't be set directly");
    }
    return doAddParameter(key, value);
  }

  @NotNull
  ObjectApiRequest<T> doAddParameter(@NotNull String key, @NotNull Object value) {
    parameterMap.put(key, value);
    return this;
  }

  @Override
  public @NotNull ApiRequest<T> addField(@NotNull String fieldName, String... fieldNames) {
    specs.addField(fieldName, fieldNames);
    return this;
  }

  @Override
  public @NotNull ApiRequest<T> addRecursiveField(@NotNull String fieldName, String... fieldNames) {
    specs.addRecursiveField(fieldName, fieldNames);
    return this;
  }

  @Override
  public @NotNull ApiRequest<T> addParameterList(@NotNull String key, @NotNull Collection<String> values) {
    throw new UnsupportedOperationException();
  }

  @Override
  public @NotNull T execute() throws IOException, InterruptedException {
    doAddParameter("$fields", specs.toString());
    return SpaceService.GSON.fromJson(spaceService.rawJSONQuery(endpoint, method, parameterMap), type);
  }
}
