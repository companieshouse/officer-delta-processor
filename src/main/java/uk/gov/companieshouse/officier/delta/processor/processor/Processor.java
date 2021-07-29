package uk.gov.companieshouse.officier.delta.processor.processor;

import uk.gov.companieshouse.officier.delta.processor.exception.ProcessException;

public interface Processor<T> {
    void process(T delta) throws ProcessException;
}
