package uk.gov.companieshouse.officier.delta.processor.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;

@Component
public class DeltaProcessor implements Processor<ChsDelta> {
    Logger logger;

    @Autowired
    public DeltaProcessor(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void process(ChsDelta delta) {
        logger.info("Processing");
    }
}
