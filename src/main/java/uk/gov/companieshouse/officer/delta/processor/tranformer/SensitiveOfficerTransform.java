package uk.gov.companieshouse.officer.delta.processor.tranformer;

import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.lookupOfficeRole;
import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.parseDateString;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.delta.officers.SensitiveOfficerAPI;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithDateOfBirth;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithResidentialAddress;

@Component
public class SensitiveOfficerTransform implements Transformative<OfficersItem, SensitiveOfficerAPI> {

    @Autowired
    public SensitiveOfficerTransform() {
        // empty constructor
    }

    @Override
    public SensitiveOfficerAPI factory() {
        return new SensitiveOfficerAPI();
    }

    @Override
    public SensitiveOfficerAPI transform(OfficersItem source, SensitiveOfficerAPI officer) throws NonRetryableErrorException {
        final String officerRole = lookupOfficeRole(source.getKind(), source.getCorporateInd());
        if (RolesWithResidentialAddress.includes(officerRole)) {
            officer.setUsualResidentialAddress(source.getUsualResidentialAddress());
            officer.setResidentialAddressSameAsServiceAddress(
                    BooleanUtils.toBooleanObject(source.getResidentialAddressSameAsServiceAddress()));

            //Prevent it from being stored in URA within the appointments collection.
            officer.getUsualResidentialAddress().setUsualCountryOfResidence(null);
        }

        if (RolesWithDateOfBirth.includes(officerRole) && isNotEmpty(source.getDateOfBirth())) {
            officer.setDateOfBirth(parseDateString("dateOfBirth", source.getDateOfBirth()));
        }

        return officer;
    }
}


