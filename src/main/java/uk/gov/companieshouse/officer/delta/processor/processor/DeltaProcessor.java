package uk.gov.companieshouse.officer.delta.processor.processor;

import static uk.gov.companieshouse.officer.delta.processor.OfficerDeltaProcessorApplication.APPLICATION_NAME_SPACE;
import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.parseOffsetDateTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.api.delta.OfficerDeleteDelta;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.exception.RetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.logging.DataMapHolder;
import uk.gov.companieshouse.officer.delta.processor.model.DeleteAppointmentParameters;
import uk.gov.companieshouse.officer.delta.processor.model.Officers;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;
import uk.gov.companieshouse.officer.delta.processor.service.api.ApiClientService;
import uk.gov.companieshouse.officer.delta.processor.tranformer.AppointmentTransform;
import uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils;


/**
 * The type Delta processor.
 */
@Component
public class DeltaProcessor implements Processor<ChsDelta> {

    public static final Pattern PARSE_MESSAGE_PATTERN = Pattern.compile("Source.*line",
            Pattern.DOTALL);
    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    private final AppointmentTransform transformer;
    private final ApiClientService apiClientService;
    private final ObjectMapper objectMapper;

    /**
     * Instantiates a new Delta processor.
     *
     * @param transformer      the transformer
     * @param apiClientService the api client service
     * @param objectMapper     the object mapper
     */
    @Autowired
    public DeltaProcessor(AppointmentTransform transformer, ApiClientService apiClientService,
            ObjectMapper objectMapper) {
        this.transformer = transformer;
        this.apiClientService = apiClientService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void process(ChsDelta delta) {
        try {
            Officers officers = objectMapper.readValue(delta.getData(), Officers.class);
            final List<OfficersItem> officersList = officers.getOfficers();

            for (OfficersItem officer : officersList) {
                DataMapHolder.get().companyNumber(officer.getCompanyNumber())
                        .officerId(officer.getOfficerId()).internalId(officer.getInternalId())
                        .appointmentId(TransformerUtils.encode(officer.getInternalId()));

                FullRecordCompanyOfficerApi appointmentApi = transformer.transform(officer);
                appointmentApi.getInternalData()
                        .setDeltaAt(parseOffsetDateTime("deltaAt", officers.getDeltaAt()));

                apiClientService.putAppointment(officer.getCompanyNumber(), appointmentApi);
            }
        } catch (JsonProcessingException ex) {
            /* IMPORTANT: do not propagate the original cause as it contains the full source JSON
             with potentially sensitive data.
             */
            final String cleanMessage = PARSE_MESSAGE_PATTERN.matcher(ex.getMessage())
                    .replaceAll("Source line");
            final NonRetryableErrorException cause = new NonRetryableErrorException(cleanMessage,
                    null);
            LOGGER.error(
                    "Unable to read JSON from delta: " + ExceptionUtils.getRootCauseMessage(cause),
                    cause, DataMapHolder.getLogMap());

            throw new NonRetryableErrorException("Unable to JSON parse CHSDelta", cause);
        } catch (IllegalArgumentException ex) {
            // Workaround for Docker router. When service is unavailable:
            // "IllegalArgumentException: expected numeric type but got class uk.gov
            // .companieshouse.api.error.ApiErrorResponse" is thrown
            // when the SDK parses ApiErrorResponseException.
            LOGGER.info("Failed to process officer delta", DataMapHolder.getLogMap());
            throw new RetryableErrorException("Failed to send data for officer, retry", ex);
        }
    }

    @Override
    public void processDelete(ChsDelta chsDelta) {
        OfficerDeleteDelta officersDelete;
        try {
            officersDelete = objectMapper.readValue(chsDelta.getData(), OfficerDeleteDelta.class);
        } catch (JsonProcessingException ex) {
            LOGGER.error("Error when extracting officers delete delta", ex,
                    DataMapHolder.getLogMap());
            throw new NonRetryableErrorException("Error when extracting officers delete delta", ex);
        }

        final String companyNumber = officersDelete.getCompanyNumber();
        final String officerId = officersDelete.getOfficerId();
        final String internalId = officersDelete.getInternalId();
        final String encodedInternalId = TransformerUtils.encode(internalId);
        DataMapHolder.get().companyNumber(companyNumber).officerId(officerId).internalId(internalId)
                .appointmentId(encodedInternalId);

        final String encodedOfficerId = TransformerUtils.encode(officerId);
        apiClientService.deleteAppointment(
                DeleteAppointmentParameters.builder().encodedInternalId(encodedInternalId)
                        .companyNumber(companyNumber).deltaAt(officersDelete.getDeltaAt())
                        .encodedOfficerId(encodedOfficerId).build());
    }
}
