package uk.gov.companieshouse.officer.delta.processor.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OfficerRole {

    CIC("cic-manager"),
    DIR("director"),
    DIRCORP("corporate-director"),
    EEIGMAN("manager-of-an-eeig"),
    EEIGMANCORP("corporate-manager-of-an-eeig"),
    FACTOR("judicial-factor"),
    LLPDESMEM("llp-designated-member"),
    LLPDESMEMCORP("corporate-llp-designated-member"),
    LLPGENPART("general-partner-in-a-limited-partnership"),
    LLPLIMPART("limited-partner-in-a-limited-partnership"),
    LLPMEM("llp-member"),
    LLPMEMCORP("corporate-llp-member"),
    MEMADMORG("member-of-an-administrative-organ"),
    MEMADMORGCORP("corporate-member-of-an-administrative-organ"),
    MEMMANORG("member-of-a-management-organ"),
    MEMMANORGCORP("corporate-member-of-a-management-organ"),
    MEMSUPORG("member-of-a-supervisory-organ"),
    MEMSUPORGCORP("corporate-member-of-a-supervisory-organ"),
    NOMDIR("nominee-director"),
    NOMDIRCORP("corporate-nominee-director"),
    NOMSEC("nominee-secretary"),
    NOMSECCORP("corporate-nominee-secretary"),
    PERSAUTHA("person-authorised-to-accept"),
    PERSAUTHRA("person-authorised-to-represent-and-accept"),
    PERSAUTHR("person-authorised-to-represent"),
    RECMAN("receiver-and-manager"),
    SEC("secretary"),
    SECCORP("corporate-secretary"),
    MANOFFCORP("corporate-managing-officer");

    private final String value;

    OfficerRole(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
