package org.jetbrains.space.sdk.datatype;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.Map;

public class TD_MemberProfile implements SpaceObject {

    public final String id;
    public final TD_ProfileName name;
    public final TD_Location location;
    public final LocalDate joined;
    public final LocalDate left;
    public final Map<String, CFValue> customFields;

    private TD_MemberProfile(String id,
                             TD_ProfileName name, TD_Location location,
                             LocalDate joined, LocalDate left,
                             Map<String, CFValue> customFields) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.joined = joined;
        this.left = left;
        this.customFields = customFields;
    }

    public @Nullable String getEmployeeNumber() {
        if (customFields != null) {
            final CFValue employeeNumberField = customFields.get("Employee Number");
            if (employeeNumberField instanceof StringCFValue) {
                return ((StringCFValue) employeeNumberField).getValue();
            }
        }
        return null;
    }

    public @NotNull String getFormalOrLastFirstName() {
        String formal = getFormalName();
        return formal != null ? formal : name.lastThenFirst();
    }

    public @Nullable String getFormalName() {
        if (customFields != null) {
            final CFValue formalNameField = customFields.get("Formal name");
            if (formalNameField instanceof StringCFValue) {
                return ((StringCFValue) formalNameField).getValue();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "TD_MemberProfile{id='" + id + '\'' + ", name=" + name + '}';
    }
}
