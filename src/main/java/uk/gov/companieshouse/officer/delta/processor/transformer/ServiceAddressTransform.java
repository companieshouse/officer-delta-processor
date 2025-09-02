package uk.gov.companieshouse.officer.delta.processor.transformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.ServiceAddress;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;

/**
 * The type Service address transform.
 */
@Component
public class ServiceAddressTransform implements Transformative<AddressAPI, ServiceAddress> {

    @Override
    public ServiceAddress factory() {
        return new ServiceAddress();
    }

    /**
     * Transform.
     * @param addressApi                        the addressApi
     * @param serviceAddress                    the serviceAddress
     * @return                                  ServiceAddress
     * @throws NonRetryableErrorException       NonRetryableErrorException
     */
    public ServiceAddress transform(AddressAPI addressApi, ServiceAddress serviceAddress)
            throws NonRetryableErrorException {
        serviceAddress.setAddressLine1(addressApi.getAddressLine1());
        serviceAddress.setAddressLine2(addressApi.getAddressLine2());
        serviceAddress.setCareOf(addressApi.getCareOfName());
        serviceAddress.setCountry(addressApi.getCountry());
        serviceAddress.setLocality(addressApi.getLocality());
        serviceAddress.setPoBox(addressApi.getPoBox());
        serviceAddress.setPostalCode(addressApi.getPostcode());
        serviceAddress.setPremises(addressApi.getPremises());
        serviceAddress.setRegion(addressApi.getRegion());

        return serviceAddress;
    }
}
