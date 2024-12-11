package uk.gov.companieshouse.officer.delta.processor.model.enums;

import java.util.EnumSet;

/**
 * The enum Roles with pre 1992 appointment.
 */
public enum RolesWithPre1992Appointment {

    /**
     * Dircorp roles with pre 1992 appointment.
     */
    DIRCORP(OfficerRole.DIRCORP),
    /**
     * Nomdircorp roles with pre 1992 appointment.
     */
    NOMDIRCORP(OfficerRole.NOMDIRCORP),
    /**
     * Nomseccorp roles with pre 1992 appointment.
     */
    NOMSECCORP(OfficerRole.NOMSECCORP),
    /**
     * Seccorp roles with pre 1992 appointment.
     */
    SECCORP(OfficerRole.SECCORP),
    /**
     * Dir roles with pre 1992 appointment.
     */
    DIR(OfficerRole.DIR),
    /**
     * Nomdir roles with pre 1992 appointment.
     */
    NOMDIR(OfficerRole.NOMDIR),
    /**
     * Nomsec roles with pre 1992 appointment.
     */
    NOMSEC(OfficerRole.NOMSEC),
    /**
     * Sec roles with pre 1992 appointment.
     */
    SEC(OfficerRole.SEC);

    private final OfficerRole officerRole;

    RolesWithPre1992Appointment(OfficerRole officerRole) {
        this.officerRole = officerRole;
    }

    /**
     * Gets officer role.
     *
     * @return the officer role
     */
    public String getOfficerRole() {
        return officerRole.getValue();
    }

    /**
     * Includes boolean.
     *
     * @param role the role
     * @return the boolean
     */
    public static boolean includes(final OfficerRole role) {
        return includes(role.getValue());
    }

    /**
     * Includes boolean.
     *
     * @param role the role
     * @return the boolean
     */
    public static boolean includes(final String role) {
        return EnumSet.allOf(RolesWithPre1992Appointment.class).stream()
            .map(r -> r.officerRole.getValue())
            .anyMatch(role::equals);
    }
}
