package uk.gov.companieshouse.officier.delta.processor.processr;

import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.officier.delta.processor.exception.ProcessException;

public interface Processor {
    void process(ChsDelta delta) throws ProcessException;
}
