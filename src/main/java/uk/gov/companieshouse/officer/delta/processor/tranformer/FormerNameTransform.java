package uk.gov.companieshouse.officer.delta.processor.tranformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.FormerNames;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.model.PreviousNameArray;

@Component
public class FormerNameTransform implements Transformative<PreviousNameArray, FormerNames>{

    @Override
    public FormerNames factory() {
        return new FormerNames();
    }

    public FormerNames transform(PreviousNameArray previousNameArray, FormerNames formerNames) throws NonRetryableErrorException {

        formerNames.setForenames(previousNameArray.getPreviousForename());
        formerNames.setSurname(previousNameArray.getPreviousSurname());

        return formerNames;
    }


}
