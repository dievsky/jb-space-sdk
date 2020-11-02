package org.jetbrains.space.sdk.datatype;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public class CFValue implements SpaceObject {

  final @NotNull String className;
  final @Nullable Object value;
  final @Nullable List<Object> values;

  protected CFValue(@NotNull String className, @Nullable Object value, @Nullable List<Object> values) {
    this.className = className;
    this.value = value;
    this.values = values;
  }

  public @Nullable Object getValue() {
    return value;
  }

  public @Nullable List<?> getValues() {
    return values;
  }

  /**
   * Try to cast this CFValue object to a more specific type based on the className field.
   * @return a CFValue of a more specific type, or this.
   */
  public CFValue cast() {
    if ("StringCFValue".equals(className)) {
      return new StringCFValue((String) value);
    } else {
      return this;
    }
  }

  public static TypeAdapterFactory ADAPTER_FACTORY = new TypeAdapterFactory() {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      if (type.getType().equals(CFValue.class)) {
        final TypeAdapter<CFValue> delegateAdapter = (TypeAdapter<CFValue>) gson.getDelegateAdapter(this, type);
        return (TypeAdapter<T>) new TypeAdapter<CFValue>() {
          @Override
          public void write(JsonWriter out, CFValue value) throws IOException {
            delegateAdapter.write(out, value);
          }

          @Override
          public CFValue read(JsonReader in) throws IOException {
            return delegateAdapter.read(in).cast();
          }
        };
      } else {
        return null;
      }

    }
  };
}
