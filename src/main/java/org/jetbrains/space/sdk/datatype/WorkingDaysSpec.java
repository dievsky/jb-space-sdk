package org.jetbrains.space.sdk.datatype;

import java.util.ArrayList;
import java.util.List;

public class WorkingDaysSpec implements SpaceObject {
    public final List<WorkingHours> workingHours;

    public final List<WorkingLocation> locations;

    public WorkingDaysSpec(List<WorkingHours> workingHours) {
      this(workingHours, new ArrayList<>());
    }

    public WorkingDaysSpec(List<WorkingHours> workingHours, List<WorkingLocation> locations) {
        this.workingHours = workingHours;
        this.locations = locations;
    }

    @Override
    public String toString() {
        return workingHours.toString();
    }
}
