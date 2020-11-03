package org.jetbrains.space.sdk;

import com.google.gson.reflect.TypeToken;
import org.jetbrains.space.sdk.datatype.BatchResponse;
import org.jetbrains.space.sdk.datatype.DatatypeStructureDiscovery;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class BatchApiRequest<T> implements ApiRequest<List<T>> {

  private final static int CHUNK_SIZE = 20;

  protected String multiparameterKey = null;
  protected List<String> multiparameterValues = null;

  protected final ObjectApiRequest<BatchResponse<T>> request;

  BatchApiRequest(SpaceService spaceService, String api, String method, Type elementType) {
    Type batchType = TypeToken.getParameterized(BatchResponse.class, elementType).getType();
    request = new ObjectApiRequest<>(spaceService, api, method, batchType,
            BatchResponse.structure(DatatypeStructureDiscovery.structure(elementType)));
  }

  @Override
  public BatchApiRequest<T> addParameter(String key, String value) {
    request.addParameter(key, value);
    return this;
  }

  @Override
  public BatchApiRequest<T> addField(String fieldName, String... fieldNames) {
    request.addField("data", concatStrings(fieldName, fieldNames));
    return this;
  }

  @Override
  public BatchApiRequest<T> addRecursiveField(String fieldName, String... fieldNames) {
    request.addRecursiveField("data", concatStrings(fieldName, fieldNames));
    return this;
  }

  @Override
  public BatchApiRequest<T> addParameterList(String key, Collection<String> values) {
    if (multiparameterKey != null) {
      throw new IllegalStateException("only one multi-value parameter can be supplied");
    }
    multiparameterKey = key;
    multiparameterValues = new ArrayList<>(values);
    return this;
  }

  private List<T> doExecute() throws IOException, InterruptedException {
    BatchResponse<T> batchResponse = request.execute();
    var res = new ArrayList<>(batchResponse.data);
    String next = "!" + batchResponse.next;
    while (next != null && !next.equals(batchResponse.next) && res.size() != batchResponse.totalCount) {
      next = batchResponse.next;
      request.doAddParameter("$skip", next);
      batchResponse = request.execute();
      res.addAll(batchResponse.data);
    }
    return res;
  }

  @Override
  public List<T> execute() throws IOException, InterruptedException {
    if (multiparameterKey == null) {
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

  private String[] concatStrings(String v, String[] a) {
    String[] fields = new String[a.length + 1];
    fields[0] = v;
    System.arraycopy(a, 0, fields, 1, a.length);
    return fields;
  }
}
