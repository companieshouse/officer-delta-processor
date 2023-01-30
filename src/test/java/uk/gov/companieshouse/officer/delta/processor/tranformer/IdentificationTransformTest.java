package uk.gov.companieshouse.officer.delta.processor.tranformer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.officer.delta.processor.model.Identification;

@ExtendWith(MockitoExtension.class)
class IdentificationTransformTest {
    private IdentificationTransform testTransform;

    @BeforeEach
    void setUp() {
        testTransform = new IdentificationTransform();
    }

    @Test
    void factory() {
        assertThat(testTransform.factory(), is(instanceOf(uk.gov.companieshouse.api.appointment.Identification.class)));
    }

    @Test
    void transformEea() {
        final Identification identification = new Identification();
        final uk.gov.companieshouse.api.appointment.Identification identificationAPI = createIdentificationAPI("eea");

        identification.setEea(identificationAPI);

        final uk.gov.companieshouse.api.appointment.Identification result = testTransform.transform(identification, identificationAPI);

        assertThat(result, is(equalTo(identificationAPI)));
    }

    @Test
    void transformOtherCorpBody() {
        final Identification identification = new Identification();
        final uk.gov.companieshouse.api.appointment.Identification identificationAPI = createIdentificationAPI("other-corporate-body-or-firm");

        identification.setOtherCorporateBodyOrFirm(identificationAPI);

        final uk.gov.companieshouse.api.appointment.Identification result = testTransform.transform(identification, identificationAPI);

        assertThat(result, is(equalTo(identificationAPI)));
    }

    @Test
    void transformNonEea() {
        final Identification identification = new Identification();
        final uk.gov.companieshouse.api.appointment.Identification identificationAPI = createIdentificationAPI("non-eea");

        identification.setNonEeaApi(identificationAPI);

        final uk.gov.companieshouse.api.appointment.Identification result = testTransform.transform(identification, identificationAPI);

        assertThat(result, is(equalTo(identificationAPI)));
    }

    @Test
    void transformUkLimitedCompany() {
        final Identification identification = new Identification();
        //changed uk-limited-company to uk-limited
        final uk.gov.companieshouse.api.appointment.Identification identificationAPI = createIdentificationAPI("uk-limited");

        identification.setUKLimitedCompany(identificationAPI);

        final uk.gov.companieshouse.api.appointment.Identification result = testTransform.transform(identification, identificationAPI);

        assertThat(result, is(equalTo(identificationAPI)));
    }

    private uk.gov.companieshouse.api.appointment.Identification createIdentificationAPI(String identificationType) {

        uk.gov.companieshouse.api.appointment.Identification identification = new uk.gov.companieshouse.api.appointment.Identification();

        identification.setIdentificationType(uk.gov.companieshouse.api.appointment.Identification.IdentificationTypeEnum.fromValue(identificationType));
        identification.setLegalAuthority("legalAuthority");
        identification.setLegalForm("legalForm");
        identification.setPlaceRegistered("placeRegistered");
        identification.setRegistrationNumber("registrationNumber");

        return identification;
    }

}
