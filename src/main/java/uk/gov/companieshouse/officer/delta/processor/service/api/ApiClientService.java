package uk.gov.companieshouse.officer.delta.processor.service.api;

import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.api.model.ApiResponse;

/**
 * The {@code ApiClientService} interface provides an abstraction that can be used when testing
 * {@code ApiClientManager} static methods, without imposing the use of a test framework that
 * supports mocking of static methods.
 */
public interface ApiClientService {

    /**
     * Apply a Company Appointment delta.
     *
     * @param logContext    the log context
     * @param companyNumber the company number
     * @param appointment   the appointment
     * @return the api response
     */
    ApiResponse<Void> putAppointment(final String logContext, final String companyNumber,
            final FullRecordCompanyOfficerApi appointment);

    /**
     * Delete a Company Appointment.
     *
     * @param logContext    the log context
     * @param internalId    the internal Id
     * @param companyNumber the company number
     * @param deltaAt       the delta at
     * @return the api response
     */
    ApiResponse<Void> deleteAppointment(final String logContext, final String internalId,
            final String companyNumber, String deltaAt);
}
