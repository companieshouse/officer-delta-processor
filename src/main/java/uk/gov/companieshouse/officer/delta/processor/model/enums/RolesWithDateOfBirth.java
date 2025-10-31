package uk.gov.companieshouse.officer.delta.processor.model.enums;

import java.util.EnumSet;

/**
 * The enum Roles with date of birth.
 */
public enum RolesWithDateOfBirth {
    /**
     * Director roles with date of birth.
     */
    DIRECTOR(OfficerRole.DIR),
    /**
     * Llp member roles with date of birth.
     */
    LLP_MEMBER(OfficerRole.LLPMEM),
    /**
     * Llp designated member roles with date of birth.
     */
    LLP_DESIGNATED_MEMBER(OfficerRole.LLPDESMEM),
    /**
     * Member of a management organ roles with date of birth.
     */
    MEMBER_OF_A_MANAGEMENT_ORGAN(OfficerRole.MEMMANORG),
    /**
     * Member of a supervisory organ roles with date of birth.
     */
    MEMBER_OF_A_SUPERVISORY_ORGAN(OfficerRole.MEMSUPORG),
    /**
     * Member of an administrative organ roles with date of birth.
     */
    MEMBER_OF_AN_ADMINISTRATIVE_ORGAN(OfficerRole.MEMADMORG),
    /**
     * Nominee director roles with date of birth.
     */
    NOMINEE_DIRECTOR(OfficerRole.NOMDIR),
    /**
     * Manoff roles with date of birth.
     */
    MANOFF(OfficerRole.MANOFF),
    /**
     * LP General Partner role with date of birth.
     */
    LLPGENPART(OfficerRole.LLPGENPART),
    /**
     * LP Limited Partner role with date of birth.
     */
    LLPLIMPART(OfficerRole.LLPLIMPART);

    private final OfficerRole officerRole;

    RolesWithDateOfBirth(OfficerRole officerRole) {
        this.officerRole = officerRole;
    }

    /**
     * Gets officer role.
     *
     * @return the officer role
     */
    public OfficerRole getOfficerRole() {
        return officerRole;
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
        return EnumSet.allOf(RolesWithDateOfBirth.class).stream()
                .map(r -> r.officerRole.getValue())
                .anyMatch(role::equals);
    }
}
