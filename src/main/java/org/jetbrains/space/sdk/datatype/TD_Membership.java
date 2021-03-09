package org.jetbrains.space.sdk.datatype;

import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

public class TD_Membership implements SpaceObject, TimeRanged {
  public final LocalDate since, till;
  public final TD_Team team;
  public final TD_Role role;

  public TD_Membership(LocalDate since, LocalDate till, TD_Team team, TD_Role role) {
    this.since = since;
    this.till = till;
    this.team = team;
    this.role = role;
  }

  @Override
  public @Nullable LocalDate getStartDate() {
    return since;
  }

  @Override
  public @Nullable LocalDate getEndDate() {
    return till;
  }

  @Override
  public String toString() {
    return "TD_Membership{" +
            "since=" + since +
            ", till=" + till +
            ", team=" + team +
            ", role=" + role +
            '}';
  }
}
