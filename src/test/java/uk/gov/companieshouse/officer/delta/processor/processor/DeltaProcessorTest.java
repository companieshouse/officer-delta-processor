package uk.gov.companieshouse.officer.delta.processor.processor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.companieshouse.api.delta.OfficerDeleteDelta;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officer.delta.processor.config.OfficerRoleConfig;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.exception.RetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.model.Officers;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;
import uk.gov.companieshouse.officer.delta.processor.service.api.ApiClientService;
import uk.gov.companieshouse.officer.delta.processor.tranformer.AppointmentTransform;
import uk.gov.companieshouse.officer.delta.processor.tranformer.IdentificationTransform;
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

    private static AppointmentTransform appointmentTransform;
    private static String json;
    private static AppointmentAPI expectedAppointment;

    private DeltaProcessor testProcessor;

    @Mock
    private ApiClientService apiClientService;
    @Mock
    private Logger logger;

    @BeforeAll
    static void beforeAll() throws IOException, NonRetryableErrorException {
        json = loadJson("officer_delta_dummy.json");
        IdentificationTransform idTransform = new IdentificationTransform();
        OfficerTransform officerTransform = new OfficerTransform(idTransform);
        SensitiveOfficerTransform sensitiveOfficerTransform = new SensitiveOfficerTransform();
        HashMap<String, Integer> resigned = new HashMap<>();
        resigned.put("director", 200);
        OfficerRoleConfig officerRoleConfig = new OfficerRoleConfig(new HashMap<>(), resigned);
        appointmentTransform = new AppointmentTransform(officerTransform, sensitiveOfficerTransform, officerRoleConfig);
        expectedAppointment = jsonToAppointment(json);
    }

    private static AppointmentAPI jsonToAppointment(final String json)
            throws JsonProcessingException, NonRetryableErrorException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Officers officers = objectMapper.readValue(json, Officers.class);
        final List<OfficersItem> officersOfficers = officers.getOfficers();
        final OfficersItem officer = officersOfficers.get(0);
        final AppointmentAPI appointmentAPI = appointmentTransform.transform(officer);

        appointmentAPI.setDeltaAt(officers.getDeltaAt());

        return appointmentAPI;
    }

    private static OfficerDeleteDelta jsonToDelete(final String json)
            throws JsonProcessingException, NonRetryableErrorException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final OfficerDeleteDelta officerDelete = objectMapper.readValue(json, OfficerDeleteDelta.class);
        return officerDelete;
    }

    @BeforeEach
    void setUp() {
        testProcessor = new DeltaProcessor(logger, appointmentTransform, apiClientService);
    }

    @Test
    void process() throws NonRetryableErrorException, RetryableErrorException {
        final ChsDelta delta = new ChsDelta(json, 0, CONTEXT_ID, false);
        final String expectedNumber = expectedAppointment.getCompanyNumber();
        final ApiResponse<Void> response = new ApiResponse<>(HttpStatus.OK.value(), null, null);

        when(apiClientService.putAppointment(CONTEXT_ID, expectedNumber, expectedAppointment)).thenReturn(response);

        testProcessor.process(delta);

        verify(apiClientService).putAppointment(CONTEXT_ID, expectedNumber, expectedAppointment);
        verifyNoMoreInteractions(apiClientService);
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

        final InOrder inOrder = inOrder(logger, apiClientService);

        inOrder.verify(logger).errorContext(anyString(), anyString(), any(NonRetryableErrorException.class), isNull());
        inOrder.verifyNoMoreInteractions();

    }

    private static Stream<HttpStatus> provideRetryableStatuses() {
        return EnumSet.allOf(HttpStatus.class).stream().filter(s -> s.value() > HttpStatus.BAD_REQUEST.value());
    }

    @ParameterizedTest
    @MethodSource("provideRetryableStatuses")
    void processWhenResponseStatusRetryable(final HttpStatus responseStatus) {
        final ChsDelta delta = new ChsDelta(json, 0, CONTEXT_ID, false);
        final String expectedNumber = expectedAppointment.getCompanyNumber();

        when(apiClientService.putAppointment(CONTEXT_ID, expectedNumber, expectedAppointment)).thenThrow(
                new ResponseStatusException(responseStatus));

        assertThrows(RetryableErrorException.class, () -> testProcessor.process(delta));

        final InOrder inOrder = inOrder(logger, apiClientService);

        inOrder.verify(apiClientService).putAppointment(CONTEXT_ID, expectedNumber, expectedAppointment);
        inOrder.verify(logger).errorContext(eq(CONTEXT_ID), anyString(), isNull(), isNotNull());
        inOrder.verifyNoMoreInteractions();

    }

    @Test
    void processWhenClientServiceThrowsIllegalArgumentException() {
        final ChsDelta delta = new ChsDelta(json, 0, CONTEXT_ID, false);
        final String expectedNumber = expectedAppointment.getCompanyNumber();

        doThrow(new IllegalArgumentException("simulate parsing error in api SDK")).when(apiClientService)
                .putAppointment(CONTEXT_ID, expectedNumber, expectedAppointment);

        assertThrows(RetryableErrorException.class, () -> testProcessor.process(delta));

        final InOrder inOrder = inOrder(logger, apiClientService);

        inOrder.verify(apiClientService).putAppointment(CONTEXT_ID, expectedNumber, expectedAppointment);
        inOrder.verifyNoMoreInteractions();

    }

    private static Stream<HttpStatus> provideNonRetryableStatuses() {
        return Stream.of(HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest
    @MethodSource("provideNonRetryableStatuses")
    void processWhenResponseStatusNonRetryable(final HttpStatus responseStatus) {
        final ChsDelta delta = new ChsDelta(json, 0, CONTEXT_ID, false);
        final String expectedNumber = expectedAppointment.getCompanyNumber();

        when(apiClientService.putAppointment(CONTEXT_ID, expectedNumber, expectedAppointment)).thenThrow(
                new ResponseStatusException(responseStatus));

        assertThrows(NonRetryableErrorException.class, () -> testProcessor.process(delta));

        final InOrder inOrder = inOrder(logger, apiClientService);

        inOrder.verify(apiClientService).putAppointment(CONTEXT_ID, expectedNumber, expectedAppointment);
        inOrder.verify(logger).errorContext(eq(CONTEXT_ID), anyString(), isNull(), isNotNull());
        inOrder.verifyNoMoreInteractions();

    }

    @Test
    void processDelete() throws IOException, NonRetryableErrorException {
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
    }

    @Test
    void brokenDeleteThrowsNonRetryableError() throws IOException, NonRetryableErrorException {
        String brokendelete = loadJson("broken_delta.json");
        final ChsDelta delta = new ChsDelta(brokendelete, 0, CONTEXT_ID, true);

        Assert.assertThrows(NonRetryableErrorException.class, ()->testProcessor.processDelete(delta));
    }

    private static String loadJson(String filename) throws IOException {
        final Resource jsonFile = new ClassPathResource(filename);
         return new BufferedReader(new InputStreamReader(jsonFile.getInputStream())).lines()
                    .collect(Collectors.joining("\n"));
    }

}
