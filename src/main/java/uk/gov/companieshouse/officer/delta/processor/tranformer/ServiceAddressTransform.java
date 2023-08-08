package uk.gov.companieshouse.officer.delta.processor.tranformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.ServiceAddress;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;

@Component
public class ServiceAddressTransform implements Transformative<AddressAPI, ServiceAddress> {

    @Override
    public ServiceAddress factory() {
        return new ServiceAddress();
    }

    public ServiceAddress transform(AddressAPI addressAPI, ServiceAddress serviceAddress) throws NonRetryableErrorException {
        serviceAddress.setAddressLine1(addressAPI.getAddressLine1());
        serviceAddress.setAddressLine2(addressAPI.getAddressLine2());
        serviceAddress.setCareOf(addressAPI.getCareOf());
        serviceAddress.setCountry(addressAPI.getCountry());
        serviceAddress.setLocality(addressAPI.getLocality());
        serviceAddress.setPoBox(addressAPI.getPoBox());
        serviceAddress.setPostalCode(addressAPI.getPostcode());
        serviceAddress.setPremises(addressAPI.getPremises());
        serviceAddress.setRegion(addressAPI.getRegion());

        return serviceAddress;
    }
}
