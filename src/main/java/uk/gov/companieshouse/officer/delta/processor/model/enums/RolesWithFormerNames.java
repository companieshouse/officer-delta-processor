package uk.gov.companieshouse.officer.delta.processor.model.enums;

import java.util.EnumSet;

public enum RolesWithFormerNames {

    DIRECTOR(OfficerRole.DIR),
    LLP_MEMBER(OfficerRole.LLPMEM),
    LLP_DESIGNATED_MEMBER(OfficerRole.LLPDESMEM),
    MEMBER_OF_A_MANAGEMENT_ORGAN(OfficerRole.MEMMANORG),
    MEMBER_OF_A_SUPERVISORY_ORGAN(OfficerRole.MEMSUPORG),
    MEMBER_OF_AN_ADMINISTRATIVE_ORGAN(OfficerRole.MEMADMORG),
    NOMINEE_DIRECTOR(OfficerRole.NOMDIR),
    NOM_SEC(OfficerRole.NOMSEC),
    SEC(OfficerRole.SEC),
    MANOFF(OfficerRole.MANOFF);

    private final OfficerRole officerRole;

    RolesWithFormerNames(OfficerRole officerRole) {
        this.officerRole = officerRole;
    }

    public String getOfficerRole() {
        return officerRole.getValue();
    }

    public static boolean includes(final OfficerRole role) {
        return includes(role.getValue());
    }

    public static boolean includes(final String role) {
        return EnumSet.allOf(RolesWithFormerNames.class).stream()
                .map(r -> r.officerRole.getValue())
                .anyMatch(role::equals);
    }
}
