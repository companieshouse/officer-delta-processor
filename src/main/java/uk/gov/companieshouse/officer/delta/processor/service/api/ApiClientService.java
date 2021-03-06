
package uk.gov.companieshouse.officer.delta.processor.service.api;

import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;

/**
 * The {@code ApiClientService} interface provides an abstraction that can be
 * used when testing {@code ApiClientManager} static methods, without imposing
 * the use of a test framework that supports mocking of static methods.
 */
public interface ApiClientService {

    InternalApiClient getApiClient(String contextId);

    /**
     * Apply a Company Appointment delta.
     *
     *
     * @param logContext
     * @param companyNumber the company number
     * @param appointment
     * @return the api response
     */
    ApiResponse<Void> putAppointment(final String logContext, final String companyNumber, final AppointmentAPI appointment);

}
