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
        return new IdentificationAPI(source.getEea());
    }
}
