package uk.gov.companieshouse.officer.delta.processor.transformer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.ContributionSubType;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.model.DeltaContributionSubType;

/**
 * The type Contribution sub-type transform.
 */
@Component
public class ContributionSubTypeTransform implements Transformative<DeltaContributionSubType, ContributionSubType> {

    @Override
    public ContributionSubType factory() {
        return new ContributionSubType();
    }

    /**
     * Transform.
     * @param contributionSubTypeArray the contributionSubTypeArray
     * @param contributionSubType       the contributionSubType
     * @return the ContributionSubTypes
     * @throws NonRetryableErrorException NonRetryableErrorException
     */
    public ContributionSubType transform(DeltaContributionSubType contributionSubTypeArray, ContributionSubType contributionSubType)
            throws NonRetryableErrorException {
        String subType = contributionSubTypeArray.getSubType();
        contributionSubType.setSubType(StringUtils.isNotBlank(subType) ? CapitalContributionSubType.getMappedValue(subType) : null);

        return contributionSubType;
    }
}
