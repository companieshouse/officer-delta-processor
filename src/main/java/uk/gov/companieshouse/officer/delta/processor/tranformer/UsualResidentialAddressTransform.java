package uk.gov.companieshouse.officer.delta.processor.tranformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.UsualResidentialAddress;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;

/**
 * The type Usual residential address transform.
 */
@Component
public class UsualResidentialAddressTransform implements
        Transformative<AddressAPI, UsualResidentialAddress> {

    @Override
    public UsualResidentialAddress factory() {
        return new UsualResidentialAddress();
    }

    /**
     * Transform.
     *
     * @param addressApi              the source
     * @param usualResidentialAddress the output
     * @return UsualResidentialAddress
     * @throws NonRetryableErrorException NonRetryableErrorException
     */
    public UsualResidentialAddress transform(AddressAPI addressApi,
            UsualResidentialAddress usualResidentialAddress) throws NonRetryableErrorException {
        usualResidentialAddress.setAddressLine1(addressApi.getAddressLine1());
        usualResidentialAddress.setAddressLine2(addressApi.getAddressLine2());
        usualResidentialAddress.setCareOf(addressApi.getCareOfName());
        usualResidentialAddress.setCountry(addressApi.getCountry());
        usualResidentialAddress.setLocality(addressApi.getLocality());
        usualResidentialAddress.setPoBox(addressApi.getPoBox());
        usualResidentialAddress.setPostalCode(addressApi.getPostcode());
        usualResidentialAddress.setPremises(addressApi.getPremises());
        usualResidentialAddress.setRegion(addressApi.getRegion());

        return usualResidentialAddress;
    }
}
