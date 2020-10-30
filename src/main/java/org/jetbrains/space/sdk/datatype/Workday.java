package org.jetbrains.space.sdk.datatype;

public class Workday implements SpaceObject {

    public final String weekday;
    public final boolean working;

    private Workday(String weekday, boolean working) {
        this.weekday = weekday;
        this.working = working;
    }
}
