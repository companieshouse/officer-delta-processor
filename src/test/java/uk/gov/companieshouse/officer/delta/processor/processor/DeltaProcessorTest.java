package uk.gov.companieshouse.officer.delta.processor.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.parseOffsetDateTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.api.delta.OfficerDeleteDelta;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.officer.delta.processor.config.OfficerRoleConfig;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.exception.RetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.model.Officers;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;
import uk.gov.companieshouse.officer.delta.processor.service.api.ApiClientService;
import uk.gov.companieshouse.officer.delta.processor.tranformer.AppointmentTransform;
import uk.gov.companieshouse.officer.delta.processor.tranformer.IdentificationTransform;
import uk.gov.companieshouse.officer.delta.processor.tranformer.PrincipalOfficeAddressTransform;
import uk.gov.companieshouse.officer.delta.processor.tranformer.ServiceAddressTransform;
import uk.gov.companieshouse.officer.delta.processor.tranformer.FormerNameTransform;
import uk.gov.companieshouse.officer.delta.processor.tranformer.UsualResidentialAddressTransform;
import uk.gov.companieshouse.officer.delta.processor.tranformer.OfficerTransform;
import uk.gov.companieshouse.officer.delta.processor.tranformer.SensitiveOfficerTransform;
import uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class DeltaProcessorTest {
    private static final String CONTEXT_ID = "context_id";

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private static AppointmentTransform appointmentTransform;
    private static String json;
    private static FullRecordCompanyOfficerApi expectedAppointment;

    private DeltaProcessor testProcessor;

    @Mock
    private ApiClientService apiClientService;

    @Captor
    private ArgumentCaptor<FullRecordCompanyOfficerApi> appointmentCapture;

    @BeforeAll
    static void beforeAll() throws IOException, NonRetryableErrorException {
        json = loadJson("officer_delta_example_2.json");
        OfficerTransform officerTransform = new OfficerTransform(new IdentificationTransform(),
                new ServiceAddressTransform(),
                new FormerNameTransform(),
                new PrincipalOfficeAddressTransform());
        SensitiveOfficerTransform sensitiveOfficerTransform = new SensitiveOfficerTransform(
                new UsualResidentialAddressTransform());
        Map<String, Integer> resigned = Map.of("director", 200);
        OfficerRoleConfig officerRoleConfig = new OfficerRoleConfig(new HashMap<>(), resigned);
        appointmentTransform = new AppointmentTransform(officerTransform, sensitiveOfficerTransform, officerRoleConfig);
        expectedAppointment = jsonToAppointment(json);
    }

    private static FullRecordCompanyOfficerApi jsonToAppointment(final String json)
            throws JsonProcessingException, NonRetryableErrorException {

        final Officers officers = objectMapper.readValue(json, Officers.class);
        final List<OfficersItem> officersOfficers = officers.getOfficers();
        final OfficersItem officer = officersOfficers.get(0);
        final FullRecordCompanyOfficerApi appointmentAPI = appointmentTransform.transform(officer);

        appointmentAPI.getInternalData().setDeltaAt(parseOffsetDateTime("deltaAt", officers.getDeltaAt()));

        return appointmentAPI;
    }

    private static OfficerDeleteDelta jsonToDelete(final String json)
            throws JsonProcessingException, NonRetryableErrorException {
        return objectMapper.readValue(json, OfficerDeleteDelta.class);
    }

    @BeforeEach
    void setUp() {
        testProcessor = new DeltaProcessor(appointmentTransform, apiClientService,
                objectMapper);
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void process(CapturedOutput capture) {
        final ChsDelta delta = new ChsDelta(json, 0, CONTEXT_ID, false);
        final String expectedNumber = expectedAppointment.getExternalData().getCompanyNumber();
        final ApiResponse<Void> response = new ApiResponse<>(HttpStatus.OK.value(), null, null);

        when(apiClientService.putAppointment(CONTEXT_ID, expectedNumber, expectedAppointment)).thenReturn(response);

        testProcessor.process(delta);

        verify(apiClientService).putAppointment(CONTEXT_ID, expectedNumber, expectedAppointment);
        verifyNoMoreInteractions(apiClientService);
        assertThat(capture.getOut()).doesNotContain("event: error");
    }

    @Test
    void processWhenJsonParseFailureThenContentRedacted() {
        final String badJson = json.replace(":", "-");
        final ChsDelta delta = new ChsDelta(badJson, 0, CONTEXT_ID, false);

        final NonRetryableErrorException exception =
                assertThrows(NonRetryableErrorException.class, () -> testProcessor.process(delta));
        final String redactedMessage = "Unexpected character ('-' (code 45)): was expecting a colon"
                + " to separate field name and value\n at [Source line: 2, column: 14]";

        assertThat(exception.getMessage(), is("Unable to JSON parse CHSDelta"));
        assertThat(exception.getCause().getMessage(), is(redactedMessage));

        final InOrder inOrder = inOrder(apiClientService);

        inOrder.verifyNoMoreInteractions();
    }

    @ParameterizedTest
    @MethodSource("provideRetryableStatuses")
    void processWhenResponseStatusRetryable(final HttpStatus responseStatus) {
        final ChsDelta delta = new ChsDelta(json, 0, CONTEXT_ID, false);
        final String expectedNumber = expectedAppointment.getExternalData().getCompanyNumber();

        when(apiClientService.putAppointment(CONTEXT_ID, expectedNumber, expectedAppointment)).thenThrow(
                new ResponseStatusException(responseStatus));

        assertThrows(RetryableErrorException.class, () -> testProcessor.process(delta));

        final InOrder inOrder = inOrder(apiClientService);

        inOrder.verify(apiClientService).putAppointment(CONTEXT_ID, expectedNumber, expectedAppointment);
        inOrder.verifyNoMoreInteractions();

    }

    private static Stream<HttpStatus> provideRetryableStatuses() {
        return EnumSet.allOf(HttpStatus.class).stream().filter(s -> s.value() > HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void processWhenClientServiceThrowsIllegalArgumentException() {
        final ChsDelta delta = new ChsDelta(json, 0, CONTEXT_ID, false);
        final String expectedNumber = expectedAppointment.getExternalData().getCompanyNumber();

        doThrow(new IllegalArgumentException("simulate parsing error in api SDK")).when(apiClientService)
                .putAppointment(CONTEXT_ID, expectedNumber, expectedAppointment);

        assertThrows(RetryableErrorException.class, () -> testProcessor.process(delta));

        final InOrder inOrder = inOrder(apiClientService);

        inOrder.verify(apiClientService).putAppointment(CONTEXT_ID, expectedNumber, expectedAppointment);
        inOrder.verifyNoMoreInteractions();
    }

    @ParameterizedTest
    @MethodSource("provideNonRetryableStatuses")
    void processWhenResponseStatusNonRetryable(final HttpStatus responseStatus) {
        final ChsDelta delta = new ChsDelta(json, 0, CONTEXT_ID, false);
        final String expectedNumber = expectedAppointment.getExternalData().getCompanyNumber();

        when(apiClientService.putAppointment(CONTEXT_ID, expectedNumber, expectedAppointment)).thenThrow(
                new ResponseStatusException(responseStatus));

        assertThrows(NonRetryableErrorException.class, () -> testProcessor.process(delta));

        final InOrder inOrder = inOrder(apiClientService);

        inOrder.verify(apiClientService).putAppointment(CONTEXT_ID, expectedNumber, expectedAppointment);
        inOrder.verifyNoMoreInteractions();
    }

    private static Stream<HttpStatus> provideNonRetryableStatuses() {
        return Stream.of(HttpStatus.BAD_REQUEST, HttpStatus.CONFLICT);
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void processDelete(CapturedOutput capture) throws IOException {
        String deleteJson = loadJson("officer_delete_delta.json");
        OfficerDeleteDelta expectedDelete = jsonToDelete(deleteJson);
        final ChsDelta delta = new ChsDelta(deleteJson, 0, CONTEXT_ID, true);
        final String expectedNumber = expectedDelete.getCompanyNumber();
        final String expectedInternalId = TransformerUtils.encode(expectedDelete.getInternalId());
        final ApiResponse<Void> response = new ApiResponse<>(HttpStatus.OK.value(), null, null);

        when(apiClientService.deleteAppointment(CONTEXT_ID, expectedInternalId, expectedNumber)).thenReturn(response);

        testProcessor.processDelete(delta);

        verify(apiClientService).deleteAppointment(CONTEXT_ID, expectedInternalId, expectedNumber);
        verifyNoMoreInteractions(apiClientService);
        assertThat(capture.getOut()).doesNotContain("event: error");
    }

    @Test
    void brokenDeleteThrowsNonRetryableError() throws IOException {
        String brokendelete = loadJson("broken_delta.json");
        final ChsDelta delta = new ChsDelta(brokendelete, 0, CONTEXT_ID, true);

        assertThrows(NonRetryableErrorException.class, ()->testProcessor.processDelete(delta));
    }

    private static String loadJson(String filename) throws IOException {
        final Resource jsonFile = new ClassPathResource(filename);
         return new BufferedReader(new InputStreamReader(jsonFile.getInputStream())).lines()
                    .collect(Collectors.joining("\n"));
    }

    @ParameterizedTest
    @CsvSource({
            "20230125171003950844,1674666603950",
            "20230325235959950844,1679788799950",
            "20230614091500950844,1686734100950",
            "20231029010000950844,1698541200950",
            "20231029020000950844,1698544800950"
    })
    void processDeltaAtConversion(String chipsDeltaAt, long serialisedDeltaAt) {

        String deltaMessage = json.replaceFirst("\"delta_at\": \"(.*)\"",
                String.format("\"delta_at\": \"%s\"", chipsDeltaAt));

        final ChsDelta delta = new ChsDelta(deltaMessage, 0, CONTEXT_ID, false);
        final ApiResponse<Void> response = new ApiResponse<>(HttpStatus.OK.value(), null, null);

        when(apiClientService.putAppointment(eq(CONTEXT_ID), any(), any())).thenReturn(response);

        testProcessor.process(delta);

        verify(apiClientService).putAppointment(eq(CONTEXT_ID), any(), appointmentCapture.capture());
        assertThat(appointmentCapture.getValue().getInternalData().getDeltaAt().toInstant().toEpochMilli(),
                is(serialisedDeltaAt));
    }
}
