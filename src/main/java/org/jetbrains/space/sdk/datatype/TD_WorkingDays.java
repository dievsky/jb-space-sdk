package org.jetbrains.space.sdk.datatype;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

public class TD_WorkingDays implements SpaceObject {

    public final String id;
    public final @Nullable LocalDate dateStart;
    public final @Nullable LocalDate dateEnd;
    public final WorkingDaysSpec workingDaysSpec;

    private TD_WorkingDays(String id, @Nullable LocalDate dateStart, @Nullable LocalDate dateEnd, WorkingDaysSpec workingDaysSpec) {
        this.id = id;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.workingDaysSpec = workingDaysSpec;
    }

    public boolean intersects(@NotNull LocalDate intervalStart, @NotNull LocalDate intervalEnd) {
        boolean intervalAfterMe = dateEnd != null && intervalStart.isAfter(dateEnd);
        boolean intervalBeforeMe = dateStart != null && intervalEnd.isBefore(dateStart);
        return !intervalAfterMe && !intervalBeforeMe;
    }

    public boolean contains(@NotNull LocalDate date) {
        return (dateEnd == null || !date.isAfter(dateEnd)) && (dateStart == null || !date.isBefore(dateStart));
    }

    @Override
    public String toString() {
        return "TD_WorkingDays{" +
                "start=" + dateStart +
                ", end=" + dateEnd +
                ", " + workingDaysSpec +
                '}';
    }
}
