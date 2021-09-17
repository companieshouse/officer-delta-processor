package uk.gov.companieshouse.officer.delta.processor.tranformer.rules;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.officer.delta.processor.model.enums.Pre1992Role;
import java.util.Arrays;

@Component
public class ReferenceData {

    public boolean isPre1992Role(String officerRole) {

        return Arrays.stream(Pre1992Role.values()).anyMatch(t -> t.getValue().equals(officerRole));
    }
}
