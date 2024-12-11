package uk.gov.companieshouse.officer.delta.processor.model.enums;

import java.util.EnumSet;

/**
 * The enum Roles with occupation.
 */
public enum RolesWithOccupation {

    /**
     * Director roles with occupation.
     */
    DIRECTOR(OfficerRole.DIR),
    /**
     * Member of a management organ roles with occupation.
     */
    MEMBER_OF_A_MANAGEMENT_ORGAN(OfficerRole.MEMMANORG),
    /**
     * Member of a supervisory organ roles with occupation.
     */
    MEMBER_OF_A_SUPERVISORY_ORGAN(OfficerRole.MEMSUPORG),
    /**
     * Member of an administrative organ roles with occupation.
     */
    MEMBER_OF_AN_ADMINISTRATIVE_ORGAN(OfficerRole.MEMADMORG),
    /**
     * Nominee director roles with occupation.
     */
    NOMINEE_DIRECTOR(OfficerRole.NOMDIR),
    /**
     * Nominee secretary roles with occupation.
     */
    NOMINEE_SECRETARY(OfficerRole.NOMSEC),
    /**
     * Secretary roles with occupation.
     */
    SECRETARY(OfficerRole.SEC),
    /**
     * Manoff roles with occupation.
     */
    MANOFF(OfficerRole.MANOFF);

    private final OfficerRole officerRole;

    RolesWithOccupation(OfficerRole officerRole) {
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
        return EnumSet.allOf(RolesWithOccupation.class).stream()
            .map(r -> r.officerRole.getValue())
            .anyMatch(role::equals);
    }
}
