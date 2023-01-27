package uk.gov.companieshouse.officer.delta.processor.tranformer;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

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
import uk.gov.companieshouse.api.appointment.SensitiveData;
import uk.gov.companieshouse.api.appointment.UsualResidentialAddress;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.model.Identification;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;
import uk.gov.companieshouse.officer.delta.processor.model.enums.OfficerRole;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithDateOfBirth;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithResidentialAddress;

import java.util.Arrays;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class SensitiveOfficerTransformTest {
    public static final String VALID_DATE = "20000101";
    public static final String INVALID_DATE = "12345";
    public static final String KIND_OF_OFFICER_ROLE_WITH_DOB = OfficerRole.DIR.name();
    private SensitiveOfficerTransform testTransform;
    @Mock
    private AddressAPI addressAPI;
    @Mock
    private Identification identification;
    @Mock
    private UsualResidentialAddressTransform usualResidentialAddressTransform;
    @Mock
    private UsualResidentialAddress usualResidentialAddress;

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
        testTransform = new SensitiveOfficerTransform(usualResidentialAddressTransform);
    }

    @Test
    void factory() {
        assertThat(testTransform.factory(), is(instanceOf(SensitiveData.class)));
    }

    @Test
    void transformSingleWhenDateOfBirthInvalid() {
        final SensitiveData officerAPI = testTransform.factory();
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
        final SensitiveData officerAPI = testTransform.factory();
        final OfficersItem officer = createOfficer(addressAPI, identification);

        when(usualResidentialAddressTransform.transform(addressAPI)).thenReturn(usualResidentialAddress);

        officer.setUsualResidentialAddress(addressAPI);;
        officer.setAppointmentDate(VALID_DATE);
        officer.setDateOfBirth(VALID_DATE);

        final SensitiveData result = testTransform.transform(officer, officerAPI);

        assertThat(result.getUsualResidentialAddress(), is(usualResidentialAddress));
        if (RolesWithDateOfBirth.includes(officer.getOfficerRole())) {
            assertThat(result.getDateOfBirth().getDay(), is(1));
            assertThat(result.getDateOfBirth().getMonth(), is(1));
            assertThat(result.getDateOfBirth().getYear(), is(2000));
        }
        assertThat(result.getDateOfBirth().getDay(), is(1));
        assertThat(result.getDateOfBirth().getMonth(), is(1));
        assertThat(result.getDateOfBirth().getYear(), is(2000));
    }

    @DisplayName("Date of Birth is not included when the officers role does not require it")
    @ParameterizedTest
    @EnumSource
    void onlyRolesWithDobIncludeDateOfBirth(OfficerRole officerRole) throws NonRetryableErrorException {
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setDateOfBirth(VALID_DATE);
        officer.setKind(officerRole.name());
        officer.setAppointmentDate(VALID_DATE);

        final SensitiveData outputOfficer = testTransform.transform(officer);

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

        final SensitiveData outputOfficer = testTransform.transform(officer);

        assertThat(outputOfficer.getDateOfBirth(), is(nullValue()));
    }


    @DisplayName("URA and secure indicator is not included when the officers role does not require it")
    @ParameterizedTest
    @EnumSource
    void onlyRolesWithResidentialAddressIncludeAddressAndIndicators(OfficerRole officerRole)
            throws NonRetryableErrorException {
        final OfficersItem officer = createOfficer(addressAPI, identification);

        lenient().when(usualResidentialAddressTransform.transform(addressAPI)).thenReturn(usualResidentialAddress);

        officer.setDateOfBirth(VALID_DATE);
        officer.setKind(officerRole.name());
        officer.setAppointmentDate(VALID_DATE);

        final SensitiveData outputOfficer = testTransform.transform(officer);

        if (RolesWithResidentialAddress.includes(officerRole)) {
            assertThat(outputOfficer.getUsualResidentialAddress(), is(notNullValue()));
            assertThat(outputOfficer.getResidentialAddressSameAsServiceAddress(), is(true));
        } else {
            assertThat(outputOfficer.getUsualResidentialAddress(), is(nullValue()));
            assertThat(outputOfficer.getResidentialAddressSameAsServiceAddress(), is(nullValue()));
        }
    }

    private void verifyProcessingError(final SensitiveData officerAPI, final OfficersItem officer,
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
