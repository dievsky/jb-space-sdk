package org.jetbrains.space.sdk.datatype;

import org.jetbrains.space.sdk.fields.DatatypeStructure;
import org.jetbrains.space.sdk.fields.LiteralObjectStructure;
import org.jetbrains.space.sdk.fields.ReferenceStructure;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatatypeStructureDiscovery {

  private DatatypeStructureDiscovery() {} // singleton

  private static final Map<Type, DatatypeStructure> visited = new HashMap<>();

  public static DatatypeStructure structure(Type type) {

    if (visited.containsKey(type)) {
      return visited.get(type);
    }

    Class<?> clazz;
    if (type instanceof Class<?>) {
      // a raw type, cast to Class
      clazz = (Class<?>) type;
    } else if (type instanceof ParameterizedType) {
      clazz = (Class<?>) ((ParameterizedType) type).getRawType();
      if (clazz.equals(List.class)) {
        // a generic list! let's investigate the type of the generic argument recursively
        return structure(((ParameterizedType) type).getActualTypeArguments()[0]);
      } else if (clazz.equals(Map.class)) {
        // a map! we can't go further
        visited.put(type, DatatypeStructure.PRIMITIVE);
        return DatatypeStructure.PRIMITIVE;
      } else {
        // we can only investigate lists
        throw new IllegalArgumentException("can't investigate the structure of " + type.toString());
      }
    } else {
      throw new IllegalArgumentException("can't investigate the structure of " + type.toString());
    }

    if (!SpaceObject.class.isAssignableFrom(clazz)) {
      // a primitive!
      visited.put(type, DatatypeStructure.PRIMITIVE);
      return DatatypeStructure.PRIMITIVE;
    }

    boolean isReference;
    try {
      clazz.getField("id");
      isReference = true;
    } catch (NoSuchFieldException ignore) {
      isReference = false;
    }

    final Map<String, DatatypeStructure> fieldMap = new HashMap<>();
    final DatatypeStructure res;
    if (isReference) {
      res = new ReferenceStructure(fieldMap);
    } else {
      res = new LiteralObjectStructure(fieldMap);
    }
    visited.put(clazz, res);

    Field[] fields = clazz.getFields();
    for (Field field : fields) {
      fieldMap.put(field.getName(), structure(field.getGenericType()));
    }
    return res;
  }
}
