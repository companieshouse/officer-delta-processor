package uk.gov.companieshouse.officer.delta.processor.model.enums;

import java.util.EnumSet;

public enum RolesWithResidentialAddress {

    DIRECTOR(OfficerRole.DIR),
    LLPMEM(OfficerRole.LLPMEM),
    LLPDESMEM(OfficerRole.LLPDESMEM),
    MEMBER_OF_A_MANAGEMENT_ORGAN(OfficerRole.MEMMANORG),
    MEMBER_OF_A_SUPERVISORY_ORGAN(OfficerRole.MEMSUPORG),
    MEMBER_OF_AN_ADMINISTRATIVE_ORGAN(OfficerRole.MEMADMORG),
    NOMINEE_DIRECTOR(OfficerRole.NOMDIR),
    PERSAUTHR(OfficerRole.PERSAUTHR),
    PERSAUTHRA(OfficerRole.PERSAUTHRA);

    private final OfficerRole officerRole;

    RolesWithResidentialAddress(OfficerRole officerRole) {
        this.officerRole = officerRole;
    }

    public String getOfficerRole() {
        return officerRole.getValue();
    }

    public static boolean includes(final OfficerRole role) {
        return includes(role.getValue());
    }

    public static boolean includes(final String role) {
        return EnumSet.allOf(RolesWithResidentialAddress.class).stream()
            .map(r -> r.officerRole.getValue())
            .anyMatch(role::equals);
    }
}
