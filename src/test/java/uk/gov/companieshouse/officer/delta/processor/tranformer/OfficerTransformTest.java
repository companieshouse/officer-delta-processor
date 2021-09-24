package uk.gov.companieshouse.officer.delta.processor.tranformer;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.DATETIME_LENGTH;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;
import uk.gov.companieshouse.api.model.delta.officers.IdentificationAPI;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.model.Identification;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;

import java.time.Instant;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class OfficerTransformTest {
    private static final String CHANGED_AT = "20210909133736012345";
    private static final Instant CHANGED_INSTANT = Instant.parse("2021-09-09T13:37:36.000Z");
    public static final String VALID_DATE = "20000101";
    public static final String INVALID_DATE = "12345";
    private static final Instant VALID_DATE_INSTANT = Instant.parse("2000-01-01T00:00:00Z");

    private OfficerTransform testTransform;

    @Mock
    private IdentificationTransform identificationTransform;
    @Mock
    private AddressAPI addressAPI;
    @Mock
    private Identification identification;
    @Mock
    private IdentificationAPI identificationAPI;
    @Mock
    private OfficerAPI officerAPI;

    @BeforeEach
    void setUp() {
        testTransform = new OfficerTransform(identificationTransform);
    }

    @Test
    void factory() {
        assertThat(testTransform.factory(), is(instanceOf(OfficerAPI.class)));
    }

    @Test
    void transformSingleWhenChangedAtInvalid() {
        final OfficerAPI officerAPI = testTransform.factory();
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setChangedAt(INVALID_DATE);

        verifyProcessingError(officerAPI, officer, "changedAt: date/time pattern not matched: [yyyyMMddHHmmss]");
    }

    @Test
    void transformSingleWhenAppointmentDateInvalid() {
        final OfficerAPI officerAPI = testTransform.factory();
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setChangedAt(CHANGED_AT);
        officer.setAppointmentDate(INVALID_DATE);

        verifyProcessingError(officerAPI, officer, "appointmentDate: date/time pattern not matched: [yyyyMMdd]");
    }

    @Test
    void transformSingleWhenResignationDateInvalid() {
        final OfficerAPI officerAPI = testTransform.factory();
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setChangedAt(CHANGED_AT);
        officer.setAppointmentDate("20000101");
        officer.setResignationDate(INVALID_DATE);

        verifyProcessingError(officerAPI, officer, "resignation_date: date/time pattern not matched: [yyyyMMdd]");
    }

    @Test
    void transformSingleWhenDateOfBirthInvalid() {
        final OfficerAPI officerAPI = testTransform.factory();
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setChangedAt(CHANGED_AT);
        officer.setAppointmentDate(VALID_DATE);
        officer.setAdditionalProperty("resignation_date", VALID_DATE);
        officer.setDateOfBirth(INVALID_DATE);

        verifyProcessingError(officerAPI, officer, "dateOfBirth: date/time pattern not matched: [yyyyMMdd]");
    }

    private static Stream<Arguments> provideScenarioParams() {
        return Stream.of(Arguments.of(CHANGED_AT, true),
                // changedAt full accuracy, resignation date present
                Arguments.of(CHANGED_AT.substring(0, DATETIME_LENGTH), false)
                // changedAt seconds accuracy, resignation date absent
        );
    }

    @ParameterizedTest(name = "{index}: changedAt={0}, has resignation_date={1}")
    @MethodSource("provideScenarioParams")
    void verifySuccessfulTransform(final String changedAt, final boolean hasResignationDate) {
        final OfficerAPI officerAPI = testTransform.factory();
        final OfficersItem officer = createOfficer(addressAPI, identification);

        when(identificationTransform.transform(identification)).thenReturn(identificationAPI);
        officer.setChangedAt(changedAt);
        officer.setAppointmentDate(VALID_DATE);
        officer.setDateOfBirth(VALID_DATE);
        if (hasResignationDate) {
            officer.setResignationDate(VALID_DATE);
        }

        final OfficerAPI result = testTransform.transform(officer, officerAPI);

        assertThat(result.getUpdatedAt(), is(CHANGED_INSTANT));
        assertThat(result.getAppointedOn(), is(VALID_DATE_INSTANT));
        assertThat(result.getResignedOn(), is(hasResignationDate ? VALID_DATE_INSTANT: null));
        assertThat(result.getDateOfBirth(), is(VALID_DATE_INSTANT));
        assertThat(result.getCompanyNumber(), is(officer.getCompanyNumber()));
        assertThat(result.getTitle(), is(officer.getTitle()));
        assertThat(result.getForename(), is(officer.getForename()));
        assertThat(result.getOtherForenames(), is(officer.getMiddleName()));
        assertThat(result.getSurname(), is(officer.getSurname()));
        assertThat(result.getNationality(), is(officer.getNationality()));
        assertThat(result.getOccupation(), is(officer.getOccupation()));
        assertThat(result.getOfficerRole(), is(officer.getOfficerRole()));
        assertThat(result.getHonours(), is(officer.getHonours()));
        assertThat(result.getServiceAddress(), is(sameInstance(addressAPI)));
        assertThat(result.isServiceAddressSameAsRegisteredOfficeAddress(), is(true));
        assertThat(result.getCountryOfResidence(), is(officer.getUsualResidentialCountry()));
        assertThat(result.getIdentificationData(), is(sameInstance(identificationAPI)));
    }

    private void verifyProcessingError(final OfficerAPI officerAPI, final OfficersItem officer,
            final String expectedMessage) {
        final NonRetryableErrorException exception =
                assertThrows(NonRetryableErrorException.class, () -> testTransform.transform(officer, officerAPI));

        assertThat(exception.getMessage(), is(expectedMessage));
        assertThat(exception.getCause(), is(nullValue()));
    }

    private OfficersItem createOfficer(final AddressAPI serviceAddress, final Identification identification) {
        final OfficersItem item = new OfficersItem();

        item.setCompanyNumber("companyNumber");
        item.setTitle("title");
        item.setForename("forename");
        item.setSurname("surname");
        item.setNationality("nationality");
        item.setOccupation("occupation");
        item.setDateOfBirth("dateOfBirth");
        item.setOfficerRole("officerRole");
        item.setHonours("honours");
        item.setServiceAddress(serviceAddress);
        item.setServiceAddressSameAsRegisteredAddress("Y");
        item.setUsualResidentialCountry("usualResidentialCountry");
        item.setIdentification(identification);

        return item;
    }

}
