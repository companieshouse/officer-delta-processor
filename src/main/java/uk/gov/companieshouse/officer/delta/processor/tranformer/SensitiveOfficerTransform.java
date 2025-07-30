package uk.gov.companieshouse.officer.delta.processor.tranformer;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.lookupOfficerRole;
import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.parseLocalDate;

import org.apache.commons.lang3.BooleanUtils;
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

    private static final String DOB_IDENTIFIER = "dateOfBirth";
    UsualResidentialAddressTransform usualResidentialAddressTransform;

    @Autowired
    public SensitiveOfficerTransform(
            UsualResidentialAddressTransform usualResidentialAddressTransform) {
        this.usualResidentialAddressTransform = usualResidentialAddressTransform;
    }

    @Override
    public SensitiveData factory() {
        return new SensitiveData();
    }

    @Override
    public SensitiveData transform(OfficersItem source, SensitiveData officer)
            throws NonRetryableErrorException {
        final String officerRole = lookupOfficerRole(source.getKind(), source.getCorporateInd());
        if (RolesWithResidentialAddress.includes(officerRole)) {
            officer.setUsualResidentialAddress(usualResidentialAddressTransform.transform(
                    source.getUsualResidentialAddress()));
            officer.setResidentialAddressIsSameAsServiceAddress(BooleanUtils.toBooleanObject(
                    source.getResidentialAddressSameAsServiceAddress()));
        }

        if (RolesWithDateOfBirth.includes(officerRole) && isNotEmpty(source.getDateOfBirth())) {
            var dobFromString = parseLocalDate(DOB_IDENTIFIER, source.getDateOfBirth());
            var year = String.valueOf(dobFromString.getYear());
            var month = String.valueOf(dobFromString.getMonthValue());
            var day = String.valueOf(dobFromString.getDayOfMonth());

            var dateOfBirth = new DateOfBirth();
            dateOfBirth.setYear(Integer.parseInt(year));
            dateOfBirth.setMonth(Integer.parseInt(month));
            dateOfBirth.setDay(Integer.parseInt(day));

            officer.setDateOfBirth(dateOfBirth);
        }

        return officer;
    }
}


