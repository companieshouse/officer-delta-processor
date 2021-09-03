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
import uk.gov.companieshouse.officer.delta.processor.service.api.ApiClientService;
import uk.gov.companieshouse.officer.delta.processor.transformer.OfficersTransformer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DeltaProcessor implements Processor<ChsDelta> {
    final Logger logger;

    final OfficersTransformer transformer;
    final ApiClientService apiClientService;

    @Autowired
    public DeltaProcessor(Logger logger, OfficersTransformer transformer, ApiClientService apiClientService) {
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
            List<AppointmentAPI> xformed = transformer.transform(officers);

            Map<String, Object> info = new HashMap<>();
            info.put("output Appointments",
                    ReflectionToStringBuilder.toString(xformed.get(0), ToStringStyle.SHORT_PREFIX_STYLE));
            logger.debug("Transformed officers", info);

            xformed.forEach(app -> apiClientService.putAppointment(app.getData().getCompanyNumber(),
                    app.getId(),
                    app));
        } catch (JsonProcessingException e) {
            // TODO: figure out how to print exception without dumping sensitive fields
            logger.error("Unable to read JSON from delta: " + ExceptionUtils.getRootCauseMessage(e),
                    e);

            throw ProcessException.fatal("Unable to JSON parse CHSDelta", e);
        } catch (Throwable e) {
            // TODO: figure out how to print exception without dumping sensitive fields

            throw ProcessException.fatal("Unable to JSON parse CHSDelta", e);
        }
    }
}
