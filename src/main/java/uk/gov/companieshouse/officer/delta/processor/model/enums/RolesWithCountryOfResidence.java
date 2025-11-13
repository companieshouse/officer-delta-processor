package uk.gov.companieshouse.officer.delta.processor.model.enums;

import java.util.EnumSet;

/**
 * The enum Roles with country of residence.
 */
public enum RolesWithCountryOfResidence {

    /**
     * Dir roles with country of residence.
     */
    DIR(OfficerRole.DIR),
    /**
     * Llpmem roles with country of residence.
     */
    LLPMEM(OfficerRole.LLPMEM),
    /**
     * Llpdesmem roles with country of residence.
     */
    LLPDESMEM(OfficerRole.LLPDESMEM),
    /**
     * Memmanorg roles with country of residence.
     */
    MEMMANORG(OfficerRole.MEMMANORG),
    /**
     * Memsuporg roles with country of residence.
     */
    MEMSUPORG(OfficerRole.MEMSUPORG),
    /**
     * Memadmorg roles with country of residence.
     */
    MEMADMORG(OfficerRole.MEMADMORG),
    /**
     * Nomdir roles with country of residence.
     */
    NOMDIR(OfficerRole.NOMDIR),
    /**
     * Manoff roles with country of residence.
     */
    MANOFF(OfficerRole.MANOFF),
    /**
     * LP General Partner natural person role with country of residence.
     */
    LLPGENPART(OfficerRole.LLPGENPART),
    /**
     * LP Limited Partner natural person role with country of residence.
     */
    LLPLIMPART(OfficerRole.LLPLIMPART),
    /**
     * LP General Partner natural person role with country of residence.
     */
    LPGENPART(OfficerRole.LPGENPART),
    /**
     * LP Limited Partner natural person role with country of residence.
     */
    LPLIMPART(OfficerRole.LPLIMPART);


    private final OfficerRole officerRole;

    RolesWithCountryOfResidence(OfficerRole officerRole) {
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
        return EnumSet.allOf(RolesWithCountryOfResidence.class).stream()
            .map(r -> r.officerRole.getValue())
            .anyMatch(role::equals);
    }
}
