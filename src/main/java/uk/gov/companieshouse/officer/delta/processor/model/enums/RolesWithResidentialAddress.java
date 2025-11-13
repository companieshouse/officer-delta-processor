package uk.gov.companieshouse.officer.delta.processor.model.enums;

import java.util.EnumSet;

/**
 * The enum Roles with residential address.
 */
public enum RolesWithResidentialAddress {

    /**
     * Director roles with residential address.
     */
    DIRECTOR(OfficerRole.DIR),
    /**
     * Llpmem roles with residential address.
     */
    LLPMEM(OfficerRole.LLPMEM),
    /**
     * Llpdesmem roles with residential address.
     */
    LLPDESMEM(OfficerRole.LLPDESMEM),
    /**
     * Member of a management organ roles with residential address.
     */
    MEMBER_OF_A_MANAGEMENT_ORGAN(OfficerRole.MEMMANORG),
    /**
     * Member of a supervisory organ roles with residential address.
     */
    MEMBER_OF_A_SUPERVISORY_ORGAN(OfficerRole.MEMSUPORG),
    /**
     * Member of an administrative organ roles with residential address.
     */
    MEMBER_OF_AN_ADMINISTRATIVE_ORGAN(OfficerRole.MEMADMORG),
    /**
     * Nominee director roles with residential address.
     */
    NOMINEE_DIRECTOR(OfficerRole.NOMDIR),
    /**
     * Persauthr roles with residential address.
     */
    PERSAUTHR(OfficerRole.PERSAUTHR),
    /**
     * Persauthra roles with residential address.
     */
    PERSAUTHRA(OfficerRole.PERSAUTHRA),
    /**
     * Manoff roles with residential address.
     */
    MANOFF(OfficerRole.MANOFF),
    /**
     * LP General Partner natural person role with residential address.
     */
    LLPGENPART(OfficerRole.LLPGENPART),
    /**
     * LP Limited Partner natural person role with residential address.
     */
    LLPLIMPART(OfficerRole.LLPLIMPART),

    LPGENPART(OfficerRole.LPGENPART),
    LPLIMPART(OfficerRole.LPLIMPART);



    private final OfficerRole officerRole;

    RolesWithResidentialAddress(OfficerRole officerRole) {
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
        return EnumSet.allOf(RolesWithResidentialAddress.class).stream()
            .map(r -> r.officerRole.getValue())
            .anyMatch(role::equals);
    }
}
