package uk.gov.companieshouse.officier.delta.processor.exception;

public class ProcessException extends Exception {
    public boolean canRetry;

    public ProcessException(String message, Throwable cause, boolean canRetry) {
        super(message, cause);
        this.canRetry = canRetry;
    }
}
