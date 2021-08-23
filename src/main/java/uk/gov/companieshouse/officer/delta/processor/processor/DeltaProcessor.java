package uk.gov.companieshouse.officer.delta.processor.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officer.delta.processor.exception.ProcessException;
import uk.gov.companieshouse.officer.delta.processor.model.Officers;

@Component
public class DeltaProcessor implements Processor<ChsDelta> {
    final Logger logger;

    @Autowired
    public DeltaProcessor(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void process(ChsDelta delta) throws ProcessException {
        logger.info("Processing");

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Officers officers = objectMapper.readValue(delta.getData(), Officers.class);

        } catch (JsonProcessingException e) {
            // TODO: figure out how to print exception without dumping sensitive fields
            logger.error("Unable to read JSON from delta: " + ExceptionUtils.getRootCauseMessage(e),
                    e);

            throw ProcessException.fatal("Unable to JSON parse CHSDelta", e);
        }
    }
}
