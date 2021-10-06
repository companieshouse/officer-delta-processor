package uk.gov.companieshouse.officer.delta.processor.tranformer;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;

@ExtendWith(MockitoExtension.class)
class AppointmentTransformTest {
    private AppointmentTransform testTransform;

    @Mock
    private OfficerTransform officerTransform;
    @Mock
    private OfficerAPI officerAPI;

    @BeforeEach
    void setUp() {
        testTransform = new AppointmentTransform(officerTransform);
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

        when(officerTransform.transform(item)).thenReturn(officerAPI);

        final AppointmentAPI result = testTransform.transform(item, appointmentAPI);

        assertThat(result, is(sameInstance(appointmentAPI)));
        assertThat(appointmentAPI.getInternalId(), is("internalId"));
        assertThat(appointmentAPI.getId(), is("inamTI4b12taUuJyjgA72RNkYbs"));
        assertThat(appointmentAPI.getAppointmentId(), is("inamTI4b12taUuJyjgA72RNkYbs"));
        assertThat(appointmentAPI.getOfficerId(), is("6zmr-K93Jh_iDBMbWqRj3GuaQwQ"));
        assertThat(appointmentAPI.getPreviousOfficerId(), is("F_kqEbg83lQRIXkF6yUjxZ-wN9E"));
        verify(officerTransform).transform(item);
        assertThat(appointmentAPI.getData(), is(sameInstance(officerAPI)));
    }

    private OfficersItem createOfficer() {
        final OfficersItem item = new OfficersItem();

        item.setInternalId("internalId");
        item.setOfficerId("officerId");
        item.setPreviousOfficerId("previousOfficerId");

        return item;
    }
}
