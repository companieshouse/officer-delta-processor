package uk.gov.companieshouse.officer.delta.processor.service.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.logging.Logger;

/**
 * Service that sends REST requests via private SDK.
 */
@Primary
@Service
public class ApiClientServiceImpl extends BaseApiClientServiceImpl implements ApiClientService {

    @Value("${chs.api.key}")
    private String chsApiKey;

    @Value("${api.url}")
    private String apiUrl;

    @Value("${payments.api.url}")
    private String paymentsApiUrl;

    @Value("${internal.api.url}")
    private String internalApiUrl;

    /**
     * Construct an {@link ApiClientServiceImpl}.
     *
     * @param logger the CH logger
     */
    @Autowired
    public ApiClientServiceImpl(final Logger logger) {
        super(logger);
    }

    @Override
    public InternalApiClient getApiClient() {
        InternalApiClient internalApiClient = new InternalApiClient(getHttpClient());
        internalApiClient.setBasePath(apiUrl);
        internalApiClient.setBasePaymentsPath(paymentsApiUrl);
        internalApiClient.setInternalBasePath(internalApiUrl);

        return internalApiClient;
    }

    private HttpClient getHttpClient() {
        ApiKeyHttpClient httpClient = new ApiKeyHttpClient(chsApiKey);
        // TODO: find out what the request id is supposed to be.
        httpClient.setRequestId("Test request id");
        return httpClient;
    }

    @Override
    public ApiResponse<Void> putOfficers(String companyNumber, String appointmentId, OfficerAPI appointment) {
        final String uri = String.format("%s/company/%s/appointments/%s", ROOT_URI, companyNumber, appointmentId);

        logger.debug(String.format("PUT %s", uri));
        logger.debug(String.format("appointment=%s", appointment));

// TODO: needs company-appointments.api.ch.gov.uk service

        return executeOp("putCompanyAppointment",
                uri,
                getApiClient().privateDeltaCompanyAppointmentResourceHandler().putOfficers().upsert(uri, appointment));
    }
}
