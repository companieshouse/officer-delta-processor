package uk.gov.companieshouse.officer.delta.processor.model.enums;

import java.util.EnumSet;

/**
 * The enum Roles with former names.
 */
public enum RolesWithFormerNames {

    /**
     * Director roles with former names.
     */
    DIRECTOR(OfficerRole.DIR),
    /**
     * Llp member roles with former names.
     */
    LLP_MEMBER(OfficerRole.LLPMEM),
    /**
     * Llp designated member roles with former names.
     */
    LLP_DESIGNATED_MEMBER(OfficerRole.LLPDESMEM),
    /**
     * Member of a management organ roles with former names.
     */
    MEMBER_OF_A_MANAGEMENT_ORGAN(OfficerRole.MEMMANORG),
    /**
     * Member of a supervisory organ roles with former names.
     */
    MEMBER_OF_A_SUPERVISORY_ORGAN(OfficerRole.MEMSUPORG),
    /**
     * Member of an administrative organ roles with former names.
     */
    MEMBER_OF_AN_ADMINISTRATIVE_ORGAN(OfficerRole.MEMADMORG),
    /**
     * Nominee director roles with former names.
     */
    NOMINEE_DIRECTOR(OfficerRole.NOMDIR),
    /**
     * Nom sec roles with former names.
     */
    NOM_SEC(OfficerRole.NOMSEC),
    /**
     * Sec roles with former names.
     */
    SEC(OfficerRole.SEC),
    /**
     * Manoff roles with former names.
     */
    MANOFF(OfficerRole.MANOFF),
    /**
     * LP General Partner role with former names.
     */
    LLPGENPART(OfficerRole.LLPGENPART),
    /**
     * LP Limited Partner role with former names.
     */
    LLPLIMPART(OfficerRole.LLPLIMPART),
    /**
     * LP General Partner role with former names.
     */
    LPGENPART(OfficerRole.LPGENPART),
    /**
     * LP Limited Partner role with former names.
     */
    LPLIMPART(OfficerRole.LPLIMPART);

    private final OfficerRole officerRole;

    RolesWithFormerNames(OfficerRole officerRole) {
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
        return EnumSet.allOf(RolesWithFormerNames.class).stream()
                .map(r -> r.officerRole.getValue())
                .anyMatch(role::equals);
    }
}
