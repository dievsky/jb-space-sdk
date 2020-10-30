package org.jetbrains.space.sdk.datatype;

public class TD_ProfileWorkingDays implements SpaceObject {

    public final TD_MemberProfile profile;
    public final TD_WorkingDays workingDays;

    private TD_ProfileWorkingDays(TD_MemberProfile profile, TD_WorkingDays workingDays) {
        this.profile = profile;
        this.workingDays = workingDays;
    }
}
