package uk.gov.companieshouse.officer.delta.processor.apiclient;

import static uk.gov.companieshouse.officer.delta.processor.OfficerDeltaProcessorApplication.NAMESPACE;

import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.exception.RetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.logging.DataMapHolder;

@Component
public class ResponseHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private static final String API_INFO_RESPONSE_MESSAGE = "Call to Company Appointments API failed, status code: %d. %s";
    private static final String API_ERROR_RESPONSE_MESSAGE = "Call to Company Appointments API failed, status code: %d";
    private static final String URI_VALIDATION_EXCEPTION_MESSAGE = "Invalid URI";

    public void handle(ApiErrorResponseException ex) {
        final int statusCode = ex.getStatusCode();
        HttpStatus httpStatus = HttpStatus.valueOf(statusCode);

        if (HttpStatus.CONFLICT.equals(httpStatus) || HttpStatus.BAD_REQUEST.equals(httpStatus)) {
            final String msg = API_ERROR_RESPONSE_MESSAGE.formatted(statusCode);
            LOGGER.error(msg, ex, DataMapHolder.getLogMap());
            throw new NonRetryableErrorException(msg, ex);
        } else {
            final String msg = API_INFO_RESPONSE_MESSAGE.formatted(statusCode, Arrays.toString(ex.getStackTrace()));
            LOGGER.info(msg, DataMapHolder.getLogMap());
            throw new RetryableErrorException(msg, ex);
        }
    }

    public void handle(URIValidationException ex) {
        LOGGER.error(URI_VALIDATION_EXCEPTION_MESSAGE, DataMapHolder.getLogMap());
        throw new NonRetryableErrorException(URI_VALIDATION_EXCEPTION_MESSAGE, ex);
    }
}
