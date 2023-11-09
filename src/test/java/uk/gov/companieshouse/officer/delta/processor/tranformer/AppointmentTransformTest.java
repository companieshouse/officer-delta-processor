package uk.gov.companieshouse.officer.delta.processor.tranformer;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.api.appointment.Data;
import uk.gov.companieshouse.api.appointment.ExternalData;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.api.appointment.InternalData;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;
import uk.gov.companieshouse.officer.delta.processor.config.OfficerRoleConfig;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;

@ExtendWith(MockitoExtension.class)
class AppointmentTransformTest {
    private AppointmentTransform testTransform;
    private static final String CHANGED_AT = "20210909133736012345";

    @Mock
    private OfficerTransform officerTransform;
    @Mock
    private SensitiveOfficerTransform sensitiveOfficerTransform;
    @Mock
    private Data data;
    @Mock
    private ExternalData externalData;
    @Mock
    private InternalData internalData;
    @Mock
    private OfficerRoleConfig officerRoleConfig;

    @BeforeEach
    void setUp() {
        testTransform = new AppointmentTransform(officerTransform, sensitiveOfficerTransform, officerRoleConfig);
    }

    @Test
    void factory() {
        assertThat(testTransform.factory(), is(instanceOf(FullRecordCompanyOfficerApi.class)));
    }

    @Test
    void transformSingle() throws NonRetryableErrorException {
        final OfficersItem item = createOfficer();
        item.setCompanyStatus("Q");
        final FullRecordCompanyOfficerApi appointmentAPI = testTransform.factory();

        appointmentAPI.setExternalData(externalData);
        appointmentAPI.getExternalData().setAppointmentId("internalId");
        HashMap<String, Integer> nonResigned = new HashMap<>();
        nonResigned.put("secretary", 10);

        appointmentAPI.setInternalData(internalData);

        when(officerTransform.transform(item)).thenReturn(data);
        when(officerRoleConfig.getNonResigned()).thenReturn(nonResigned);
        when(data.getOfficerRole()).thenReturn(Data.OfficerRoleEnum.valueOf("SECRETARY"));

        final FullRecordCompanyOfficerApi result = testTransform.transform(item, appointmentAPI);

        assertThat(result, is(sameInstance(appointmentAPI)));
        assertThat(appointmentAPI.getExternalData().getInternalId(), is("internalId"));
        assertThat(appointmentAPI.getExternalData().getAppointmentId(), is("inamTI4b12taUuJyjgA72RNkYbs"));
        assertThat(appointmentAPI.getExternalData().getOfficerId(), is("6zmr-K93Jh_iDBMbWqRj3GuaQwQ"));
        assertThat(appointmentAPI.getExternalData().getPreviousOfficerId(), is("F_kqEbg83lQRIXkF6yUjxZ-wN9E"));
        assertThat(appointmentAPI.getExternalData().getCompanyNumber(), is("12345678"));
        //UpdatedAt is in LocalDate format, should it be in LocalDateTime?
        assertThat(appointmentAPI.getInternalData().getUpdatedAt(), is(LocalDate.of(2021, 9, 9)));
        verify(officerTransform).transform(item);
        assertThat(appointmentAPI.getExternalData().getData(), is(sameInstance(data)));
        assertThat(appointmentAPI.getInternalData().getOfficerRoleSortOrder(), is(10));
    }

    @Test
    void transformResignedSingle() throws NonRetryableErrorException {
        final OfficersItem item = createOfficer();
        item.setCompanyStatus("Q");
        item.setResignationDate("2020-01-01");
        final FullRecordCompanyOfficerApi appointmentAPI = testTransform.factory();
        appointmentAPI.setExternalData(externalData);
        appointmentAPI.getExternalData().setAppointmentId("internalId");
        HashMap<String, Integer> resigned = new HashMap<>();
        resigned.put("secretary", 100);

        when(officerTransform.transform(item)).thenReturn(data);
        when(officerRoleConfig.getResigned()).thenReturn(resigned);
        when(data.getOfficerRole()).thenReturn(Data.OfficerRoleEnum.valueOf("SECRETARY"));
        when(data.getResignedOn()).thenReturn(LocalDate.ofInstant(Instant.EPOCH, ZoneOffset.UTC));

        final FullRecordCompanyOfficerApi result = testTransform.transform(item, appointmentAPI);

        assertThat(result, is(sameInstance(appointmentAPI)));
        assertThat(appointmentAPI.getExternalData().getInternalId(), is("internalId"));
        verify(officerTransform).transform(item);
        assertThat(appointmentAPI.getExternalData().getData(), is(sameInstance(data)));
        assertThat(appointmentAPI.getInternalData().getUpdatedAt(), is(LocalDate.of(2021, 9, 9)));
        assertThat(appointmentAPI.getInternalData().getOfficerRoleSortOrder(), is(100));
    }

    @Test
    void testTransformShouldHandleNullSource() {
        assertThat(testTransform.transform((OfficersItem) null), is(nullValue()));
    }

    private OfficersItem createOfficer() {
        final OfficersItem item = new OfficersItem();

        item.setInternalId("internalId");
        item.setOfficerId("officerId");
        item.setPreviousOfficerId("previousOfficerId");
        item.setCompanyNumber("12345678");
        item.setChangedAt(CHANGED_AT);

        return item;
    }
}
