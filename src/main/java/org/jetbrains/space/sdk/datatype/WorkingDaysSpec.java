package org.jetbrains.space.sdk.datatype;

import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.util.List;

public class WorkingDaysSpec implements SpaceObject {

    public final List<Workday> days;

    private WorkingDaysSpec(List<Workday> days) {
        this.days = days;
    }

    public @NotNull Workday find(@NotNull DayOfWeek dayOfWeek) {
        return days.stream().filter(d -> d.weekday.equals(dayOfWeek.toString())).findFirst().orElseThrow();
    }

    @Override
    public String toString() {
        return days.toString();
    }
}
