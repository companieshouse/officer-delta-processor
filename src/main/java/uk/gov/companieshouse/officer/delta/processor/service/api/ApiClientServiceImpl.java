package uk.gov.companieshouse.officer.delta.processor.service.api;

import static uk.gov.companieshouse.officer.delta.processor.OfficerDeltaProcessorApplication.NAMESPACE;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.officer.delta.processor.apiclient.InternalApiClientFactory;
import uk.gov.companieshouse.officer.delta.processor.apiclient.ResponseHandler;
import uk.gov.companieshouse.officer.delta.processor.logging.DataMapHolder;
import uk.gov.companieshouse.officer.delta.processor.model.DeleteAppointmentParameters;

/**
 * The type Api client service.
 */
@Component
public class ApiClientServiceImpl implements ApiClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private static final String URI = "/company/%s/appointments/%s/full_record";

    private final InternalApiClientFactory internalApiClientFactory;
    private final ResponseHandler responseHandler;

    /**
     * Instantiates a new Api client service.
     *
     * @param internalApiClientFactory the internal api client factory
     * @param responseHandler          the response handler
     */
    public ApiClientServiceImpl(InternalApiClientFactory internalApiClientFactory,
            ResponseHandler responseHandler) {
        this.internalApiClientFactory = internalApiClientFactory;
        this.responseHandler = responseHandler;
    }

    /**
     * Put appointment.
     *
     * @param companyNumber the company number
     * @param appointment   the FullRecordCompanyOfficerApi
     */
    public void putAppointment(String companyNumber, FullRecordCompanyOfficerApi appointment) {
        final String uri = String.format(URI, companyNumber,
                appointment.getExternalData().getAppointmentId());

        LOGGER.info(String.format("PUT %s", uri), DataMapHolder.getLogMap());

        InternalApiClient client = internalApiClientFactory.get();
        try {
            client.privateDeltaCompanyAppointmentResourceHandler().putAppointment()
                    .upsert(uri, appointment).execute();
        } catch (ApiErrorResponseException ex) {
            responseHandler.handle(ex);
        } catch (URIValidationException ex) {
            responseHandler.handle(ex);
        }
    }

    /**
     * Delete appointment.
     *
     * @param deleteAppointmentParameters the DeleteAppointmentParameters
     * @throws IllegalArgumentException an IllegalArgumentException
     */
    public void deleteAppointment(DeleteAppointmentParameters deleteAppointmentParameters) {
        final String deltaAt = deleteAppointmentParameters.getDeltaAt();
        final String companyNumber = deleteAppointmentParameters.getCompanyNumber();
        final String encodedInternalId = deleteAppointmentParameters.getEncodedInternalId();
        final String encodedOfficerId = deleteAppointmentParameters.getEncodedOfficerId();

        if (StringUtils.isBlank(deltaAt)) {
            LOGGER.error("Missing delta_at in request", DataMapHolder.getLogMap());
            throw new IllegalArgumentException("delta_at null or empty");
        }

        final String uri = String.format(URI, companyNumber, encodedInternalId);

        LOGGER.info(String.format("DELETE %s", uri), DataMapHolder.getLogMap());

        InternalApiClient client = internalApiClientFactory.get();
        try {
            client.privateDeltaResourceHandler().deleteOfficer(uri, deltaAt, encodedOfficerId)
                    .execute();
        } catch (ApiErrorResponseException ex) {
            responseHandler.handle(ex);
        } catch (URIValidationException ex) {
            responseHandler.handle(ex);
        }
    }
}
