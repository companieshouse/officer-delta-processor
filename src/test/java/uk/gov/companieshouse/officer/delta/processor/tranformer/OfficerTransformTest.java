package uk.gov.companieshouse.officer.delta.processor.tranformer;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.DATETIME_LENGTH;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;
import uk.gov.companieshouse.api.model.delta.officers.IdentificationAPI;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.officer.delta.processor.exception.ProcessException;
import uk.gov.companieshouse.officer.delta.processor.model.Identification;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;
import uk.gov.companieshouse.officer.delta.processor.model.PreviousNameArray;
import uk.gov.companieshouse.officer.delta.processor.model.enums.OfficerRole;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithCountryOfResidence;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithDateOfBirth;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithFormerNames;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithOccupation;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithPre1992Appointment;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithResidentialAddress;

@ExtendWith(MockitoExtension.class)
class OfficerTransformTest {
    public static final String VALID_DATE = "20000101";
    public static final String INVALID_DATE = "12345";
    public static final String KIND_OF_OFFICER_ROLE_WITH_DOB = OfficerRole.DIR.name();
    private static final String CHANGED_AT = "20210909133736012345";
    private static final Instant CHANGED_INSTANT = Instant.parse("2021-09-09T13:37:36.000Z");
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

    private static Stream<Arguments> emptyDobsWithDobRoles() {
        Stream<OfficerRole> requiresDob = Arrays.stream(RolesWithDateOfBirth.values())
                .map(RolesWithDateOfBirth::getOfficerRole);

        return requiresDob.flatMap(role -> Stream.of(
                Arguments.of(role, null),
                Arguments.of(role, "")
        ));
    }

    private static Stream<Arguments> provideScenarioParams() {
        return Stream.of(Arguments.of(CHANGED_AT, true),
                // changedAt full accuracy, resignation date present
                Arguments.of(CHANGED_AT.substring(0, DATETIME_LENGTH), false)
                // changedAt seconds accuracy, resignation date absent
        );
    }

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

