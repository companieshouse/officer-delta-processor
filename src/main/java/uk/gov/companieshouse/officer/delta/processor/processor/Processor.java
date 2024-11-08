package uk.gov.companieshouse.officer.delta.processor.processor;

import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.exception.RetryableErrorException;

public interface Processor<I> {

    void process(I delta) throws RetryableErrorException, NonRetryableErrorException;

    void processDelete(I delta) throws NonRetryableErrorException;
}
