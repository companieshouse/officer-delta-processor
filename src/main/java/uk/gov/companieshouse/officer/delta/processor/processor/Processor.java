package uk.gov.companieshouse.officer.delta.processor.processor;

import uk.gov.companieshouse.officer.delta.processor.exception.ProcessException;

public interface Processor<InputModel> {
    void process(InputModel delta) throws ProcessException;
}
