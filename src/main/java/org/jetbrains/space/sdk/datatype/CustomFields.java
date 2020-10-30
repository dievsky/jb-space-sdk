package org.jetbrains.space.sdk.datatype;

public class CustomFields implements SpaceObject {

    public final StringCFValue employeeNumber;

    public CustomFields(StringCFValue employeeNumber) {
        this.employeeNumber = employeeNumber;
    }
}
