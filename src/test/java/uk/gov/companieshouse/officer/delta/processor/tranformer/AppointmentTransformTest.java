package uk.gov.companieshouse.officer.delta.processor.tranformer;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.officer.delta.processor.config.OfficerRoleConfig;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;

@ExtendWith(MockitoExtension.class)
class AppointmentTransformTest {
    private AppointmentTransform testTransform;

    @Mock
    private OfficerTransform officerTransform;
    @Mock
    private SensitiveOfficerTransform sensitiveOfficerTransform;
    @Mock
    private OfficerAPI officerAPI;
    @Mock
    private OfficerRoleConfig officerRoleConfig;

    @BeforeEach
    void setUp() {
        testTransform = new AppointmentTransform(officerTransform, sensitiveOfficerTransform, officerRoleConfig);
    }

    @Test
    void factory() {
        assertThat(testTransform.factory(), is(instanceOf(AppointmentAPI.class)));
    }

    @Test
    void transformSingle() throws NonRetryableErrorException {
        final OfficersItem item = createOfficer();
        final AppointmentAPI appointmentAPI = testTransform.factory();
        appointmentAPI.setId("internalId");
        appointmentAPI.setAppointmentId("internalId");
        HashMap<String, Integer> nonResigned = new HashMap<>();
        nonResigned.put("secretary", 10);

        when(officerTransform.transform(item)).thenReturn(officerAPI);
        when(officerRoleConfig.getNonResigned()).thenReturn(nonResigned);
        when(officerAPI.getOfficerRole()).thenReturn("secretary");

        final AppointmentAPI result = testTransform.transform(item, appointmentAPI);

        assertThat(result, is(sameInstance(appointmentAPI)));
        assertThat(appointmentAPI.getInternalId(), is("internalId"));
        assertThat(appointmentAPI.getId(), is("inamTI4b12taUuJyjgA72RNkYbs"));
        assertThat(appointmentAPI.getAppointmentId(), is("inamTI4b12taUuJyjgA72RNkYbs"));
        assertThat(appointmentAPI.getOfficerId(), is("6zmr-K93Jh_iDBMbWqRj3GuaQwQ"));
        assertThat(appointmentAPI.getPreviousOfficerId(), is("F_kqEbg83lQRIXkF6yUjxZ-wN9E"));
        assertThat(appointmentAPI.getCompanyNumber(), is("12345678"));
        verify(officerTransform).transform(item);
        assertThat(appointmentAPI.getData(), is(sameInstance(officerAPI)));
        assertThat(appointmentAPI.getOfficerRoleSortOrder(), is(10));
    }

    @Test
    void transformResignedSingle() throws NonRetryableErrorException {
        final OfficersItem item = createOfficer();
        item.setResignationDate("2020-01-01");
        final AppointmentAPI appointmentAPI = testTransform.factory();
        appointmentAPI.setId("internalId");
        appointmentAPI.setAppointmentId("internalId");
        HashMap<String, Integer> resigned = new HashMap<>();
        resigned.put("secretary", 100);

        when(officerTransform.transform(item)).thenReturn(officerAPI);
        when(officerRoleConfig.getResigned()).thenReturn(resigned);
        when(officerAPI.getOfficerRole()).thenReturn("secretary");
        when(officerAPI.getResignedOn()).thenReturn(Instant.EPOCH);

        final AppointmentAPI result = testTransform.transform(item, appointmentAPI);

        assertThat(result, is(sameInstance(appointmentAPI)));
        assertThat(appointmentAPI.getInternalId(), is("internalId"));
        assertThat(appointmentAPI.getId(), is("inamTI4b12taUuJyjgA72RNkYbs"));
        verify(officerTransform).transform(item);
        assertThat(appointmentAPI.getData(), is(sameInstance(officerAPI)));
        assertThat(appointmentAPI.getOfficerRoleSortOrder(), is(100));
    }

    private OfficersItem createOfficer() {
        final OfficersItem item = new OfficersItem();

        item.setInternalId("internalId");
        item.setOfficerId("officerId");
        item.setPreviousOfficerId("previousOfficerId");
        item.setCompanyNumber("12345678");

        return item;
    }
}
