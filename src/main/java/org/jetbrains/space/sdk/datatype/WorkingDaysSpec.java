package org.jetbrains.space.sdk.datatype;

import java.util.List;

public class WorkingDaysSpec implements SpaceObject {

    public final List<Workday> days;

    private WorkingDaysSpec(List<Workday> days) {
        this.days = days;
    }
}
