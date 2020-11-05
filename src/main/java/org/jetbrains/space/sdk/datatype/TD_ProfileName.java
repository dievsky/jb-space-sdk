package org.jetbrains.space.sdk.datatype;

public class TD_ProfileName implements SpaceObject {

    public final String firstName;
    public final String lastName;

    private TD_ProfileName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}
