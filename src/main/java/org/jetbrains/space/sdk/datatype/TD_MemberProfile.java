package org.jetbrains.space.sdk.datatype;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TD_MemberProfile implements SpaceObject {
  public final String id, username;
  public final TD_ProfileName name;
  public final TD_Location location;
  public final List<TD_MemberLocation> locations;
  public final LocalDate joined;
  public final LocalDateTime leftAt;
  public final LocalDate birthday;
  public final String gender;
  public final String profilePicture, avatar, smallAvatar, about;
  public final List<TD_MemberProfile> managers;
  public final List<TD_Membership> memberships;
  public final List<TD_ProfileEmail> emails;
  public final Map<String, CFValue> customFields;
  public final boolean notAMember;
  public final String externalId;

  public TD_MemberProfile(String id, TD_ProfileName name) {
    this.id = id;
    this.name = name;

    location = null;
    locations = null;
    joined = null;
    leftAt = null;
    profilePicture = avatar = smallAvatar = null;
    about = null;
    managers = null;
    emails = null;
    customFields = null;
    username = null;
    memberships = null;
    birthday = null;
    gender = null;
    notAMember = false;
    externalId = null;
  }

  public @Nullable TD_MemberLocation findLocationForDate(LocalDate date) {
    return locations == null ? null : locations.stream().filter(l -> l.containsDate(date)).findFirst().orElse(null);
  }

  public @Nullable LocalDate dateLeft() {
    return leftAt == null ? null : leftAt.toLocalDate();
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
      // get the "new" formal name
      final CFValue formalFirstName = customFields.get("First Name (Formal)");
      final CFValue formalLastName = customFields.get("Last Name (Formal)");
      if (formalFirstName != null || formalLastName != null) {
        StringBuilder builder = new StringBuilder();
        if (formalLastName != null && ((StringCFValue) formalLastName).getValue() != null && !((StringCFValue) formalLastName).getValue()
          .isBlank()) {
          builder.append(((StringCFValue) formalLastName).getValue());
        } else {
          builder.append(name.lastName);
        }
        builder.append(" ");
        if (formalFirstName != null && ((StringCFValue) formalFirstName).getValue() != null && !((StringCFValue) formalFirstName).getValue()
          .isBlank()) {
          builder.append(((StringCFValue) formalFirstName).getValue());
        } else {
          builder.append(name.firstName);
        }
        return builder.toString();
      }
      // get the "old" formal name
      final CFValue formalNameField = customFields.get("Formal name");
      if (formalNameField instanceof StringCFValue) {
        var formalName = ((StringCFValue) formalNameField).getValue();
        if (formalName != null && !formalName.isBlank()) return formalName;
      }
    }
    return null;
  }

  public @Nullable String getWearSize() {
    if (customFields != null) {
      final CFValue wearSizeField = customFields.get("Wear Size");
      if (wearSizeField instanceof EnumCFValue) {
        return ((EnumCFValue) wearSizeField).getValue();
      }
    }
    return null;
  }

  public @Nullable String getGender() {
    if (customFields != null) {
      final CFValue genderField = customFields.get("Gender");
      if (genderField instanceof EnumCFValue) {
        return ((EnumCFValue) genderField).getValue();
      }
    }
    return null;
  }

  public @Nullable String getCurrentLocation() {
    if (locations == null) return null;

    return locations.stream()
      .filter(loc -> loc.till == null)
      .map(loc ->
        loc.location.hierarchy()
          .map(l -> l.name)
          .collect(Collectors.joining(" / "))
      ).findFirst().orElse(null);
  }

  @Override
  public String toString() {
    return "TD_MemberProfile{id='" + id + '\'' + ", name=" + name + '}';
  }
}
