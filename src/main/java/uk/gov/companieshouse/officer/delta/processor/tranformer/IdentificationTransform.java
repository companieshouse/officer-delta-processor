package uk.gov.companieshouse.officer.delta.processor.tranformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.delta.officers.IdentificationAPI;
import uk.gov.companieshouse.officer.delta.processor.model.Identification;

@Component
public class IdentificationTransform implements Transformative<Identification, IdentificationAPI> {
    @Override
    public IdentificationAPI factory() {
        return new IdentificationAPI();
    }

    @Override
    public IdentificationAPI transform(Identification source, IdentificationAPI identification) {

        if (source.getEea() != null) {
            identification = new IdentificationAPI(source.getEea());
            identification.setIdentificationType("eea");
        } else if (source.getOtherCorporateBodyOrFirm() != null) {
            identification = new IdentificationAPI(source.getOtherCorporateBodyOrFirm());
            identification.setIdentificationType("other-corporate-body-or-firm");
        } else if (source.getNonEeaApi() != null) {
            identification = new IdentificationAPI(source.getNonEeaApi());
            identification.setIdentificationType("non-eea");
        } else if (source.getUKLimitedCompany() != null) {
            identification = new IdentificationAPI(source.getUKLimitedCompany());
            identification.setIdentificationType("UK-limited-company");
        }

        return identification;
    }
}
