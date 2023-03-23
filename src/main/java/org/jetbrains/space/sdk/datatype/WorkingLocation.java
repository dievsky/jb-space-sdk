package org.jetbrains.space.sdk.datatype;

public class WorkingLocation implements SpaceObject {
    public final int day;
    public final boolean remote;

    public WorkingLocation(int day, boolean remote) {
      this.day = day;
      this.remote = remote;
    }
}
