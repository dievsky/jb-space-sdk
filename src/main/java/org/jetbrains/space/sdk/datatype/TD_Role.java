package org.jetbrains.space.sdk.datatype;

public class TD_Role implements SpaceObject {
  public final String id;
  public final String name;

  public TD_Role(String id, String name) {
    this.id = id;
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
