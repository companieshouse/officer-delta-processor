package uk.gov.companieshouse.officer.delta.processor.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.exception.RetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.model.Officers;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;
import uk.gov.companieshouse.officer.delta.processor.service.api.ApiClientService;
import uk.gov.companieshouse.officer.delta.processor.tranformer.AppointmentTransform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public void process(ChsDelta delta) {
        ObjectMapper objectMapper = new ObjectMapper();
        final String logContext = delta.getContextId();

        try {
            Officers officers = objectMapper.readValue(delta.getData(), Officers.class);

            final List<OfficersItem> officersOfficers = officers.getOfficers();

            for (int i = 0; i < officersOfficers.size(); i++) {
                final OfficersItem officer = officersOfficers.get(i);
                AppointmentAPI appointmentAPI = transformer.transform(officer);
                appointmentAPI.setDeltaAt(officers.getDeltaAt());

                final ApiResponse<Void> response =
                        apiClientService.putAppointment(logContext, officer.getCompanyNumber(),
                                appointmentAPI);
                final HttpStatus httpStatus = HttpStatus.valueOf(response.getStatusCode());

                final Map<String, Object> logMap = new HashMap<>();
                logMap.put("company_number", officer.getCompanyNumber());
                logMap.put("status", httpStatus.toString());

                if(httpStatus.is2xxSuccessful()) {
                    final String msg = String.format("Data for officer[%d] was sent successfully", i);
                    logger.infoContext(logContext, msg, logMap);
                }
                else if (httpStatus.is5xxServerError()) {
                    final String msg = String.format("Failed to send data for officer[%d], retry", i);

                    logger.errorContext(logContext, msg, null, logMap);
                    throw new RetryableErrorException(msg, null);
                }
                else if (httpStatus.is4xxClientError()) {
                    final String msg = String.format("Failed to send data for officer[%d]", i);

                    logger.errorContext(logContext, msg, null, logMap);
                    throw new NonRetryableErrorException(msg, null);
                }
            }
        } catch (JsonProcessingException e) {
            logger.errorContext(logContext, "Unable to JSON parse CHSDelta", e, null);
            throw new NonRetryableErrorException("Unable to JSON parse CHSDelta", e);
        }
    }
}
