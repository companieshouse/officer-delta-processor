package uk.gov.companieshouse.officer.delta.processor.apiclient;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.exception.RetryableErrorException;

@ExtendWith(MockitoExtension.class)
class ResponseHandlerTest {

    private final ResponseHandler responseHandler = new ResponseHandler();

    @Mock
    private ApiErrorResponseException apiErrorResponseException;
    @Mock
    private URIValidationException uriValidationException;

    @ParameterizedTest
    @CsvSource({
            "401",
            "403",
            "404",
            "405",
            "500",
            "501",
            "502",
            "503",
            "504"
    })
    void shouldHandleRetryableApiErrorResponseException(final int statusCode) {
        // given
        when(apiErrorResponseException.getStatusCode()).thenReturn(statusCode);

        // when
        Executable executable = () -> responseHandler.handle(apiErrorResponseException);

        // then
        assertThrows(RetryableErrorException.class, executable);
    }

    @ParameterizedTest
    @CsvSource({
            "400",
            "409"
    })
    void shouldHandleNonRetryableApiErrorResponseException(final int statusCode) {
        // given
        when(apiErrorResponseException.getStatusCode()).thenReturn(statusCode);

        // when
        Executable executable = () -> responseHandler.handle(apiErrorResponseException);

        // then
        assertThrows(NonRetryableErrorException.class, executable);
    }

    @Test
    void shouldHandleURIValidationExceptionByThrowingNonRetryableException() {
        // given

        // when
        Executable executable = () -> responseHandler.handle(uriValidationException);

        // then
        assertThrows(NonRetryableErrorException.class, executable);
    }
}