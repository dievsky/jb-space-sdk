package org.jetbrains.space.sdk.datatype;

import java.util.List;

public class WorkingDaysSpec implements SpaceObject {

    public final List<WorkingHours> workingHours;

    public WorkingDaysSpec(List<WorkingHours> workingHours) {
        this.workingHours = workingHours;
    }

    @Override
    public String toString() {
        return workingHours.toString();
    }
}
