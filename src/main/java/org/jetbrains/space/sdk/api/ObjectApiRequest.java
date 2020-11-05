package org.jetbrains.space.sdk.api;

import org.jetbrains.space.sdk.fields.DatatypeStructure;
import org.jetbrains.space.sdk.fields.FieldSpecs;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class ObjectApiRequest<T> implements ApiRequest<T> {

  private final SpaceService spaceService;
  private final String endpoint;
  private final String method;
  private final Type type;
  private final Map<String, Object> parameterMap = new HashMap<>();
  private final FieldSpecs specs;

  ObjectApiRequest(SpaceService spaceService, String endpoint, String method, Type type, DatatypeStructure structure) {
    this.spaceService = spaceService;
    this.endpoint = endpoint;
    this.method = method;
    this.type = type;
    specs = new FieldSpecs(true, new HashMap<>(), structure);
  }

  @Override
  public ObjectApiRequest<T> addParameter(String key, String value) {
    if (key.startsWith("$")) {
      throw new IllegalArgumentException("special parameter " + key + " can't be set directly");
    }
    return doAddParameter(key, value);
  }

  ObjectApiRequest<T> doAddParameter(String key, Object value) {
    parameterMap.put(key, value);
    return this;
  }

  @Override
  public ObjectApiRequest<T> addField(String fieldName, String... fieldNames) {
    specs.addField(fieldName, fieldNames);
    return this;
  }

  @Override
  public ObjectApiRequest<T> addRecursiveField(String fieldName, String... fieldNames) {
    specs.addRecursiveField(fieldName, fieldNames);
    return this;
  }

  @Override
  public ApiRequest<T> addParameterList(String key, Collection<String> values) {
    throw new UnsupportedOperationException();
  }

  @Override
  public T execute() throws IOException, InterruptedException {
    doAddParameter("$fields", specs.toString());
    return SpaceService.GSON.fromJson(spaceService.rawJSONQuery(endpoint, method, parameterMap), type);
  }
}
