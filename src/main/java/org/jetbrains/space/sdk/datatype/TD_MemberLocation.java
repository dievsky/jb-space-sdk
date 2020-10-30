package org.jetbrains.space.sdk.datatype;

import java.time.LocalDate;

public class TD_MemberLocation implements SpaceObject {

    public final String id;
    public final Boolean archived;
    public final TD_Location location;
    public final TD_MemberProfile member;
    public final LocalDate since;
    public final LocalDate till;

    private TD_MemberLocation(String id, Boolean archived, TD_Location location,
                              TD_MemberProfile member, LocalDate since, LocalDate till) {
        this.id = id;
        this.archived = archived;
        this.location = location;
        this.member = member;
        this.since = since;
        this.till = till;
    }

}
