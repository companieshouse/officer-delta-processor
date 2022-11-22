package uk.gov.companieshouse.officer.delta.processor.service.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.logging.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Service that sends REST requests via private SDK.
 */
@Primary
@Service
public class ApiClientServiceImpl extends BaseApiClientServiceImpl implements ApiClientService {

    @Value("${chs.internal.api.key}")
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
    public InternalApiClient getApiClient(String contextId) {
        InternalApiClient internalApiClient = new InternalApiClient(getHttpClient(contextId));
        internalApiClient.setBasePath(apiUrl);
        internalApiClient.setBasePaymentsPath(paymentsApiUrl);
        internalApiClient.setInternalBasePath(internalApiUrl);

        return internalApiClient;
    }

    private HttpClient getHttpClient(String contextId) {
        ApiKeyHttpClient httpClient = new ApiKeyHttpClient(chsApiKey);
        httpClient.setRequestId(contextId);
        return httpClient;
    }

    @Override
    public ApiResponse<Void> putAppointment(final String logContext, String companyNumber, AppointmentAPI appointment) {
        final var uri =
                String.format("/company/%s/appointments/%s/full_record", companyNumber, appointment.getAppointmentId());

        Map<String,Object> logMap = createLogMap(companyNumber,"PUT", uri);
        logger.infoContext(logContext, String.format("PUT %s", uri), logMap);

        return executeOp(logContext, "putCompanyAppointment", uri,
                getApiClient(logContext).privateDeltaCompanyAppointmentResourceHandler()
                        .putAppointment()
                        .upsert(uri, appointment));
    }

    @Override
    public ApiResponse<Void> deleteAppointment(
            final String log, final String internalId,
            final String companyNumber) {
        final var uri =
                String.format("/company/%s/appointments/%s/full_record/delete",
                        companyNumber, internalId);

        Map<String,Object> logMap = createLogMap(internalId,"DELETE", uri);
        logger.infoContext(log, String.format("DELETE %s", uri), logMap);

        return executeOp(log, "deleteOfficer", uri,
                getApiClient(log).privateDisqualificationResourceHandler()
                        .deleteOfficer(uri));
    }

    private Map<String,Object> createLogMap(String companyNumber, String method, String path){
        final Map<String, Object> logMap = new HashMap<>();
        logMap.put("company_number", companyNumber);
        logMap.put("method",method);
        logMap.put("path", path);
        return logMap;
    }
}
