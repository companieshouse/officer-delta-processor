package uk.gov.companieshouse.officer.delta.processor.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.api.delta.OfficerDeleteDelta;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.exception.RetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.logging.DataMapHolder;
import uk.gov.companieshouse.officer.delta.processor.model.Officers;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;
import uk.gov.companieshouse.officer.delta.processor.service.api.ApiClientService;
import uk.gov.companieshouse.officer.delta.processor.tranformer.AppointmentTransform;
import uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.parseOffsetDateTime;


@Component
public class DeltaProcessor implements Processor<ChsDelta> {
    public static final Pattern PARSE_MESSAGE_PATTERN = Pattern.compile("Source.*line", Pattern.DOTALL);

    private final Logger logger;
    private final AppointmentTransform transformer;
    private final ApiClientService apiClientService;
    private final ObjectMapper objectMapper;

    @Autowired
    public DeltaProcessor(Logger logger, AppointmentTransform transformer, ApiClientService apiClientService,
            ObjectMapper objectMapper) {
        this.logger = logger;
        this.transformer = transformer;
        this.apiClientService = apiClientService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void process(ChsDelta delta) throws RetryableErrorException, NonRetryableErrorException {

        String logContext = delta.getContextId();
        Map<String, Object> logMap = new HashMap<>();

        try {
            Officers officers = objectMapper.readValue(delta.getData(), Officers.class);
            final List<OfficersItem> officersList = officers.getOfficers();

            for (int i = 0; i < officersList.size(); i++) {
                OfficersItem officer = officersList.get(i);
                DataMapHolder.get()
                        .companyNumber(officer.getCompanyNumber())
                        .officerId(officer.getOfficerId());

                logMap = DataMapHolder.getLogMap();
                logger.infoContext(logContext, String.format("Process data for officer [%d]", i), logMap);

                FullRecordCompanyOfficerApi appointmentAPI = transformer.transform(officer);
                appointmentAPI
                        .getInternalData()
                        .setDeltaAt(parseOffsetDateTime("deltaAt", officers.getDeltaAt()));

                ApiResponse<Void> response =
                        apiClientService.putAppointment(logContext, officer.getCompanyNumber(), appointmentAPI);

                handleResponse(null, HttpStatus.valueOf(response.getStatusCode()), logContext,
                        "Response from sending officer data", logMap);
            }
        }
        catch (JsonProcessingException e) {
            /* IMPORTANT: do not propagate the original cause as it contains the full source JSON with
             * potentially sensitive data.
             */
            final String cleanMessage = PARSE_MESSAGE_PATTERN.matcher(e.getMessage()).replaceAll("Source line");
            final NonRetryableErrorException cause = new NonRetryableErrorException(cleanMessage, null);
            logger.errorContext(logContext,
                    "Unable to read JSON from delta: " + ExceptionUtils.getRootCauseMessage(cause), cause, logMap);

            throw new NonRetryableErrorException("Unable to JSON parse CHSDelta", cause);
        }
        catch (ResponseStatusException e) {
            handleResponse(e, e.getStatus(), logContext, "Sending officer data failed", logMap);
        }
        catch (IllegalArgumentException e) {
            // Workaround for Docker router. When service is unavailable: "IllegalArgumentException: expected numeric
            // type but got class uk.gov.companieshouse.api.error.ApiErrorResponse" is thrown when the SDK parses
            // ApiErrorResponseException.
            throw new RetryableErrorException("Failed to send data for officer, retry", e);
        }
    }

    @Override
    public void processDelete(ChsDelta chsDelta) {
        final String logContext = chsDelta.getContextId();
        OfficerDeleteDelta officersDelete;
        try {
            officersDelete = objectMapper.readValue(chsDelta.getData(),
                    OfficerDeleteDelta.class);
        final String companyNumber = officersDelete.getCompanyNumber();
        DataMapHolder.get()
                .companyNumber(companyNumber)
                .officerId(officersDelete.getOfficerId());

        final String internalId = TransformerUtils.encode(officersDelete.getInternalId());
            apiClientService.deleteAppointment(logContext, internalId, companyNumber);
        } catch (ResponseStatusException e) {
            handleResponse(e, e.getStatus(), logContext, "Sending officer delete failed", DataMapHolder.getLogMap());
        } catch (Exception ex) {
            throw new NonRetryableErrorException(
                    "Error when extracting officers delete delta", ex);
        }
    }

    private void handleResponse(final ResponseStatusException ex, final HttpStatus httpStatus, final String logContext,
            final String msg, final Map<String, Object> logMap)
            throws NonRetryableErrorException, RetryableErrorException {
        logMap.put("status", httpStatus.toString());
        if (HttpStatus.BAD_REQUEST == httpStatus) {
            // 400 BAD REQUEST status is not retryable
            logger.errorContext(logContext, msg, null, logMap);
            throw new NonRetryableErrorException(msg, ex);
        }
        else if (httpStatus.is4xxClientError() || httpStatus.is5xxServerError()) {
            // any other client or server status is retryable
            logger.errorContext(logContext, msg + ", retry", null, logMap);
            throw new RetryableErrorException(msg, ex);
        }
        else {
            logger.debugContext(logContext, msg, logMap);
        }
    }
}
