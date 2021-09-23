package uk.gov.companieshouse.officer.delta.processor.model.enums;

import java.util.EnumSet;

public enum RolesWithPre1992Appointment {

    DIRCORP(OfficerRole.DIRCORP),
    NOMDIRCORP(OfficerRole.NOMDIRCORP),
    NOMSECCORP(OfficerRole.NOMSECCORP),
    SECCORP(OfficerRole.SECCORP),
    DIR(OfficerRole.DIR),
    NOMDIR(OfficerRole.NOMDIR),
    NOMSEC(OfficerRole.NOMSEC),
    SEC(OfficerRole.SEC);

    private final OfficerRole officerRole;

    RolesWithPre1992Appointment(OfficerRole officerRole) {
        this.officerRole = officerRole;
    }

    public String getOfficerRole() {
        return officerRole.getValue();
    }

    public static boolean includes(final OfficerRole role) {
        return includes(role.getValue());
    }

    public static boolean includes(final String role) {
        return EnumSet.allOf(RolesWithPre1992Appointment.class).stream()
            .map(r -> r.officerRole.getValue())
            .anyMatch(role::equals);
    }
}
