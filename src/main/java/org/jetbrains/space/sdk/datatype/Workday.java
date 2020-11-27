package org.jetbrains.space.sdk.datatype;

public class Workday implements SpaceObject {

    public final String weekday;
    public final boolean working;
    public final int hours;
    public final int minutes;

    public Workday(String weekday, boolean working, int hours, int minutes) {
        this.weekday = weekday;
        this.working = working;
        this.hours = hours;
        this.minutes = minutes;
    }

    @Override
    public String toString() {
        return weekday + "(" + (working ? hours + "h" + (minutes == 0 ? "" : minutes + "m") : "_") + ")";
    }
}
