package org.jetbrains.space.sdk.datatype;

import java.time.LocalDate;

public class BusinessEntityRelation implements SpaceObject {

    public final String id;
    public final boolean archived;
    public final BusinessEntity entity;
    public final TD_MemberProfile member;
    public final LocalDate since;
    public final LocalDate till;

    private BusinessEntityRelation(String id, boolean archived, BusinessEntity entity, TD_MemberProfile member,
                                   LocalDate since, LocalDate till) {
        this.id = id;
        this.archived = archived;
        this.entity = entity;
        this.member = member;
        this.since = since;
        this.till = till;
    }
}