        verifyProcessException(officerAPI, officer, "changedAt: date/time pattern not matched: [yyyyMMddHHmmss]");
    }

    @Test
    void transformSingleWhenAppointmentDateInvalid() {
        final OfficerAPI officerAPI = testTransform.factory();
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setChangedAt(CHANGED_AT);
        officer.setAppointmentDate(INVALID_DATE);
        officer.setDateOfBirth(VALID_DATE);
        officer.setKind(OfficerRole.DIR.name());

        verifyProcessException(officerAPI, officer, "appointmentDate: date/time pattern not matched: [yyyyMMdd]");
    }

    @Test
    void transformSingleWhenResignationDateInvalid() {
        final OfficerAPI officerAPI = testTransform.factory();
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setChangedAt(CHANGED_AT);
        officer.setAppointmentDate("20000101");
        officer.setResignationDate(INVALID_DATE);

        verifyProcessException(officerAPI, officer, "resignation_date: date/time pattern not matched: [yyyyMMdd]");
    }

    @Test
    void transformSingleWhenDateOfBirthInvalid() {
        final OfficerAPI officerAPI = testTransform.factory();
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setChangedAt(CHANGED_AT);
        officer.setAppointmentDate(VALID_DATE);
        officer.setAdditionalProperty("resignation_date", VALID_DATE);
        officer.setDateOfBirth(INVALID_DATE);
        officer.setOfficerRole(KIND_OF_OFFICER_ROLE_WITH_DOB);

        verifyProcessException(officerAPI, officer, "dateOfBirth: date/time pattern not matched: [yyyyMMdd]");
    }

    @DisplayName("Date of Birth is not included when the officers role does not require it")
    @ParameterizedTest
    @EnumSource
    void onlyRolesWithDobIncludeDateOfBirth(OfficerRole officerRole) throws ProcessException {
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setDateOfBirth(VALID_DATE);
        officer.setKind(officerRole.name());
        officer.setChangedAt(CHANGED_AT);
        officer.setAppointmentDate(VALID_DATE);

        final OfficerAPI outputOfficer = testTransform.transform(officer);

        if (RolesWithDateOfBirth.includes(officerRole)) {
            assertThat(outputOfficer.getDateOfBirth(), is(notNullValue()));
        } else {
            assertThat(outputOfficer.getDateOfBirth(), is(nullValue()));
        }
    }

    @DisplayName("Transformation doesn't fail when no DOB on role which requires it")
    @ParameterizedTest
    @MethodSource("emptyDobsWithDobRoles")
    void transformsWhenNoDob(OfficerRole role, String dob) throws ProcessException {
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setDateOfBirth(dob);
        officer.setKind(role.name());
        officer.setChangedAt(CHANGED_AT);
        officer.setAppointmentDate(VALID_DATE);

        final OfficerAPI outputOfficer = testTransform.transform(officer);

        assertThat(outputOfficer.getDateOfBirth(), is(nullValue()));
    }

    @DisplayName("Occupation and Nationality is not included when the officers role does not require it")
    @ParameterizedTest
    @EnumSource
    void onlyRolesWithOccupationIncludeOccupationAndNationality(OfficerRole officerRole) throws ProcessException {
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setDateOfBirth(VALID_DATE);
        officer.setKind(officerRole.name());
        officer.setChangedAt(CHANGED_AT);
        officer.setAppointmentDate(VALID_DATE);
        officer.setOccupation("Super Hero");
        officer.setNationality("Krypton");

        final OfficerAPI outputOfficer = testTransform.transform(officer);

        if (RolesWithOccupation.includes(officerRole)) {
            assertThat(outputOfficer.getOccupation(), is(notNullValue()));
            assertThat(outputOfficer.getNationality(), is(notNullValue()));
        } else {
            assertThat(outputOfficer.getOccupation(), is(nullValue()));
            assertThat(outputOfficer.getNationality(), is(nullValue()));
        }
    }

    @DisplayName("Country of Residence is not included when the officers role does not require it")
    @ParameterizedTest
    @EnumSource
    void onlyRolesWithCountryOfResidenceIncludeCountryOfResidence(OfficerRole officerRole) throws ProcessException {
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setDateOfBirth(VALID_DATE);
        officer.setKind(officerRole.name());
        officer.setChangedAt(CHANGED_AT);
        officer.setAppointmentDate(VALID_DATE);

        if (RolesWithCountryOfResidence.includes(officerRole)) {
            when(addressAPI.getUsualCountryOfResidence()).thenReturn("Wales");
        }

        final OfficerAPI outputOfficer = testTransform.transform(officer);

        if (RolesWithCountryOfResidence.includes(officerRole)) {
            assertThat(outputOfficer.getCountryOfResidence(), is("Wales"));
        } else {
            assertThat(outputOfficer.getCountryOfResidence(), is(nullValue()));
        }
    }

    @DisplayName("Set AppointedOn to AppointedBefore when the OfficerRole is included in the roleSet " +
            "and is Pre1992Appointment")
    @ParameterizedTest
    @EnumSource
    void onlyRolesWithPre1992AppointmentIncludePre1992Appointment(OfficerRole officerRole) throws ProcessException {
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setDateOfBirth(VALID_DATE);
        officer.setKind(officerRole.name());
        officer.setChangedAt(CHANGED_AT);
        officer.setAppointmentDate(VALID_DATE);
        officer.setApptDatePrefix("Y");

        final OfficerAPI outputOfficer = testTransform.transform(officer);

        if (RolesWithPre1992Appointment.includes(officerRole)) {
            assertThat(outputOfficer.isPre1992Appointment(), is(true));
            assertThat(outputOfficer.getAppointedBefore(), is(VALID_DATE_INSTANT));
        } else {
            assertThat(outputOfficer.isPre1992Appointment(), is(false));
            assertThat(outputOfficer.getAppointedOn(), is(VALID_DATE_INSTANT));
        }
    }

    @DisplayName("Former Names is not included when the officers role does not require it")
    @ParameterizedTest
    @EnumSource
    void onlyRolesWithFormerNamesIncludeFormerNames(OfficerRole officerRole) throws ProcessException {
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setDateOfBirth(VALID_DATE);
        officer.setKind(officerRole.name());
        officer.setChangedAt(CHANGED_AT);
        officer.setAppointmentDate(VALID_DATE);
        officer.setPreviousNameArray(Collections.singletonList(
            new PreviousNameArray("forename", "surname")));

        final OfficerAPI outputOfficer = testTransform.transform(officer);

        if (RolesWithFormerNames.includes(officerRole)) {
            assertThat(outputOfficer.getFormerNameData(), is(notNullValue()));
        } else {
            assertThat(outputOfficer.getFormerNameData(), is(nullValue()));
        }
    }


    @DisplayName("URA and secure indicator is not included when the officers role does not require it")
    @ParameterizedTest
    @EnumSource
    void onlyRolesWithResidentialAddressIncludeAddressAndIndicators(OfficerRole officerRole) throws ProcessException {
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setDateOfBirth(VALID_DATE);
        officer.setKind(officerRole.name());
        officer.setChangedAt(CHANGED_AT);
        officer.setAppointmentDate(VALID_DATE);

        final OfficerAPI outputOfficer = testTransform.transform(officer);

        if (RolesWithResidentialAddress.includes(officerRole)) {
            assertThat(outputOfficer.getUsualResidentialAddress(), is(notNullValue()));
            assertThat(outputOfficer.isResidentialAddressSameAsServiceAddress(), is(notNullValue()));
            assertThat(outputOfficer.secureOfficer(), is(notNullValue()));
        } else {
            assertThat(outputOfficer.getUsualResidentialAddress(), is(nullValue()));
            assertThat(outputOfficer.isResidentialAddressSameAsServiceAddress(), is(nullValue()));
            assertThat(outputOfficer.secureOfficer(), is(nullValue()));
        }
    }

    @ParameterizedTest(name = "{index}: changedAt={0}, has resignation_date={1}")
    @MethodSource("provideScenarioParams")
    void verifySuccessfulTransform(final String changedAt, final boolean hasResignationDate) throws ProcessException {
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
        assertThat(result.getResignedOn(), is(hasResignationDate ? VALID_DATE_INSTANT : null));
        if (RolesWithDateOfBirth.includes(result.getOfficerRole())) {
            assertThat(result.getDateOfBirth(), is(VALID_DATE_INSTANT));
        }
        assertThat(result.getOfficerRole(), is(officer.getOfficerRole()));
        assertThat(result.isPre1992Appointment(), is(false));
        assertThat(result.getResignedOn(), is(hasResignationDate ? VALID_DATE_INSTANT : null));
        assertThat(result.getDateOfBirth(), is(VALID_DATE_INSTANT));
        assertThat(result.getCompanyNumber(), is(officer.getCompanyNumber()));
        assertThat(result.getTitle(), is(officer.getTitle()));
        assertThat(result.getForename(), is(officer.getForename()));
        assertThat(result.getOtherForenames(), is(officer.getMiddleName()));
        assertThat(result.getSurname(), is(officer.getSurname()));
        assertThat(result.getNationality(), is(officer.getNationality()));
        assertThat(result.getOccupation(), is(officer.getOccupation()));
        assertThat(result.getHonours(), is(officer.getHonours()));
        assertThat(result.getServiceAddress(), is(sameInstance(addressAPI)));
        assertThat(result.isServiceAddressSameAsRegisteredOfficeAddress(), is(true));
        assertThat(result.getIdentificationData(), is(sameInstance(identificationAPI)));
    }

    private void verifyProcessException(final OfficerAPI officerAPI, final OfficersItem officer,
                                        final String expectedMessage) {
        final ProcessException exception =
                assertThrows(ProcessException.class, () -> testTransform.transform(officer, officerAPI));

        assertThat(exception.getMessage(), is(expectedMessage));
        assertThat(exception.getCause(), is(nullValue()));
    }

    private OfficersItem createOfficer(final AddressAPI address, final Identification identification) {
        final OfficersItem item = new OfficersItem();

        item.setCompanyNumber("companyNumber");
        item.setTitle("title");
        item.setForename("forename");
        item.setSurname("surname");
        item.setNationality("nationality");
        item.setOccupation("occupation");
        item.setDateOfBirth("dateOfBirth");
        item.setKind(OfficerRole.DIR.name());
        item.setSecureDirector("N");
        item.setOfficerRole(OfficerRole.DIR.getValue());
        item.setApptDatePrefix("N");
        item.setHonours("honours");
        item.setServiceAddress(address);
        item.setUsualResidentialAddress(address);
        item.setServiceAddressSameAsRegisteredAddress("Y");
        item.setResidentialAddressSameAsServiceAddress("Y");
        item.setIdentification(identification);

        return item;
    }

}
