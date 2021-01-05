package org.jetbrains.space.sdk.api;

import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.space.sdk.datatype.BatchResponse;
import org.jetbrains.space.sdk.datatype.DatatypeStructureDiscovery;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class BatchApiRequest<T> implements ApiRequest<List<T>> {

  private final static int CHUNK_SIZE = 20;

  protected @Nullable String multiparameterKey = null;
  protected @Nullable List<String> multiparameterValues = null;

  protected final @NotNull ObjectApiRequest<BatchResponse<T>> request;

  BatchApiRequest(@NotNull SpaceService spaceService, @NotNull String api, @NotNull String method,
                  @NotNull Type elementType) {
    Type batchType = TypeToken.getParameterized(BatchResponse.class, elementType).getType();
    request = new ObjectApiRequest<>(spaceService, api, method, batchType,
            BatchResponse.structure(DatatypeStructureDiscovery.structure(elementType)));
  }

  @Override
  public @NotNull ApiRequest<List<T>> addParameter(@NotNull String key, @NotNull String value) {
    request.addParameter(key, value);
    return this;
  }

  @Override
  public @NotNull ApiRequest<List<T>> addField(@NotNull String fieldName, @NotNull String... fieldNames) {
    request.addField("data", concatStrings(fieldName, fieldNames));
    return this;
  }

  @Override
  public @NotNull ApiRequest<List<T>> addRecursiveField(@NotNull String fieldName, @NotNull String... fieldNames) {
    request.addRecursiveField("data", concatStrings(fieldName, fieldNames));
    return this;
  }

  @Override
  public @NotNull ApiRequest<List<T>> addParameterList(@NotNull String key, @NotNull Collection<String> values) {
    if (multiparameterKey != null) {
      throw new IllegalStateException("only one multi-value parameter can be supplied");
    }
    multiparameterKey = key;
    multiparameterValues = new ArrayList<>(values);
    return this;
  }

  private @NotNull List<T> doExecute() throws IOException, InterruptedException {
    BatchResponse<T> batchResponse = request.execute();
    var res = new ArrayList<>(batchResponse.data);
    String next = "!" + batchResponse.next;
    while (!next.equals(batchResponse.next) && res.size() != batchResponse.totalCount) {
      next = batchResponse.next;
      request.doAddParameter("$skip", next);
      batchResponse = request.execute();
      res.addAll(batchResponse.data);
    }
    request.doRemoveParameter("$skip");
    return res;
  }

  @Override
  public @NotNull List<T> execute() throws IOException, InterruptedException {
    if (multiparameterKey == null || multiparameterValues == null) {
      return doExecute();
    }

    var res = new ArrayList<T>();

    // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/chunked.html :((

    int chunkCount = (multiparameterValues.size() - 1) / CHUNK_SIZE + 1;

    for (int i = 0; i < chunkCount; i++) {
      List<String> chunk = multiparameterValues.subList(CHUNK_SIZE * i,
              Math.min(multiparameterValues.size(), CHUNK_SIZE * (i + 1)));
      request.doAddParameter(multiparameterKey, chunk);
      res.addAll(doExecute());
    }

    return res;
  }

  private @NotNull String[] concatStrings(String v, @NotNull String[] a) {
    String[] fields = new String[a.length + 1];
    fields[0] = v;
    System.arraycopy(a, 0, fields, 1, a.length);
    return fields;
  }
}
