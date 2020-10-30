package org.jetbrains.space.sdk.datatype;

public class TD_Location implements SpaceObject {

    public final String id;
    public final String name;
    public final TD_Location parent;

    private TD_Location(String id, String name, TD_Location parent) {
        this.id = id;
        this.name = name;
        this.parent = parent;
    }

}
