package uk.gov.companieshouse.officer.delta.processor.processor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.companieshouse.officer.delta.processor.tranformer.IdentificationTransform;
import uk.gov.companieshouse.officer.delta.processor.tranformer.OfficerTransform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.EnumSet;
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
        final Resource jsonFile = new ClassPathResource("officer_delta_dummy.json");

        json = new BufferedReader(new InputStreamReader(jsonFile.getInputStream())).lines()
                .collect(Collectors.joining("\n"));
        IdentificationTransform idTransform = new IdentificationTransform();
        OfficerTransform officerTransform = new OfficerTransform(idTransform);
        appointmentTransform = new AppointmentTransform(officerTransform);
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

    @BeforeEach
    void setUp() {
        testProcessor = new DeltaProcessor(logger, appointmentTransform, apiClientService);
    }

    @Test
    void process() throws NonRetryableErrorException, RetryableErrorException {
        final ChsDelta delta = new ChsDelta(json, 0, CONTEXT_ID);
        final String expectedNumber = expectedAppointment.getData().getCompanyNumber();
        final ApiResponse<Void> response = new ApiResponse<>(HttpStatus.OK.value(), null, null);

        when(apiClientService.putAppointment(CONTEXT_ID, expectedNumber, expectedAppointment)).thenReturn(response);

        testProcessor.process(delta);

        verify(apiClientService).putAppointment(CONTEXT_ID, expectedNumber, expectedAppointment);
        verifyNoMoreInteractions(apiClientService);
    }

    @Test
    void processWhenJsonParseFailureThenContentRedacted() {
        final String badJson = json.replace(":", "-");
        final ChsDelta delta = new ChsDelta(badJson, 0, CONTEXT_ID);

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
    void processWhenClientServiceServerError(final HttpStatus serverErrorStatus) {
        final ChsDelta delta = new ChsDelta(json, 0, CONTEXT_ID);
        final String expectedNumber = expectedAppointment.getData().getCompanyNumber();
        final ApiResponse<Void> response = new ApiResponse<>(serverErrorStatus.value(), null, null);

        when(apiClientService.putAppointment(CONTEXT_ID, expectedNumber, expectedAppointment)).thenReturn(response);

        assertThrows(RetryableErrorException.class, () -> testProcessor.process(delta));

        final InOrder inOrder = inOrder(logger, apiClientService);

        inOrder.verify(apiClientService).putAppointment(CONTEXT_ID, expectedNumber, expectedAppointment);
        inOrder.verify(logger).errorContext(eq(CONTEXT_ID), anyString(), isNull(), isNull());
        inOrder.verifyNoMoreInteractions();

    }

    @Test
    void processWhenClientServiceThrowsIllegalArgumentException() {
        final ChsDelta delta = new ChsDelta(json, 0, CONTEXT_ID);
        final String expectedNumber = expectedAppointment.getData().getCompanyNumber();

        doThrow(new IllegalArgumentException("simulate parsing error in api SDK")).when(apiClientService)
                .putAppointment(CONTEXT_ID, expectedNumber, expectedAppointment);

        assertThrows(RetryableErrorException.class, () -> testProcessor.process(delta));

        final InOrder inOrder = inOrder(logger, apiClientService);

        inOrder.verify(apiClientService).putAppointment(CONTEXT_ID, expectedNumber, expectedAppointment);
        inOrder.verify(logger).errorContext(eq(CONTEXT_ID), anyString(), any(IllegalArgumentException.class), isNull());
        inOrder.verifyNoMoreInteractions();

    }

    private static Stream<HttpStatus> provideNonRetryableStatuses() {
        return Stream.of(HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest
    @MethodSource("provideNonRetryableStatuses")
    void processWhenClientServiceClientError(final HttpStatus serverErrorStatus) {
        final ChsDelta delta = new ChsDelta(json, 0, CONTEXT_ID);
        final String expectedNumber = expectedAppointment.getData().getCompanyNumber();
        final ApiResponse<Void> response = new ApiResponse<>(serverErrorStatus.value(), null, null);

        when(apiClientService.putAppointment(CONTEXT_ID, expectedNumber, expectedAppointment)).thenReturn(response);

        assertThrows(NonRetryableErrorException.class, () -> testProcessor.process(delta));

        final InOrder inOrder = inOrder(logger, apiClientService);

        inOrder.verify(apiClientService).putAppointment(CONTEXT_ID, expectedNumber, expectedAppointment);
        inOrder.verify(logger).errorContext(eq(CONTEXT_ID), anyString(), isNull(), isNull());
        inOrder.verifyNoMoreInteractions();

    }

}
