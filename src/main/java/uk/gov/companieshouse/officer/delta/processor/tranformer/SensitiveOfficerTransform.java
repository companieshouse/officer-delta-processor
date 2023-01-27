package uk.gov.companieshouse.officer.delta.processor.tranformer;

import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.*;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.DateOfBirth;
import uk.gov.companieshouse.api.appointment.SensitiveData;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithDateOfBirth;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithResidentialAddress;

@Component
public class SensitiveOfficerTransform implements Transformative<OfficersItem, SensitiveData> {

    UsualResidentialAddressTransform usualResidentialAddressTransform;

    @Autowired
    public SensitiveOfficerTransform(UsualResidentialAddressTransform usualResidentialAddressTransform) {
        this.usualResidentialAddressTransform = usualResidentialAddressTransform;
    }

    @Override
    public SensitiveData factory() {
        return new SensitiveData();
    }

    @Override
    public SensitiveData transform(OfficersItem source, SensitiveData officer) throws NonRetryableErrorException {
        final String officerRole = lookupOfficerRole(source.getKind(), source.getCorporateInd());
        if (RolesWithResidentialAddress.includes(officerRole)) {
        officer.setUsualResidentialAddress(usualResidentialAddressTransform.transform(source.getUsualResidentialAddress()));
            officer.setResidentialAddressSameAsServiceAddress(
                    BooleanUtils.toBooleanObject(source.getResidentialAddressSameAsServiceAddress()));

            //Prevent it from being stored in URA within the appointments collection.
            //UPDATE no UsualCountryOfResidence field in SensitiveData
            //officer.getUsualResidentialAddress().setUsualCountryOfResidence(null);
         }

        if (RolesWithDateOfBirth.includes(officerRole) && isNotEmpty(source.getDateOfBirth())) {
            DateOfBirth dateOfBirth = new DateOfBirth();

            String year = String.valueOf((parseLocalDate("dateOfBirth", source.getDateOfBirth())).getYear());
            String month = String.valueOf((parseLocalDate("dateOfBirth", source.getDateOfBirth())).getMonthValue());
            String day = String.valueOf((parseLocalDate("dateOfBirth", source.getDateOfBirth())).getDayOfMonth());

            dateOfBirth.setYear(Integer.parseInt(year));
            dateOfBirth.setMonth(Integer.parseInt(month));
            dateOfBirth.setDay(Integer.parseInt(day));

            officer.setDateOfBirth(dateOfBirth);


        }

        return officer;
    }
}


