package org.jetbrains.space.sdk.datatype;

import java.time.LocalDate;

public class PublicHoliday implements SpaceObject {

    public final String id;
    public final String name;
    public final LocalDate date;
    public final boolean workingDay;

    public PublicHoliday(String id, String name, LocalDate date, boolean workingDay) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.workingDay = workingDay;
    }

}
