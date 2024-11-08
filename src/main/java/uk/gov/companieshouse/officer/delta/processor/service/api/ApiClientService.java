
package uk.gov.companieshouse.officer.delta.processor.service.api;

import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.officer.delta.processor.model.DeleteAppointmentParameters;

public interface ApiClientService {

    void putAppointment(final String companyNumber, final FullRecordCompanyOfficerApi appointment);

    void deleteAppointment(DeleteAppointmentParameters deleteAppointmentParameters);
}
