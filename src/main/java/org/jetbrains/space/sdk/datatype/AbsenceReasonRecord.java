package org.jetbrains.space.sdk.datatype;

public class AbsenceReasonRecord implements SpaceObject {

    public final String id;
    public final String name;

    public AbsenceReasonRecord(String id, String name) {
        this.id = id;
        this.name = name;
    }

}
