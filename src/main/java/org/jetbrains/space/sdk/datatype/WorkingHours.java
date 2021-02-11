package org.jetbrains.space.sdk.datatype;

import java.time.DayOfWeek;

public class WorkingHours implements SpaceObject {

  public final boolean checked;
  public final int day;
  public final TimeInterval interval;

  public WorkingHours(boolean checked, int day, TimeInterval interval) {
    this.checked = checked;
    this.day = day;
    this.interval = interval;
  }

  public DayOfWeek getDayOfWeek() {
    return day == 0 ? DayOfWeek.SUNDAY : DayOfWeek.values()[day - 1];
  }

  @Override
  public String toString() {
    return getDayOfWeek() + ":" + (checked ? interval.toString() : "-");
  }
}
