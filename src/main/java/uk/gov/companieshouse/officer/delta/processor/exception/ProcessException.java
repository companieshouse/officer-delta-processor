package uk.gov.companieshouse.officer.delta.processor.exception;

/**
 * Process exception encapsulates exceptions occurred during processing. Indicating weather the
 * exception is retry-able (so the message can be retried) or fatal to the message is added
 * to the error topic.
 */
public class ProcessException extends Exception {
    private final boolean canRetry;

    public ProcessException(String message, Throwable cause, boolean canRetry) {
        super(message, cause);
        this.canRetry = canRetry;
    }

    /**
     * Creates an exception which indicates processing cannot continue
     *
     * @param message a message to show what when wrong
     * @param cause   a parent exception for when this exception is wrapping another
     * @return an exception that can be thrown within the processor
     */
    public static ProcessException fatal(String message, Throwable cause) {
        return new ProcessException(message, cause, false);
    }

    /**
     * Creates an exception that indicates the processing should stop, but can be reprocessed
     *
     * @param message a message to show what when wrong
     * @param cause   a parent exception for when this exception is wrapping another
     * @return an exception that can be thrown within the processor
     */
    public static ProcessException nonFatal(String message, Throwable cause) {
        return new ProcessException(message, cause, true);
    }

    public boolean canRetry() {
        return canRetry;
    }
}
