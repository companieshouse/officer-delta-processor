package uk.gov.companieshouse.officer.delta.processor.tranformer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.delta.officers.IdentificationAPI;
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
        assertThat(testTransform.factory(), is(instanceOf(IdentificationAPI.class)));
    }

    @Test
    void transformSingle() {
        final Identification identification = new Identification();
        final IdentificationAPI identificationAPI = new IdentificationAPI("identificationType",
                "legalAuthority",
                "legalForm",
                "placeRegistered",
                "registrationNumber");

        identificationAPI.setAdditionalProperty("additional", "property");
        identification.setEea(identificationAPI);

        final IdentificationAPI result = testTransform.transform(identification, identificationAPI);

        assertThat(result, is(equalTo(identificationAPI)));
        assertThat(result.getAdditionalProperties(), hasEntry("additional", "property"));

    }

}
