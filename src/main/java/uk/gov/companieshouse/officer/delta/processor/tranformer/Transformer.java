package uk.gov.companieshouse.officer.delta.processor.tranformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;

import java.util.List;

@Component
public class Transformer {
    private List<Transform> transforms;

    @Autowired
    public Transformer(List<Transform> transforms) {
        this.transforms = transforms;
    }

    public OfficerAPI transform(OfficersItem officer) {
        OfficerAPI output = new OfficerAPI();

        transforms
                .parallelStream()
                .forEach(transformer -> transformer.transform(officer, output));

        return output;
    }
}
