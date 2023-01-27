package uk.gov.companieshouse.officer.delta.processor.tranformer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.appointment.ServiceAddress;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
class ServiceAddressTransformTest {

    private ServiceAddressTransform testTransform;

    @BeforeEach
    void setUp() {
        testTransform = new ServiceAddressTransform();
    }

    @Test
    void factory() {
        assertThat(testTransform.factory(), is(instanceOf(ServiceAddress.class)));
    }

    @Test
    void verifySuccessfulTransformOfAddress() {
        final AddressAPI addressAPI = createAddress();

        final ServiceAddress result = testTransform.transform(addressAPI);

        assertThat(result.getAddressLine1(), is(addressAPI.getAddressLine1()));
        assertThat(result.getAddressLine2(), is(addressAPI.getAddressLine2()));
        assertThat(result.getCountry(), is(addressAPI.getCountry()));
        assertThat(result.getLocality(), is(addressAPI.getLocality()));
        assertThat(result.getPostalCode(), is(addressAPI.getPostcode()));
        assertThat(result.getPremises(), is(addressAPI.getPremises()));
        assertThat(result.getRegion(), is(addressAPI.getRegion()));

    }

    private AddressAPI createAddress() {
        AddressAPI addressAPI = new AddressAPI();

        addressAPI.setAddressLine1("ura_line1");
        addressAPI.setAddressLine2("ura_line2");
        addressAPI.setCareOf("ura_care_of");
        addressAPI.setCountry("United Kingdom");
        addressAPI.setLocality("Cardiff");
        addressAPI.setPoBox("ura_po");
        addressAPI.setPostcode("CF2 1B6");
        addressAPI.setPremises("URA");
        addressAPI.setRegion("ura_region");
        addressAPI.setUsualCountryOfResidence("United Kingdom");

        return addressAPI;
    }

}

