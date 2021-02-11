package org.jetbrains.space.sdk.datatype;

public class TimeOfDay {
  public final int hours;
  public final int minutes;

  public TimeOfDay(int hours, int minutes) {
    this.hours = hours;
    this.minutes = minutes;
  }

  @Override
  public String toString() {
    return hours + ":" + minutes;
  }
}
