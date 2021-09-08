package uk.gov.companieshouse.officer.delta.processor.tranformer;

import uk.gov.companieshouse.api.model.delta.officers.IdentificationAPI;
import uk.gov.companieshouse.officer.delta.processor.exception.ProcessException;
import uk.gov.companieshouse.officer.delta.processor.model.Identification;

public class IdentificationTransform implements Transformative<Identification, IdentificationAPI> {
    @Override
    public IdentificationAPI factory() {
        return new IdentificationAPI();
    }

    @Override
    public IdentificationAPI transform(Identification source, IdentificationAPI identification) throws ProcessException {
        identification.setIdentificationType(source.getEea()
                .getIdentificationType());
        identification.setPlaceRegistered(source.getEea().getPlaceRegistered());
        identification.setRegistrationNumber(source.getEea().getRegistrationNumber());
        identification.setLegalAuthority(source.getEea().getLegalAuthority());
        identification.setLegalForm(source.getEea().getLegalForm());

        return identification;
    }
}
