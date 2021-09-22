package uk.gov.companieshouse.officer.delta.processor.model;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;

@ExtendWith(MockitoExtension.class)
class OfficersItemTest {
    private static final String EXPECTED = "expected";

    private OfficersItem testItem;

    @BeforeEach
    void setUp() {
        testItem = new OfficersItem();
    }

    @Test
    void setAdditionalProperty() {
        testItem.setAdditionalProperty("additional", "property");

        assertThat(testItem.getAdditionalProperties(), hasEntry("additional", "property"));
    }

    @Test
    void setOccupation() {
        testItem.setOccupation(EXPECTED);
        assertThat(testItem.getOccupation(), is(EXPECTED));
    }

    @Test
    void setOfficerRole() {
        testItem.setOfficerRole(EXPECTED);
        assertThat(testItem.getOfficerRole(), is(EXPECTED));
    }

    @Test
    void setInternalId() {
        testItem.setInternalId(EXPECTED);
        assertThat(testItem.getInternalId(), is(EXPECTED));
    }

    @Test
    void setKind() {
        testItem.setKind(EXPECTED);
        assertThat(testItem.getKind(), is(EXPECTED));
    }

    @Test
    void setDateOfBirth() {
        testItem.setDateOfBirth(EXPECTED);
        assertThat(testItem.getDateOfBirth(), is(EXPECTED));
    }

    @Test
    void setServiceAddressSameAsRegisteredAddress() {
        testItem.setServiceAddressSameAsRegisteredAddress(EXPECTED);
        assertThat(testItem.getServiceAddressSameAsRegisteredAddress(), is(EXPECTED));
    }

    @Test
    void setAppointmentDate() {
        testItem.setAppointmentDate(EXPECTED);
        assertThat(testItem.getAppointmentDate(), is(EXPECTED));
    }

    @Test
    void setApptDatePrefix() {
        testItem.setApptDatePrefix(EXPECTED);
        assertThat(testItem.getApptDatePrefix(), is(EXPECTED));
    }

    @Test
    void getResignationDate() {
        testItem.setResignationDate(EXPECTED);
        assertThat(testItem.getResignationDate(), is(EXPECTED));
    }

    @Test
    void setOfficerDetailId() {
        testItem.setOfficerDetailId(EXPECTED);
        assertThat(testItem.getOfficerDetailId(), is(EXPECTED));
    }

    @Test
    void setChangedAt() {
        testItem.setChangedAt(EXPECTED);
        assertThat(testItem.getChangedAt(), is(EXPECTED));
    }

    @Test
    void setTitle() {
        testItem.setTitle(EXPECTED);
        assertThat(testItem.getTitle(), is(EXPECTED));
    }

    @Test
    void setHonours() {
        testItem.setHonours(EXPECTED);
        assertThat(testItem.getHonours(), is(EXPECTED));
    }

    @Test
    void setMiddleName() {
        testItem.setMiddleName(EXPECTED);
        assertThat(testItem.getMiddleName(), is(EXPECTED));
    }

    @Test
    void setCorporateInd() {
        testItem.setCorporateInd(EXPECTED);
        assertThat(testItem.getCorporateInd(), is(EXPECTED));
    }

    @Test
    void setServiceAddress() {
        final AddressAPI expected = new AddressAPI();

        testItem.setServiceAddress(expected);

        assertThat(testItem.getServiceAddress(), is(sameInstance(expected)));
    }

    @Test
    void setUsualResidentialAddress() {
        final AddressAPI expected = new AddressAPI();

        testItem.setUsualResidentialAddress(expected);

        assertThat(testItem.getUsualResidentialAddress(), is(sameInstance(expected)));
    }

    @Test
    void setForename() {
        testItem.setForename(EXPECTED);
        assertThat(testItem.getForename(), is(EXPECTED));
    }

    @Test
    void setCompanyNumber() {
        testItem.setCompanyNumber(EXPECTED);
        assertThat(testItem.getCompanyNumber(), is(EXPECTED));
    }

    @Test
    void setOfficerId() {
        testItem.setOfficerId(EXPECTED);
        assertThat(testItem.getOfficerId(), is(EXPECTED));
    }

    @Test
    void setUsualResidentialCountry() {
        testItem.setUsualResidentialCountry(EXPECTED);
        assertThat(testItem.getUsualResidentialCountry(), is(EXPECTED));
    }

    @Test
    void setIdentification() {
        final Identification expected = new Identification();

        testItem.setIdentification(expected);

        assertThat(testItem.getIdentification(), is(sameInstance(expected)));
    }

    @Test
    void setNationality() {
        testItem.setNationality(EXPECTED);
        assertThat(testItem.getNationality(), is(EXPECTED));
    }

    @Test
    void setSurname() {
        testItem.setSurname(EXPECTED);
        assertThat(testItem.getSurname(), is(EXPECTED));
    }

    @Test
    void setSecureDirector() {
        testItem.setSecureDirector(EXPECTED);
        assertThat(testItem.getSecureDirector(), is(EXPECTED));
    }

    @Test
    void setPreviousNameArray() {
        final PreviousNameArray expected = new PreviousNameArray();

        testItem.setPreviousNameArray(expected);
        assertThat(testItem.getPreviousNameArray(), is(sameInstance(expected)));
    }

    @Test
    void testEqualsAndHashcode() {
        EqualsVerifier.forClass(OfficersItem.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    void testToString() {
        assertThat(testItem.toString(),
                allOf(containsString("OfficersItem["),
                        containsString("additionalProperties={}"),
                        containsString("appointmentDate=<null>"),
                        containsString("apptDatePrefix"),
                        containsString("changedAt=<null>"),
                        containsString("companyNumber=<null>"),
                        containsString("corporateInd=<null>"),
                        containsString("dateOfBirth=<null>"),
                        containsString("forename=<null>"),
                        containsString("honours=<null>"),
                        containsString("identification=<null>"),
                        containsString("internalId=<null>"),
                        containsString("kind=<null>"),
                        containsString("middleName=<null>"),
                        containsString("nationality=<null>"),
                        containsString("occupation=<null>"),
                        containsString("officerDetailId=<null>"),
                        containsString("officerId=<null>"),
                        containsString("officerRole=<null>"),
                        containsString("previousNameArray=<null>"),
                        containsString("resignationDate=<null>"),
                        containsString("secureDirector=<null>"),
                        containsString("serviceAddress=<null>"),
                        containsString("serviceAddressSameAsRegisteredAddress=<null>"),
                        containsString("surname=<null>"),
                        containsString("title=<null>"),
                        containsString("usualResidentialAddress=<null>"),
                        containsString("usualResidentialCountry=<null>")));
    }
}
