package uk.gov.companieshouse.officier.delta.processor.processr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officier.delta.processor.exception.ProcessException;

@Component
public class ProcessorImpl implements Processor {
    Logger logger;

    @Autowired
    public ProcessorImpl(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void process(ChsDelta delta) throws ProcessException {
        logger.info("Processing");
    }
}
