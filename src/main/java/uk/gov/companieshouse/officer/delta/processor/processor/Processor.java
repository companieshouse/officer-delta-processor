package uk.gov.companieshouse.officer.delta.processor.processor;

public interface Processor<I> {
    void process(I delta);
}
