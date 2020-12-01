package org.jetbrains.space.sdk.datatype;

public class WorkingHours implements SpaceObject {

  public final boolean checked;
  public final int day;

  public WorkingHours(boolean checked, int day) {
    this.checked = checked;
    this.day = day;
  }

}
