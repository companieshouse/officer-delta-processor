package uk.gov.companieshouse.officer.delta.processor.model.enums;

import java.util.EnumSet;

public enum RolesWithCountryOfResidence {

    DIR(OfficerRole.DIR),
    LLPMEM(OfficerRole.LLPMEM),
    LLPDESMEM(OfficerRole.LLPDESMEM),
    MEMMANORG(OfficerRole.MEMMANORG),
    MEMSUPORG(OfficerRole.MEMSUPORG),
    MEMADMORG(OfficerRole.MEMADMORG),
    NOMDIR(OfficerRole.NOMDIR);

    private final OfficerRole officerRole;

    RolesWithCountryOfResidence(OfficerRole officerRole) {
        this.officerRole = officerRole;
    }

    public String getOfficerRole() {
        return officerRole.getValue();
    }

    public static boolean includes(final OfficerRole role) {
        return includes(role.getValue());
    }

    public static boolean includes(final String role) {
        return EnumSet.allOf(RolesWithCountryOfResidence.class).stream()
            .map(r -> r.officerRole.getValue())
            .anyMatch(role::equals);
    }
}
