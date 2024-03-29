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
import java.util.Map;

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

  @SuppressWarnings("unused")
  public @Nullable List<?> getValues() {
    return values;
  }

  /**
   * Try to cast this CFValue object to a more specific type based on the className field.
   *
   * @return a CFValue of a more specific type, or this.
   */
  public @NotNull CFValue cast() {
    if ("StringCFValue".equals(className)) {
      return new StringCFValue((String) value);
    }

    if ("EnumCFValue".equals(className)) {
      return new EnumCFValue((Map<String, String>) value);
    }

    return this;
  }

  public static @NotNull TypeAdapterFactory ADAPTER_FACTORY = new TypeAdapterFactory() {
    @SuppressWarnings("unchecked")
    @Override
    public <T> @Nullable TypeAdapter<T> create(@NotNull Gson gson, @NotNull TypeToken<T> type) {
      if (type.getType().equals(CFValue.class)) {
        final TypeAdapter<CFValue> delegateAdapter = (TypeAdapter<CFValue>) gson.getDelegateAdapter(this, type);
        return (TypeAdapter<T>) new TypeAdapter<CFValue>() {
          @Override
          public void write(JsonWriter out, CFValue value) throws IOException {
            delegateAdapter.write(out, value);
          }

          @Override
          public @Nullable CFValue read(JsonReader in) throws IOException {
            final CFValue raw = delegateAdapter.read(in);
            if (raw != null) {
              return raw.cast();
            } else {
              return null;
            }
          }
        };
      } else {
        return null;
      }

    }
  };
}
