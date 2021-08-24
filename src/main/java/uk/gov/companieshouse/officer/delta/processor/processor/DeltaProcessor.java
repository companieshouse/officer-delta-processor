package uk.gov.companieshouse.officer.delta.processor.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officer.delta.processor.exception.ProcessException;
import uk.gov.companieshouse.officer.delta.processor.model.Officers;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;
import uk.gov.companieshouse.officer.delta.processor.service.api.ApiClientService;
import uk.gov.companieshouse.officer.delta.processor.tranformer.Transformer;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DeltaProcessor implements Processor<ChsDelta> {
    final Logger logger;

    final Transformer transformer;
    final ApiClientService apiClientService;

    @Autowired
    public DeltaProcessor(Logger logger, Transformer transformer, ApiClientService apiClientService) {
        this.transformer = transformer;
        this.logger = logger;
        this.apiClientService = apiClientService;
    }

    @Override
    public void process(ChsDelta delta) throws ProcessException {
        logger.info("Processing");

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Officers officers = objectMapper.readValue(delta.getData(), Officers.class);

            List<OfficerAPI> transformedOfficers = officers.getOfficers().stream()
                    .map(transformer::transform)
                    .collect(Collectors.toList());

            Map<String, Object> info = new HashMap<>();
            info.put("output Officers", transformedOfficers);
            logger.debug("Transformed officer", info);

            final OfficersItem officer = officers.getOfficers().get(0);
            final String internalId = Base64.getUrlEncoder().encodeToString(
                    officer.getInternalId().getBytes(StandardCharsets.UTF_8));

            apiClientService.putOfficers(officer.getCompanyNumber(), internalId, transformedOfficers.get(0));
        } catch (JsonProcessingException e) {
            // TODO: figure out how to print exception without dumping sensitive fields
            logger.error("Unable to read JSON from delta: " + ExceptionUtils.getRootCauseMessage(e),
                    e);

            throw ProcessException.fatal("Unable to JSON parse CHSDelta", e);
        }
    }
}
