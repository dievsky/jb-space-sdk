package org.jetbrains.space.sdk.datatype;

public class TD_Role implements SpaceObject {
  public final String name;

  public TD_Role(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
