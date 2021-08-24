package uk.gov.companieshouse.officer.delta.processor.tranformer;

import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;

public interface Transform {
    void transform(OfficersItem inputOfficer, OfficerAPI outputOfficer);
}
