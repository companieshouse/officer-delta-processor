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

import java.util.List;
import java.util.regex.Pattern;

@Component
public class DeltaProcessor implements Processor<ChsDelta> {
    public static final Pattern PARSE_MESSAGE_PATTERN = Pattern.compile("Source.*line", Pattern.DOTALL);

    final Logger logger;

    final AppointmentTransform transformer;
    final ApiClientService apiClientService;

    @Autowired
    public DeltaProcessor(Logger logger, AppointmentTransform transformer, ApiClientService apiClientService) {
        this.logger = logger;
        this.transformer = transformer;
        this.apiClientService = apiClientService;
    }

    @Override
    public void process(ChsDelta delta) throws RetryableErrorException, NonRetryableErrorException {
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

                if (httpStatus.is5xxServerError()) {
                    final String msg = String.format("Failed to send data for officer[%d], retry", i);
                    logger.errorContext(logContext, msg, null, null);
                    throw new RetryableErrorException(msg, null);
                }
                else if (httpStatus.is4xxClientError()) {
                    final String msg = String.format("Failed to send data for officer[%d]", i);

                    logger.errorContext(logContext, msg, null, null);
                    throw new NonRetryableErrorException(msg, null);
                }
            }
        }
        catch (JsonProcessingException e) {
            /* IMPORTANT: do not propagate the original cause as it contains the full source JSON with
             * potentially sensitive data.
             */
            final String cleanMessage = PARSE_MESSAGE_PATTERN.matcher(e.getMessage()).replaceAll("Source line");
            final NonRetryableErrorException cause = new NonRetryableErrorException(cleanMessage, null);
            logger.errorContext(logContext,
                    "Unable to read JSON from delta: " + ExceptionUtils.getRootCauseMessage(cause), cause, null);

            throw new NonRetryableErrorException("Unable to JSON parse CHSDelta", cause);
        }
        catch (IllegalArgumentException e) {
            // Workaround. When service is unavailable: "IllegalArgumentException: expected numeric type but got class
            // uk.gov.companieshouse.api.error.ApiErrorResponse" may be thrown when the SDK parses
            // ApiErrorResponseException.
            logger.errorContext(logContext, "Failed to send data for officer: " + ExceptionUtils.getRootCauseMessage(e),
                    e, null);
            throw new RetryableErrorException("Failed to send data for officer, retry", e);
        }
    }
}
