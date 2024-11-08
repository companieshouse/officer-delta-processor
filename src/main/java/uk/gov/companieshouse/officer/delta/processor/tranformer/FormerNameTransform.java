package uk.gov.companieshouse.officer.delta.processor.tranformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.FormerNames;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.model.PreviousNameArray;

/**
 * The type Former name transform.
 */
@Component
public class FormerNameTransform implements Transformative<PreviousNameArray, FormerNames> {

    @Override
    public FormerNames factory() {
        return new FormerNames();
    }

    /**
     * Transform.
     * @param previousNameArray the previousNameArray
     * @param formerNames       the formerNames
     * @return the FormerNames
     * @throws NonRetryableErrorException NonRetryableErrorException
     */
    public FormerNames transform(PreviousNameArray previousNameArray, FormerNames formerNames)
            throws NonRetryableErrorException {

        formerNames.setForenames(previousNameArray.getPreviousForename());
        formerNames.setSurname(previousNameArray.getPreviousSurname());

        return formerNames;
    }


}
