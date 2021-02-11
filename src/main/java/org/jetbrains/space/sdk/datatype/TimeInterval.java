package org.jetbrains.space.sdk.datatype;

public class TimeInterval {
  public final TimeOfDay since;
  public final TimeOfDay till;

  public TimeInterval(TimeOfDay since, TimeOfDay till) {
    this.since = since;
    this.till = till;
  }

  @Override
  public String toString() {
    return since + "-" + till;
  }
}
