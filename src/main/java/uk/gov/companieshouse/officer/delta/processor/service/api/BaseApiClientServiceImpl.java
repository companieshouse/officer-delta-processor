package uk.gov.companieshouse.officer.delta.processor.service.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.Executor;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseApiClientServiceImpl {
    public static final String ROOT_URI = "/appointments/v2";

    protected Logger logger;

    protected BaseApiClientServiceImpl(final Logger logger) {
        this.logger = logger;
    }

    /**
     * General execution of an SDK endpoint.
     *
     * @param operationName name of operation
     * @param uri           uri of sdk being called
     * @param executor      executor to use
     * @param <T>           type of api response
     * @return the response object
     */
    public <T> ApiResponse<T> executeOp(final String operationName, final String uri,
                                        final Executor<ApiResponse<T>> executor) {
        final Map<String, Object> debugMap = new HashMap<>();

        debugMap.put("operationName", operationName);
        debugMap.put("requestUri", uri);
        try {
            logger.debugContext(uri, "SDK request", debugMap);

            return executor.execute();

        } catch (URIValidationException ex) {
            logger.errorContext(uri, "SDK exception", ex, debugMap);

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (ApiErrorResponseException ex) {
            logger.errorContext(uri, "SDK exception", ex, debugMap);

            throw new ResponseStatusException(HttpStatus.valueOf(ex.getStatusCode()),
                    ex.getStatusMessage(), ex);
        }
    }
}
