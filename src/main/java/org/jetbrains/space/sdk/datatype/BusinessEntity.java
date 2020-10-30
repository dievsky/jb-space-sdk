package org.jetbrains.space.sdk.datatype;

public class BusinessEntity implements SpaceObject {

    public final String id;
    public final boolean archived;
    public final TD_Location location;
    public final String name;

    private BusinessEntity(String id, boolean archived, TD_Location location, String name) {
        this.id = id;
        this.archived = archived;
        this.location = location;
        this.name = name;
    }
}
