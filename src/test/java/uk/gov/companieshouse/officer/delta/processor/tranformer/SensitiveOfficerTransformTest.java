package uk.gov.companieshouse.officer.delta.processor.tranformer;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import uk.gov.companieshouse.api.model.delta.officers.SensitiveOfficerAPI;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.model.Identification;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;
import uk.gov.companieshouse.officer.delta.processor.model.enums.OfficerRole;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithDateOfBirth;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithResidentialAddress;

import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class SensitiveOfficerTransformTest {
    public static final String VALID_DATE = "20000101";
    public static final String INVALID_DATE = "12345";
    public static final String KIND_OF_OFFICER_ROLE_WITH_DOB = OfficerRole.DIR.name();
    private static final Instant VALID_DATE_INSTANT = Instant.parse("2000-01-01T00:00:00Z");
    private SensitiveOfficerTransform testTransform;
    @Mock
    private AddressAPI addressAPI;
    @Mock
    private Identification identification;
    @Mock
    private SensitiveOfficerAPI officerAPI;

    private static Stream<Arguments> emptyDobsWithDobRoles() {
        Stream<OfficerRole> requiresDob = Arrays.stream(RolesWithDateOfBirth.values())
                .map(RolesWithDateOfBirth::getOfficerRole);

        return requiresDob.flatMap(role -> Stream.of(
                Arguments.of(role, null),
                Arguments.of(role, "")
        ));
    }

    @BeforeEach
    void setUp() {
        testTransform = new SensitiveOfficerTransform();
    }

    @Test
    void factory() {
        assertThat(testTransform.factory(), is(instanceOf(SensitiveOfficerAPI.class)));
    }

    @Test
    void transformSingleWhenDateOfBirthInvalid() {
        final SensitiveOfficerAPI officerAPI = testTransform.factory();
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setAppointmentDate(VALID_DATE);
        officer.setAdditionalProperty("resignation_date", VALID_DATE);
        officer.setDateOfBirth(INVALID_DATE);
        officer.setOfficerRole(KIND_OF_OFFICER_ROLE_WITH_DOB);

        verifyProcessingError(officerAPI, officer, "dateOfBirth: date/time pattern not matched: [yyyyMMdd]");
    }

    @Test
    void verifySuccessfulTransform()
            throws NonRetryableErrorException {
        final SensitiveOfficerAPI officerAPI = testTransform.factory();
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setUsualResidentialAddress(addressAPI);;
        officer.setAppointmentDate(VALID_DATE);
        officer.setDateOfBirth(VALID_DATE);

        final SensitiveOfficerAPI result = testTransform.transform(officer, officerAPI);

        assertThat(result.getUsualResidentialAddress(), is(addressAPI));
        if (RolesWithDateOfBirth.includes(officer.getOfficerRole())) {
            assertThat(result.getDateOfBirth(), is(VALID_DATE_INSTANT));
        }
        assertThat(result.getDateOfBirth(), is(VALID_DATE_INSTANT));
    }

    @DisplayName("Date of Birth is not included when the officers role does not require it")
    @ParameterizedTest
    @EnumSource
    void onlyRolesWithDobIncludeDateOfBirth(OfficerRole officerRole) throws NonRetryableErrorException {
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setDateOfBirth(VALID_DATE);
        officer.setKind(officerRole.name());
        officer.setAppointmentDate(VALID_DATE);

        final SensitiveOfficerAPI outputOfficer = testTransform.transform(officer);

        if (RolesWithDateOfBirth.includes(officerRole)) {
            assertThat(outputOfficer.getDateOfBirth(), is(notNullValue()));
        } else {
            assertThat(outputOfficer.getDateOfBirth(), is(nullValue()));
        }
    }

    @DisplayName("Transformation doesn't fail when no DOB on role which requires it")
    @ParameterizedTest
    @MethodSource("emptyDobsWithDobRoles")
    void transformsWhenNoDob(OfficerRole role, String dob) throws NonRetryableErrorException {
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setDateOfBirth(dob);
        officer.setKind(role.name());
        officer.setAppointmentDate(VALID_DATE);

        final SensitiveOfficerAPI outputOfficer = testTransform.transform(officer);

        assertThat(outputOfficer.getDateOfBirth(), is(nullValue()));
    }


    @DisplayName("URA and secure indicator is not included when the officers role does not require it")
    @ParameterizedTest
    @EnumSource
    void onlyRolesWithResidentialAddressIncludeAddressAndIndicators(OfficerRole officerRole)
            throws NonRetryableErrorException {
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setDateOfBirth(VALID_DATE);
        officer.setKind(officerRole.name());
        officer.setAppointmentDate(VALID_DATE);

        final SensitiveOfficerAPI outputOfficer = testTransform.transform(officer);

        if (RolesWithResidentialAddress.includes(officerRole)) {
            assertThat(outputOfficer.getUsualResidentialAddress(), is(notNullValue()));
            assertThat(outputOfficer.isResidentialAddressSameAsServiceAddress(), is(true));
        } else {
            assertThat(outputOfficer.getUsualResidentialAddress(), is(nullValue()));
            assertThat(outputOfficer.isResidentialAddressSameAsServiceAddress(), is(nullValue()));
        }
    }

    private void verifyProcessingError(final SensitiveOfficerAPI officerAPI, final OfficersItem officer,
            final String expectedMessage) {
        final NonRetryableErrorException exception =
                assertThrows(NonRetryableErrorException.class, () -> testTransform.transform(officer, officerAPI));

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
