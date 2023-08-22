package uk.gov.companieshouse.officer.delta.processor.service.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.appointment.ExternalData;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.api.handler.delta.company.appointment.request.PrivateOfficerDelete;
import uk.gov.companieshouse.api.handler.delta.company.appointment.request.PrivateOfficersUpsert;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class ApiClientServiceImplTest {

    @Mock
    Logger logger;

    private ApiClientServiceImpl apiClientService;

    @BeforeEach
    void setup() {
        apiClientService = new ApiClientServiceImpl(logger);
        ReflectionTestUtils.setField(apiClientService, "chsApiKey", "apiKey");
        ReflectionTestUtils.setField(apiClientService, "apiUrl", "https://api.companieshouse.gov.uk");
    }

    @Test
    void putAppointment() {
        final ApiResponse<Void> expectedResponse = new ApiResponse<>(HttpStatus.OK.value(), null, null);
        ApiClientServiceImpl apiClientServiceSpy = Mockito.spy(apiClientService);
        doReturn(expectedResponse).when(apiClientServiceSpy).executeOp(anyString(), anyString(),
                anyString(),
                any(PrivateOfficersUpsert.class));
        var appointmentApi = new FullRecordCompanyOfficerApi();
        ExternalData externalData = new ExternalData();
        appointmentApi.setExternalData(externalData);
        appointmentApi.getExternalData().setAppointmentId("3102598777");
        ApiResponse<Void> response = apiClientServiceSpy.putAppointment("context_id",
                "09876543",
                appointmentApi);
        verify(apiClientServiceSpy).executeOp(anyString(), eq("putCompanyAppointment"),
                eq("/company/09876543/appointments/3102598777/full_record"),
                any(PrivateOfficersUpsert.class));

        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void deleteDisqualification() {
        final ApiResponse<Void> expectedResponse = new ApiResponse<>(HttpStatus.OK.value(), null, null);
        ApiClientServiceImpl apiClientServiceSpy = Mockito.spy(apiClientService);
        doReturn(expectedResponse).when(apiClientServiceSpy).executeOp(anyString(), anyString(),
                anyString(),
                any(PrivateOfficerDelete.class));

        ApiResponse<Void> response = apiClientServiceSpy.deleteAppointment("context_id",
                "N-YqKNwdT_HvetusfTJ0H0jAQbA", "09876543");
        verify(apiClientServiceSpy).executeOp(anyString(), eq("deleteOfficer"),
                eq("/company/09876543/appointments/N-YqKNwdT_HvetusfTJ0H0jAQbA/full_record/delete"),
                any(PrivateOfficerDelete.class));

        assertThat(response).isEqualTo(expectedResponse);
    }
}



