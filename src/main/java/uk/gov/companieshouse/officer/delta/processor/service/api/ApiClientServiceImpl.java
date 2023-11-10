package uk.gov.companieshouse.officer.delta.processor.service.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;

import java.util.Map;
import uk.gov.companieshouse.officer.delta.processor.logging.DataMapHolder;

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
    public ApiClientServiceImpl(final Logger logger) {
        super(logger);
    }

    private InternalApiClient getApiClient(String contextId) {
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
    public ApiResponse<Void> putAppointment(final String logContext, String companyNumber, FullRecordCompanyOfficerApi appointment) {
        final var uri =
                String.format("/company/%s/appointments/%s/full_record", companyNumber, appointment.getExternalData().getAppointmentId());

        Map<String,Object> logMap = createLogMap(companyNumber,"PUT", uri);
        logger.infoContext(logContext, String.format("PUT %s", uri), logMap);

        return executeOp(logContext, "putCompanyAppointment", uri,
                getApiClient(logContext).privateDeltaCompanyAppointmentResourceHandler()
                        .putAppointment()
                        .upsert(uri, appointment));
    }

    @Override
    public ApiResponse<Void> deleteAppointment(final String logContext, final String internalId,
            final String companyNumber) {
        final var uri =
                String.format("/company/%s/appointments/%s/full_record/delete",
                        companyNumber, internalId);

        Map<String,Object> logMap = createLogMap(companyNumber,"DELETE", uri);
        logger.infoContext(logContext, String.format("DELETE %s", uri), logMap);

        return executeOp(logContext, "deleteOfficer", uri,
                getApiClient(logContext).privateDisqualificationResourceHandler()
                        .deleteOfficer(uri));
    }

    private Map<String,Object> createLogMap(String companyNumber, String method, String path){
        final Map<String, Object> logMap = DataMapHolder.getLogMap();
        logMap.put("company_number", companyNumber);
        logMap.put("method",method);
        logMap.put("path", path);
        return logMap;
    }
}
