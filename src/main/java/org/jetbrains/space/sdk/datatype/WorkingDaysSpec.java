package org.jetbrains.space.sdk.datatype;

import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public class WorkingDaysSpec implements SpaceObject {

    public final List<Workday> days;
    public final List<WorkingHours> workingHours;

    private WorkingDaysSpec(List<Workday> days, List<WorkingHours> workingHours) {
        this.days = days;
        this.workingHours = workingHours;
    }

    public @NotNull Optional<Workday> find(@NotNull DayOfWeek dayOfWeek) {
        return days.stream().filter(d -> d.weekday.equals(dayOfWeek.toString())).findFirst();
    }

    @Override
    public String toString() {
        return days.toString();
    }
}
