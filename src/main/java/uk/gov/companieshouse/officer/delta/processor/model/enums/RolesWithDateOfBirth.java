package uk.gov.companieshouse.officer.delta.processor.model.enums;

import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;

import java.util.Arrays;

public enum RolesWithDateOfBirth {
    DIRECTOR(OfficerRole.DIR),
    LLP_MEMBER(OfficerRole.LLPMEM),
    LLP_DESIGNATED_MEMBER(OfficerRole.LLPDESMEM),
    MEMBER_OF_A_MANAGEMENT_ORGAN(OfficerRole.MEMMANORG),
    MEMBER_OF_A_SUPERVISORY_ORGAN(OfficerRole.MEMSUPORG),
    MEMBER_OF_AN_ADMINISTRATIVE_ORGAN(OfficerRole.MEMADMORG),
    NOMINEE_DIRECTOR(OfficerRole.NOMDIR);

    private final OfficerRole officerRole;

    RolesWithDateOfBirth(OfficerRole officerRole) {
        this.officerRole = officerRole;
    }

    public String getOfficerRole() {
        return officerRole.getValue();
    }

    public static boolean officerRequiresDateOfBirth(final OfficersItem officer) {
        return officerRequiresDateOfBirth(officer.getOfficerRole());
    }

    public static boolean officerRequiresDateOfBirth(final OfficerRole role) {
        return officerRequiresDateOfBirth(role.getValue());
    }

    public static boolean officerRequiresDateOfBirth(final String role) {
        return Arrays.stream(RolesWithDateOfBirth.values())
                .map(r -> r.officerRole.getValue())
                .anyMatch(rid -> rid.equals(role));
    }
}
