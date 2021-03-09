package org.jetbrains.space.sdk.datatype;

public class TD_Team implements SpaceObject {
  public final String name;

  public TD_Team(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
