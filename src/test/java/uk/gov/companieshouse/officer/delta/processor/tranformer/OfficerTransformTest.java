package uk.gov.companieshouse.officer.delta.processor.tranformer;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
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
import uk.gov.companieshouse.api.appointment.ContactDetails;
import uk.gov.companieshouse.api.appointment.Data;
import uk.gov.companieshouse.api.appointment.PrincipalOfficeAddress;
import uk.gov.companieshouse.api.appointment.ServiceAddress;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.model.DeltaIdentification;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;
import uk.gov.companieshouse.officer.delta.processor.model.PreviousNameArray;
import uk.gov.companieshouse.officer.delta.processor.model.enums.OfficerRole;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithCountryOfResidence;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithFormerNames;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithOccupation;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithPre1992Appointment;

import java.time.LocalDate;
import java.util.Collections;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class OfficerTransformTest {
    public static final String VALID_DATE = "20000101";
    public static final String INVALID_DATE = "12345";
    public static final String CORP_IND_Y = "Y";
    public static final String CORP_IND_N = "N";
    private static final String CHANGED_AT = "20210909133736012345";
    private static final LocalDate VALID_LOCAL_DATE = LocalDate.of(2000, 1,1);
    private OfficerTransform testTransform;

    @Mock
    private IdentificationTransform identificationTransform;
    @Mock
    private ServiceAddressTransform serviceAddressTransform;
    @Mock
    private FormerNameTransform formerNameTransform;
    @Mock
    private PrincipalOfficeAddressTransform principalOfficeAddressTransform;
    @Mock
    private AddressAPI addressAPI;
    @Mock
    private DeltaIdentification identification;
    @Mock
    private uk.gov.companieshouse.api.appointment.Identification identificationAPI;
    @Mock
    private ServiceAddress serviceAddress;
    @Mock
    private PrincipalOfficeAddress principalOfficeAddress;

    private static Stream<Arguments> provideScenarioParams() {
        return Stream.of(Arguments.of(CHANGED_AT, true),
                // changedAt full accuracy, resignation date present
                Arguments.of(CHANGED_AT.substring(0, DATETIME_LENGTH), false)
                // changedAt seconds accuracy, resignation date absent
        );
    }

    @BeforeEach
    void setUp() {
        testTransform = new OfficerTransform(identificationTransform, serviceAddressTransform, formerNameTransform, principalOfficeAddressTransform);
    }

    @Test
    void factory() {
        assertThat(testTransform.factory(), is(instanceOf(Data.class)));
    }

    @Test
    void transformSingleWhenAppointmentDateInvalid() {
        final Data data = testTransform.factory();
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setAppointmentDate(INVALID_DATE);
        officer.setDateOfBirth(VALID_DATE);
        officer.setKind(OfficerRole.DIR.name());

        verifyProcessingError(data, officer, "appointmentDate: date/time pattern not matched: [yyyyMMdd]");
    }

    @DisplayName("Identification (optional) is not transformed if not present")
    @Test
    void transformIdentificationWhenNotPresent() throws NonRetryableErrorException {
        final OfficersItem officer = createOfficer(addressAPI, null);

        officer.setAppointmentDate(VALID_DATE);
        officer.setDateOfBirth(VALID_DATE);

        testTransform.transform(officer);

        verifyNoInteractions(identificationTransform);
    }

    @Test
    void transformSingleWhenResignationDateInvalid() {
        final Data data = testTransform.factory();
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setAppointmentDate(VALID_DATE);
        officer.setResignationDate(INVALID_DATE);

        verifyProcessingError(data, officer, "resignation_date: date/time pattern not matched: [yyyyMMdd]");
    }

    @DisplayName("Occupation and Nationality is not included when the officers role does not require it")
    @ParameterizedTest
    @EnumSource
    void onlyRolesWithOccupationIncludeOccupationAndNationality(OfficerRole officerRole)
            throws NonRetryableErrorException {
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setDateOfBirth(VALID_DATE);
        officer.setKind(officerRole.name());
        officer.setAppointmentDate(VALID_DATE);
        officer.setOccupation("Super Hero");
        officer.setNationality("Krypton");

        final Data outputOfficer = testTransform.transform(officer);

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
    void onlyRolesWithCountryOfResidenceIncludeCountryOfResidence(OfficerRole officerRole)
            throws NonRetryableErrorException {
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setDateOfBirth(VALID_DATE);
        officer.setKind(officerRole.name());
        officer.setAppointmentDate(VALID_DATE);

        if (RolesWithCountryOfResidence.includes(officerRole)) {
            when(addressAPI.getUsualCountryOfResidence()).thenReturn("Wales");
        }

        final Data outputOfficer = testTransform.transform(officer);

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
    void onlyRolesWithPre1992AppointmentIncludePre1992Appointment(OfficerRole officerRole)
            throws NonRetryableErrorException {
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setDateOfBirth(VALID_DATE);
        officer.setKind(officerRole.name());
        officer.setAppointmentDate(VALID_DATE);
        officer.setApptDatePrefix("Y");

        final Data outputOfficer = testTransform.transform(officer);

        if (RolesWithPre1992Appointment.includes(officerRole)) {
            assertThat(outputOfficer.getIsPre1992Appointment(), is(true));
            assertThat(outputOfficer.getAppointedBefore(), is(VALID_LOCAL_DATE));
        } else {
            assertThat(outputOfficer.getIsPre1992Appointment(), is(false));
            assertThat(outputOfficer.getAppointedOn(), is(VALID_LOCAL_DATE));
        }
    }

    @DisplayName("Former Names is not included when the officers role does not require it")
    @ParameterizedTest
    @EnumSource
    void onlyRolesWithFormerNamesIncludeFormerNames(OfficerRole officerRole) throws NonRetryableErrorException {
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setDateOfBirth(VALID_DATE);
        officer.setKind(officerRole.name());
        officer.setAppointmentDate(VALID_DATE);
        officer.setPreviousNameArray(Collections.singletonList(
            new PreviousNameArray("forename", "surname")));

        final Data outputOfficer = testTransform.transform(officer);

        if (RolesWithFormerNames.includes(officerRole)) {
            assertThat(outputOfficer.getFormerNames(), is(notNullValue()));
        } else {
            assertThat(outputOfficer.getFormerNames(), is(nullValue()));
        }
    }

    @ParameterizedTest(name = "{index}: changedAt={0}, has resignation_date={1}")
    @MethodSource("provideScenarioParams")
    void verifySuccessfulTransform(final String changedAt, final boolean hasResignationDate)
            throws NonRetryableErrorException {
        final Data officerAPI = testTransform.factory();
        final OfficersItem officer = createOfficer(addressAPI, identification);

        when(identificationTransform.transform(identification)).thenReturn(identificationAPI);
        when(serviceAddressTransform.transform(addressAPI)).thenReturn(serviceAddress);

        officer.setAppointmentDate(VALID_DATE);
        officer.setDateOfBirth(VALID_DATE);
        if (hasResignationDate) {
            officer.setResignationDate(VALID_DATE);
        }

        final Data result = testTransform.transform(officer, officerAPI);

        assertThat(result.getAppointedOn(), is(VALID_LOCAL_DATE));
        assertThat(result.getResignedOn(), is(hasResignationDate ? VALID_LOCAL_DATE : null));
        assertThat(result.getOfficerRole(), is(Data.OfficerRoleEnum.fromValue(officer.getOfficerRole())));
        assertThat(result.getIsPre1992Appointment(), is(false));
        assertThat(result.getResignedOn(), is(hasResignationDate ? VALID_LOCAL_DATE : null));
        assertThat(result.getTitle(), is(officer.getTitle()));
        assertThat(result.getForename(), is(officer.getForename()));
        assertThat(result.getOtherForenames(), is(officer.getMiddleName()));
        assertThat(result.getSurname(), is(officer.getSurname()));
        assertThat(result.getNationality(), is(officer.getNationality()));
        assertThat(result.getOccupation(), is(officer.getOccupation()));
        assertThat(result.getHonours(), is(officer.getHonours()));
        assertThat(result.getPersonNumber(), is(officer.getExternalNumber()));
        assertThat(result.getServiceAddress(), is(sameInstance(serviceAddress)));
        assertThat(result.getServiceAddressSameAsRegisteredOfficeAddress(), is(true));
        assertThat(result.getIdentification(), is(sameInstance(identificationAPI)));
    }


    @Test
    void testCorporateManagingOfficerTransform() throws NonRetryableErrorException {
        Data officerAPI = testTransform.factory();
        OfficersItem officer = createOfficer(addressAPI, identification);
        ContactDetails contactDetails = new ContactDetails();
        AddressAPI principalOfficeAddressAPI = new AddressAPI();

        officer.setKind(OfficerRole.MANOFFCORP.name());
        officer.setPrincipalOfficeAddress(principalOfficeAddressAPI);
        officer.setContactDetails(contactDetails);
        officer.setResponsibilities("test");

        officer.setAppointmentDate(VALID_DATE);
        officer.setDateOfBirth(VALID_DATE);

        when(identificationTransform.transform(identification)).thenReturn(identificationAPI);
        when(principalOfficeAddressTransform.transform(principalOfficeAddressAPI)).thenReturn(principalOfficeAddress);

        final Data result = testTransform.transform(officer, officerAPI);

        assertThat(result.getPrincipalOfficeAddress(), is(principalOfficeAddress));
        assertThat(result.getContactDetails(), is(contactDetails));
        assertThat(result.getResponsibilities(), is("test"));
    }

    @Test
    void testManagingOfficerTransform() throws NonRetryableErrorException {
        Data officerAPI = testTransform.factory();
        OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setKind(OfficerRole.MANOFF.name());
        officer.setResponsibilities("test");

        officer.setAppointmentDate(VALID_DATE);
        officer.setDateOfBirth(VALID_DATE);

        when(identificationTransform.transform(identification)).thenReturn(identificationAPI);

        final Data result = testTransform.transform(officer, officerAPI);

        assertThat(result.getOfficerRole().getValue(), is("managing-officer"));
        assertThat(result.getResponsibilities(), is("test"));
    }

    @DisplayName("Verify data in the Links object is created as expected")
    @Test
    void verifyLinksData() throws NonRetryableErrorException {
        final Data officerAPI = testTransform.factory();
        final OfficersItem officer = createOfficer(addressAPI, identification);

        when(identificationTransform.transform(identification)).thenReturn(identificationAPI);

        officer.setAppointmentDate(VALID_DATE);
        officer.setDateOfBirth(VALID_DATE);

        final Data result = testTransform.transform(officer, officerAPI);

        assertThat(result.getLinks().get(0).getSelf(),
            is("/company/companyNumber/appointments/vuIAhYYbRDhqzx9b3e_jd6Uhres"));
        assertThat(result.getLinks().get(0).getOfficer().getSelf(),
            is("/officers/vuIAhYYbRDhqzx9b3e_jd6Uhres"));
        assertThat(result.getLinks().get(0).getOfficer().getAppointments(),
            is("/officers/vuIAhYYbRDhqzx9b3e_jd6Uhres/appointments"));
    }

    @Test
    void transformCorporateWhenKindIsDIRandCorpIndisY() {
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setAppointmentDate(VALID_DATE);
        officer.setDateOfBirth(VALID_DATE);
        officer.setCorporateInd(CORP_IND_Y);
        officer.setKind(OfficerRole.DIR.name());
        officer.setSurname("Corp Ltd");

        final Data outputOfficer = testTransform.transform(officer);

        assertThat(outputOfficer.getOfficerRole(), is(Data.OfficerRoleEnum.CORPORATE_DIRECTOR));
        assertThat(outputOfficer.getCompanyName(), is("Corp Ltd"));
    }

    @Test
    void transformNaturalWhenKindIsDIRandCorpIndisN() {
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setAppointmentDate(VALID_DATE);
        officer.setDateOfBirth(VALID_DATE);
        officer.setCorporateInd(CORP_IND_N);
        officer.setKind(OfficerRole.DIR.name());
        officer.setCompanyNumber("1111111");

        final Data outputOfficer = testTransform.transform(officer);

        assertThat(outputOfficer.getOfficerRole(), is(Data.OfficerRoleEnum.DIRECTOR));
    }

    @Test
    void transformNaturalWhenKindIsDIRandCorpIndisMissing() {
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setAppointmentDate(VALID_DATE);
        officer.setDateOfBirth(VALID_DATE);
        officer.setKind(OfficerRole.DIR.name());

        final Data outputOfficer = testTransform.transform(officer);

        assertThat(outputOfficer.getOfficerRole(), is(Data.OfficerRoleEnum.DIRECTOR));
    }

    @Test
    void transformCorporateWhenSpacesInKindName() {
        final OfficersItem officer = createOfficer(addressAPI, identification);

        officer.setAppointmentDate(VALID_DATE);
        officer.setDateOfBirth(VALID_DATE);
        officer.setCorporateInd(CORP_IND_Y);
        officer.setKind("D IR ");

        final Data outputOfficer = testTransform.transform(officer);

        assertThat(outputOfficer.getOfficerRole(), is(Data.OfficerRoleEnum.CORPORATE_DIRECTOR));
    }

    @Test
    void testTransformShouldHandleNullSource() {
        assertThat(testTransform.transform((OfficersItem) null), is(nullValue()));
    }

    private void verifyProcessingError(final Data data, final OfficersItem officer,
            final String expectedMessage) {

        final NonRetryableErrorException exception =
                assertThrows(NonRetryableErrorException.class, () -> testTransform.transform(officer, data));

        assertThat(exception.getMessage(), is(expectedMessage));
    }

    private OfficersItem createOfficer(final AddressAPI address, final DeltaIdentification identification) {
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
        item.setCorporateInd("N");
        item.setExternalNumber("1234567890");

        return item;
    }

}
