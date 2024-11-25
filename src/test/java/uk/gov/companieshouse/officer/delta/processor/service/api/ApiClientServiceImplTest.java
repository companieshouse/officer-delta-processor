package uk.gov.companieshouse.officer.delta.processor.service.api;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.appointment.ExternalData;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.delta.PrivateDeltaResourceHandler;
import uk.gov.companieshouse.api.handler.delta.company.appointment.request.PrivateOfficerDelete;
import uk.gov.companieshouse.api.handler.delta.company.appointment.request.PrivateOfficersUpsert;
import uk.gov.companieshouse.api.handler.delta.company.appointment.request.PrivateOfficersUpsertResourceHandler;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.officer.delta.processor.apiclient.InternalApiClientFactory;
import uk.gov.companieshouse.officer.delta.processor.apiclient.ResponseHandler;
import uk.gov.companieshouse.officer.delta.processor.model.DeleteAppointmentParameters;

@ExtendWith(MockitoExtension.class)
class ApiClientServiceImplTest {

    private static final String DELTA_AT = "20220925171003950844";
    private static final String APPOINTMENT_ID = "appointmentId";
    private static final String OFFICER_ID = "officerId";
    private static final String COMPANY_NUMBER = "12345678";

    @InjectMocks
    private ApiClientServiceImpl apiClientService;

    @Mock
    private InternalApiClientFactory internalApiClientFactory;
    @Mock
    private ResponseHandler responseHandler;

    @Mock
    private FullRecordCompanyOfficerApi appointment;
    @Mock
    private InternalApiClient client;
    @Mock
    private PrivateDeltaResourceHandler privateDeltaResourceHandler;
    @Mock
    private PrivateOfficersUpsertResourceHandler privateOfficersUpsertResourceHandler;
    @Mock
    private PrivateOfficersUpsert privateOfficersUpsert;
    @Mock
    private PrivateOfficerDelete privateOfficerDelete;

    @Test
    void putAppointment() {
        // given
        when(appointment.getExternalData()).thenReturn(new ExternalData().appointmentId(APPOINTMENT_ID));
        when(internalApiClientFactory.get()).thenReturn(client);
        when(client.privateDeltaCompanyAppointmentResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.putAppointment()).thenReturn(privateOfficersUpsertResourceHandler);
        when(privateOfficersUpsertResourceHandler.upsert(anyString(), any())).thenReturn(privateOfficersUpsert);

        final String expectedUri = String.format("/company/%s/appointments/%s/full_record", COMPANY_NUMBER,
                APPOINTMENT_ID);

        // when
        apiClientService.putAppointment(COMPANY_NUMBER, appointment);

        // then
        verify(privateOfficersUpsertResourceHandler).upsert(expectedUri, appointment);
        verifyNoInteractions(responseHandler);
    }

    @Test
    void shouldCallResponseHandlerWhenApiErrorResponseExceptionCaughtDuringPUT() throws Exception {
        // given
        when(appointment.getExternalData()).thenReturn(new ExternalData().appointmentId(APPOINTMENT_ID));
        when(internalApiClientFactory.get()).thenReturn(client);
        when(client.privateDeltaCompanyAppointmentResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.putAppointment()).thenReturn(privateOfficersUpsertResourceHandler);
        when(privateOfficersUpsertResourceHandler.upsert(anyString(), any())).thenReturn(privateOfficersUpsert);
        when(privateOfficersUpsert.execute()).thenThrow(ApiErrorResponseException.class);

        final String expectedUri = String.format("/company/%s/appointments/%s/full_record", COMPANY_NUMBER,
                APPOINTMENT_ID);
        // when
        apiClientService.putAppointment(COMPANY_NUMBER, appointment);

        // then
        verify(privateOfficersUpsertResourceHandler).upsert(expectedUri, appointment);
        verify(responseHandler).handle(any(ApiErrorResponseException.class));
        verify(responseHandler, times(0)).handle(any(URIValidationException.class));
    }

    @Test
    void shouldCallResponseHandlerWhenURIValidationExceptionCaughtDuringPUT() throws Exception {
        // given
        when(appointment.getExternalData()).thenReturn(new ExternalData().appointmentId(APPOINTMENT_ID));
        when(internalApiClientFactory.get()).thenReturn(client);
        when(client.privateDeltaCompanyAppointmentResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.putAppointment()).thenReturn(privateOfficersUpsertResourceHandler);
        when(privateOfficersUpsertResourceHandler.upsert(anyString(), any())).thenReturn(privateOfficersUpsert);
        when(privateOfficersUpsert.execute()).thenThrow(URIValidationException.class);

        final String expectedUri = String.format("/company/%s/appointments/%s/full_record", COMPANY_NUMBER,
                APPOINTMENT_ID);
        // when
        apiClientService.putAppointment(COMPANY_NUMBER, appointment);

        // then
        verify(privateOfficersUpsertResourceHandler).upsert(expectedUri, appointment);
        verify(responseHandler).handle(any(URIValidationException.class));
        verify(responseHandler, times(0)).handle(any(ApiErrorResponseException.class));
    }

