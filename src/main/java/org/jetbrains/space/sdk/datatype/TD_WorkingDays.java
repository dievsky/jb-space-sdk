package org.jetbrains.space.sdk.datatype;

import java.time.LocalDate;

public class TD_WorkingDays implements SpaceObject {

    public final String id;
    public final LocalDate dateStart;
    public final LocalDate dateEnd;
    public final WorkingDaysSpec workingDaysSpec;

    private TD_WorkingDays(String id, LocalDate dateStart, LocalDate dateEnd, WorkingDaysSpec workingDaysSpec) {
        this.id = id;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.workingDaysSpec = workingDaysSpec;
    }
}
