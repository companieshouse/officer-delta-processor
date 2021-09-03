package uk.gov.companieshouse.officer.delta.processor.transformer;

import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;

public interface Transform {
    void transform(OfficersItem inputOfficer, AppointmentAPI outputAppointment);
}
