package org.jetbrains.space.sdk.datatype;

import java.util.stream.Stream;

public class TD_Team implements SpaceObject {
  public final String id;
  public final String name;
  public final TD_Team parent;

  public TD_Team(String id, String name, TD_Team parent) {
    this.id = id;
    this.name = name;
    this.parent = parent;
  }

  @Override
  public String toString() {
    return name;
  }

  public Stream<TD_Team> hierarchy() {
    Stream<TD_Team> me = Stream.of(this);
    return parent == null ? me : Stream.concat(me, parent.hierarchy());
  }
}
