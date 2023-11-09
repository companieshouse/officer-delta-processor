package uk.gov.companieshouse.officer.delta.processor.tranformer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.appointment.Identification;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;
import uk.gov.companieshouse.officer.delta.processor.model.DeltaIdentification;

@ExtendWith(MockitoExtension.class)
class IdentificationTransformTest {

    private IdentificationTransform testTransform;

    @BeforeEach
    void setUp() {
        testTransform = new IdentificationTransform();
    }

    @Test
    void factory() {
        assertThat(testTransform.factory(), is(instanceOf(Identification.class)));
    }

    @Test
    void transformEea() {
        final DeltaIdentification identification = new DeltaIdentification();
        final Identification identificationAPI = createIdentificationAPI("eea");

        identification.setEea(identificationAPI);

        final Identification result = testTransform.transform(identification, identificationAPI);

        assertThat(result, is(equalTo(identificationAPI)));
    }

    @Test
    void transformOtherCorpBody() {
        final DeltaIdentification identification = new DeltaIdentification();
        final Identification identificationAPI = createIdentificationAPI("other-corporate-body-or-firm");

        identification.setOtherCorporateBodyOrFirm(identificationAPI);

        final Identification result = testTransform.transform(identification, identificationAPI);

        assertThat(result, is(equalTo(identificationAPI)));
    }

    @Test
    void transformNonEea() {
        final DeltaIdentification identification = new DeltaIdentification();
        final Identification identificationAPI = createIdentificationAPI("non-eea");

        identification.setNonEeaApi(identificationAPI);

        final Identification result = testTransform.transform(identification, identificationAPI);

        assertThat(result, is(equalTo(identificationAPI)));
    }

    @Test
    void transformUkLimitedCompany() {
        final DeltaIdentification identification = new DeltaIdentification();
        final Identification identificationAPI = createIdentificationAPI("uk-limited-company");

        identification.setUKLimitedCompany(identificationAPI);

        final Identification result = testTransform.transform(identification, identificationAPI);

        assertThat(result, is(equalTo(identificationAPI)));
    }

    @Test
    void transformRegisteredOverseasEntityCorporateManagingOfficer() {
        final DeltaIdentification identification = new DeltaIdentification();
        final Identification identificationAPI = createIdentificationAPI(
                "registered-overseas-entity-corporate-managing-officer");

        identification.setRegisteredOverseasEntityCorporateManagingOfficer(identificationAPI);

        final Identification result = testTransform.transform(identification, identificationAPI);

        assertThat(result, is(equalTo(identificationAPI)));
    }

    @Test
    void testTransformShouldHandleNullSource() {
        assertThat(testTransform.transform((DeltaIdentification) null), is(nullValue()));
    }

    private Identification createIdentificationAPI(String identificationType) {

        Identification identification = new Identification();

        identification.setIdentificationType(Identification.IdentificationTypeEnum.fromValue(identificationType));
        identification.setLegalAuthority("legalAuthority");
        identification.setLegalForm("legalForm");
        identification.setPlaceRegistered("placeRegistered");
        identification.setRegistrationNumber("registrationNumber");

        return identification;
    }

}
