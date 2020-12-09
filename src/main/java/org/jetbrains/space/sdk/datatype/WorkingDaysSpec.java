package org.jetbrains.space.sdk.datatype;

import java.util.List;

public class WorkingDaysSpec implements SpaceObject {

    public final List<Workday> days;
    public final List<WorkingHours> workingHours;

    private WorkingDaysSpec(List<Workday> days, List<WorkingHours> workingHours) {
        this.days = days;
        this.workingHours = workingHours;
    }

    @Override
    public String toString() {
        return days.toString();
    }
}
