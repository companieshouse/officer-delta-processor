package uk.gov.companieshouse.officer.delta.processor.tranformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.PrincipalOfficeAddress;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;

@Component
public class PrincipalOfficeAddressTransform implements Transformative<AddressAPI, PrincipalOfficeAddress> {

    @Override
    public PrincipalOfficeAddress factory() {
        return new PrincipalOfficeAddress();
    }

    public PrincipalOfficeAddress transform(AddressAPI addressAPI, PrincipalOfficeAddress principalOfficeAddress) throws NonRetryableErrorException {
        principalOfficeAddress.setAddressLine1(addressAPI.getAddressLine1());
        principalOfficeAddress.setAddressLine2(addressAPI.getAddressLine2());
        principalOfficeAddress.setCareOf(addressAPI.getCareOf());
        principalOfficeAddress.setCountry(addressAPI.getCountry());
        principalOfficeAddress.setLocality(addressAPI.getLocality());
        principalOfficeAddress.setPoBox(addressAPI.getPoBox());
        principalOfficeAddress.setPostalCode(addressAPI.getPostcode());
        principalOfficeAddress.setPremises(addressAPI.getPremises());
        principalOfficeAddress.setRegion(addressAPI.getRegion());

        return principalOfficeAddress;
    }
}
