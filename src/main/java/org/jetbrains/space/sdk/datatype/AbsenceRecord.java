package org.jetbrains.space.sdk.datatype;

import java.time.LocalDate;

public class AbsenceRecord implements SpaceObject {

    public final String id;
    public final boolean archived;
    public final TD_MemberProfile member;
    public final String description;
    public final LocalDate since;
    public final LocalDate till;
    public final TD_Location location;
    public final AbsenceReasonRecord reason;

    private AbsenceRecord(String id, boolean archived, TD_MemberProfile member,
                          String description, LocalDate since, LocalDate till,
                          TD_Location location, AbsenceReasonRecord reason) {
        this.id = id;
        this.archived = archived;
        this.member = member;
        this.description = description;
        this.since = since;
        this.till = till;
        this.location = location;
        this.reason = reason;
    }

}
