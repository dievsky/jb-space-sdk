package org.jetbrains.space.sdk.datatype;

import java.util.Map;

public class EnumCFValue extends CFValue {

  public EnumCFValue(Map<String, String> value) {
    super("EnumCFValue", value, null);
  }

  @Override
  public String getValue() {
    if (value == null) return null;

    if (value instanceof Map) return ((Map<String, String>) value).get("value");

    return null;
  }

}
