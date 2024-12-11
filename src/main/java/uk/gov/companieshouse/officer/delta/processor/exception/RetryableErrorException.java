package uk.gov.companieshouse.officer.delta.processor.exception;

import java.io.Serial;

/**
 * Thrown to indicate a recoverable error in processing that can be tried again. An example of a
 * recoverable error is a network connectivity error while accessing an external api that may go
 * away during subsequent retries.
 */
public class RetryableErrorException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public RetryableErrorException(String message, Exception cause) {
        super(message, cause);
    }

}