    @Test
    void deleteOfficer() {
        // given
        when(internalApiClientFactory.get()).thenReturn(client);
        when(client.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.deleteOfficer(anyString(), anyString(), anyString())).thenReturn(
                privateOfficerDelete);

        DeleteAppointmentParameters deleteAppointmentParameters = DeleteAppointmentParameters.builder()
                .encodedOfficerId(OFFICER_ID)
                .deltaAt(DELTA_AT)
                .encodedInternalId(APPOINTMENT_ID)
                .companyNumber(COMPANY_NUMBER)
                .build();

        final String expectedUri = String.format("/company/%s/appointments/%s/full_record", COMPANY_NUMBER,
                APPOINTMENT_ID);

        // when
        apiClientService.deleteAppointment(deleteAppointmentParameters);

        // then
        verify(privateDeltaResourceHandler).deleteOfficer(expectedUri, DELTA_AT, OFFICER_ID);
        verifyNoInteractions(responseHandler);
    }

    @Test
    void shouldCallResponseHandlerWhenApiErrorResponseExceptionCaughtDuringDELETE() throws Exception {
        // given
        when(internalApiClientFactory.get()).thenReturn(client);
        when(client.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.deleteOfficer(anyString(), anyString(), anyString())).thenReturn(
                privateOfficerDelete);
        when(privateOfficerDelete.execute()).thenThrow(ApiErrorResponseException.class);

        DeleteAppointmentParameters deleteAppointmentParameters = DeleteAppointmentParameters.builder()
                .encodedOfficerId(OFFICER_ID)
                .deltaAt(DELTA_AT)
                .encodedInternalId(APPOINTMENT_ID)
                .companyNumber(COMPANY_NUMBER)
                .build();

        final String expectedUri = String.format("/company/%s/appointments/%s/full_record", COMPANY_NUMBER,
                APPOINTMENT_ID);

        // when
        apiClientService.deleteAppointment(deleteAppointmentParameters);

        // then
        verify(privateDeltaResourceHandler).deleteOfficer(expectedUri, DELTA_AT, OFFICER_ID);
        verify(responseHandler).handle(any(ApiErrorResponseException.class));
        verify(responseHandler, times(0)).handle(any(URIValidationException.class));
    }

    @Test
    void shouldCallResponseHandlerWhenURIValidationExceptionCaughtDuringDELETE() throws Exception {
        // given
        when(internalApiClientFactory.get()).thenReturn(client);
        when(client.privateDeltaResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.deleteOfficer(anyString(), anyString(), anyString())).thenReturn(
                privateOfficerDelete);
        when(privateOfficerDelete.execute()).thenThrow(URIValidationException.class);

        DeleteAppointmentParameters deleteAppointmentParameters = DeleteAppointmentParameters.builder()
                .encodedOfficerId(OFFICER_ID)
                .deltaAt(DELTA_AT)
                .encodedInternalId(APPOINTMENT_ID)
                .companyNumber(COMPANY_NUMBER)
                .build();

        final String expectedUri = String.format("/company/%s/appointments/%s/full_record", COMPANY_NUMBER,
                APPOINTMENT_ID);

        // when
        apiClientService.deleteAppointment(deleteAppointmentParameters);

        // then
        verify(privateDeltaResourceHandler).deleteOfficer(expectedUri, DELTA_AT, OFFICER_ID);
        verify(responseHandler).handle(any(URIValidationException.class));
        verifyNoMoreInteractions(responseHandler);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "''",
            "null"
    }, nullValues = "null")
    void shouldFailDeleteOfficerWhenMissingDeltaAt(final String deltaAt) {
        // given
        DeleteAppointmentParameters deleteAppointmentParameters = DeleteAppointmentParameters.builder()
                .encodedOfficerId(OFFICER_ID)
                .deltaAt(deltaAt)
                .encodedInternalId(APPOINTMENT_ID)
                .companyNumber(COMPANY_NUMBER)
                .build();

        // when
        Executable executable = () -> apiClientService.deleteAppointment(deleteAppointmentParameters);

        // then
        assertThrows(IllegalArgumentException.class, executable, "delta_at null or empty");
        verifyNoInteractions(internalApiClientFactory);
        verifyNoInteractions(responseHandler);
    }
}
