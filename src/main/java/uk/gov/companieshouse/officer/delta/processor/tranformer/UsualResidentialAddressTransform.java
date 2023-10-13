package uk.gov.companieshouse.officer.delta.processor.tranformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.UsualResidentialAddress;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;

@Component
public class UsualResidentialAddressTransform implements Transformative<AddressAPI, UsualResidentialAddress>{

    @Override
    public UsualResidentialAddress factory() {
        return new UsualResidentialAddress();
    }

    public UsualResidentialAddress transform(AddressAPI addressAPI, UsualResidentialAddress usualResidentialAddress) throws NonRetryableErrorException {
        usualResidentialAddress.setAddressLine1(addressAPI.getAddressLine1());
        usualResidentialAddress.setAddressLine2(addressAPI.getAddressLine2());
        usualResidentialAddress.setCareOf(addressAPI.getCareOfName());
        usualResidentialAddress.setCountry(addressAPI.getCountry());
        usualResidentialAddress.setLocality(addressAPI.getLocality());
        usualResidentialAddress.setPoBox(addressAPI.getPoBox());
        usualResidentialAddress.setPostalCode(addressAPI.getPostcode());
        usualResidentialAddress.setPremises(addressAPI.getPremises());
        usualResidentialAddress.setRegion(addressAPI.getRegion());

        return usualResidentialAddress;
    }
}
