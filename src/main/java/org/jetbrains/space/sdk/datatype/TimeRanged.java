package org.jetbrains.space.sdk.datatype;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

public interface TimeRanged {
  @Nullable LocalDate getStartDate();

  @Nullable LocalDate getEndDate();

  default boolean containsDate(@NotNull LocalDate date) {
    LocalDate since = getStartDate();
    LocalDate till = getEndDate();
    return (since == null || !date.isBefore(since)) && (till == null || !date.isAfter(till));
  }
}
