package uk.gov.companieshouse.officer.delta.processor.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officer.delta.processor.exception.ProcessException;
import uk.gov.companieshouse.officer.delta.processor.model.Officers;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;
import uk.gov.companieshouse.officer.delta.processor.service.api.ApiClientService;
import uk.gov.companieshouse.officer.delta.processor.transformer.OfficersTransformer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.gov.companieshouse.officer.delta.processor.tranformer.AppointmentTransform;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class DeltaProcessor implements Processor<ChsDelta> {
    final Logger logger;

    final AppointmentTransform transformer;
    final ApiClientService apiClientService;

    @Autowired
    public DeltaProcessor(Logger logger,
                          AppointmentTransform transformer,
                          ApiClientService apiClientService) {
        this.logger = logger;
        this.transformer = transformer;
        this.apiClientService = apiClientService;
    }

    @Override
    public void process(ChsDelta delta) throws ProcessException {
        logger.infoContext(delta.getContextId(), "Processing", null);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Officers officers = objectMapper.readValue(delta.getData(), Officers.class);

            for (OfficersItem officer : officers.getOfficers()) {
                AppointmentAPI appointmentAPI = transformer.transform(officer);
                appointmentAPI.setDeltaAt(officers.getDeltaAt());

                // This will be moved to the transformer
                final String internalId = Base64.getUrlEncoder().encodeToString(
                        officer.getInternalId().getBytes(StandardCharsets.UTF_8));

                // Should be be making API calls for each officer or should be batch them together?
                apiClientService.putAppointment(officer.getCompanyNumber(), internalId, appointmentAPI);
            }
        } catch (JsonProcessingException e) {
            // TODO: figure out how to print exception without dumping sensitive fields
            logger.errorContext(delta.getContextId(),
                    "Unable to read JSON from delta: " + ExceptionUtils.getRootCauseMessage(e),
                    e,
                    null);

            throw ProcessException.fatal("Unable to JSON parse CHSDelta", e);
        } catch (Throwable e) {
            // TODO: figure out how to print exception without dumping sensitive fields

            throw ProcessException.fatal("Unable to JSON parse CHSDelta", e);
        }
    }
}
