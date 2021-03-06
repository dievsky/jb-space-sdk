package org.jetbrains.space.sdk.datatype;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class TD_MemberProfile implements SpaceObject {
    public final String id, username;
    public final TD_ProfileName name;
    public final TD_Location location;
    public final List<TD_MemberLocation> locations;
    public final LocalDate joined, left;
    public final String profilePicture, avatar, smallAvatar, about;
    public final List<TD_MemberProfile> managers;
    public final List<TD_Membership> memberships;
    public final List<TD_ProfileEmail> emails;
    public final Map<String, CFValue> customFields;

    public TD_MemberProfile(String id, TD_ProfileName name) {
        this.id = id;
        this.name = name;

        location = null;
        locations = null;
        joined = null;
        left = null;
        profilePicture = avatar = smallAvatar = null;
        about = null;
        managers = null;
        emails = null;
        customFields = null;
        username = null;
        memberships = null;
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
