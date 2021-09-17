package uk.gov.companieshouse.officer.delta.processor.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Pre1992Role {

    DIRCORP("corporate-director"),
    NOMDIRCORP("corporate-nominee-director"),
    NOMSECCORP("corporate-nominee-secretary"),
    SECCORP("corporate-secretary"),
    DIR("director"),
    NOMDIR("nominee-director"),
    NOMSEC("nominee-secretary"),
    SEC("secretary");

    private final String value;

    Pre1992Role(String value) {
         this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
