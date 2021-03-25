package org.jetbrains.space.sdk.datatype;

public class TD_Team implements SpaceObject {
  public final String id;
  public final String name;

  public TD_Team(String id, String name) {
    this.id = id;
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
