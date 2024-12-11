package uk.gov.companieshouse.officer.delta.processor.tranformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.PrincipalOfficeAddress;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;

/**
 * The type Principal office address transform.
 */
@Component
public class PrincipalOfficeAddressTransform implements
        Transformative<AddressAPI, PrincipalOfficeAddress> {

    @Override
    public PrincipalOfficeAddress factory() {
        return new PrincipalOfficeAddress();
    }

    /**
     * Transform.
     * @param addressApi                    the addressApi
     * @param principalOfficeAddress        the principalOfficeAddress
     * @return                              PrincipalOfficeAddress
     * @throws NonRetryableErrorException   NonRetryableErrorException
     */
    public PrincipalOfficeAddress transform(AddressAPI addressApi,
            PrincipalOfficeAddress principalOfficeAddress) throws NonRetryableErrorException {
        principalOfficeAddress.setAddressLine1(addressApi.getAddressLine1());
        principalOfficeAddress.setAddressLine2(addressApi.getAddressLine2());
        principalOfficeAddress.setCareOf(addressApi.getCareOfName());
        principalOfficeAddress.setCountry(addressApi.getCountry());
        principalOfficeAddress.setLocality(addressApi.getLocality());
        principalOfficeAddress.setPoBox(addressApi.getPoBox());
        principalOfficeAddress.setPostalCode(addressApi.getPostcode());
        principalOfficeAddress.setPremises(addressApi.getPremises());
        principalOfficeAddress.setRegion(addressApi.getRegion());

        return principalOfficeAddress;
    }
}
