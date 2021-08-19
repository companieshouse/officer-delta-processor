package uk.gov.companieshouse.officer.delta.processor.processor;

import uk.gov.companieshouse.officer.delta.processor.exception.ProcessException;

public interface Processor<I> {
    void process(I delta) throws ProcessException;
}
