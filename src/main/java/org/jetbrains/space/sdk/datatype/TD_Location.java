package org.jetbrains.space.sdk.datatype;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class TD_Location implements SpaceObject {
  public final String id;
  public final String name;
  public final TD_Location parent;
  public final String type;

  public TD_Location() {
    id = null;
    name = null;
    parent = null;
    type = null;
  }

  public boolean isAncestorOrSelf(@NotNull String id) {
    return hierarchy().anyMatch(l -> id.equals(l.id));
  }

  public Stream<TD_Location> hierarchy() {
    Stream<TD_Location> me = Stream.of(this);
    return parent == null ? me : Stream.concat(me, parent.hierarchy());
  }
}
