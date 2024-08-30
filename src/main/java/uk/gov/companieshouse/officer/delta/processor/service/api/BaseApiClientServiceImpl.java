package uk.gov.companieshouse.officer.delta.processor.service.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.Executor;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;

import java.util.Map;
import uk.gov.companieshouse.officer.delta.processor.logging.DataMapHolder;

public abstract class BaseApiClientServiceImpl {

    private static final String SDK_EXCEPTION = "SDK exception";
    
    protected Logger logger;

    protected BaseApiClientServiceImpl(final Logger logger) {
        this.logger = logger;
    }

    /**
     * General execution of an SDK endpoint.
     *
     * @param <T>           type of api response
     * @param logContext    context ID for logging
     * @param operationName name of operation
     * @param uri           uri of sdk being called
     * @param executor      executor to use
     * @return the response object
     */
    public <T> ApiResponse<T> executeOp(final String logContext, final String operationName, final String uri,
            final Executor<ApiResponse<T>> executor) {

        try {

            return executor.execute();

        }
        catch (URIValidationException ex) {
            logger.infoContext(logContext, SDK_EXCEPTION, buildLogMap(operationName, uri));

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (ApiErrorResponseException ex) {
            Map<String, Object> logMap = buildLogMap(operationName, uri);
            logMap.put("status", ex.getStatusCode());
            if (ex.getStatusCode() == HttpStatus.BAD_REQUEST.value() || ex.getStatusCode() == HttpStatus.CONFLICT.value()) {
                logger.errorContext(logContext, SDK_EXCEPTION, ex, logMap);
            }
            logger.infoContext(logContext, SDK_EXCEPTION, logMap);

            throw new ResponseStatusException(HttpStatus.valueOf(ex.getStatusCode()),
                    ex.getStatusMessage(), ex);
        }
    }

    private static Map<String, Object> buildLogMap(String operationName, String uri) {
        final Map<String, Object> logMap = DataMapHolder.getLogMap();
        logMap.put("operation_name", operationName);
        logMap.put("path", uri);
        logMap.put("request_id", DataMapHolder.getRequestId());
        return logMap;
    }
}
