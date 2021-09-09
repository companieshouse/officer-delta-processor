package uk.gov.companieshouse.officer.delta.processor.tranformer;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.officer.delta.processor.exception.ProcessException;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;

import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.parseDateString;
import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.parseDateTimeString;
import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.parseYesOrNo;

@Component
public class OfficerTransform implements Transformative<OfficersItem, OfficerAPI> {
    IdentificationTransform idTransform;

    @Autowired
    public OfficerTransform(IdentificationTransform idTransform) {
        this.idTransform = idTransform;
    }

    @Override
    public OfficerAPI factory() {
        return new OfficerAPI();
    }

    @Override
    public OfficerAPI transform(OfficersItem source, OfficerAPI officer) throws ProcessException {
        officer.setUpdatedAt(parseDateTimeString("changedAt", source.getChangedAt()));
        officer.setAppointedOn(parseDateString("appointmentDate", source.getAppointmentDate()));

        final String resignationDate = (String) source.getAdditionalProperties().get("resignation_date");

        if (resignationDate != null) {
            officer.setResignedOn(parseDateString("resignation_date", resignationDate));
        }
        officer.setCompanyNumber(source.getCompanyNumber());
        officer.setTitle(source.getTitle());
        officer.setForename(source.getForename());
        officer.setOtherForenames(source.getMiddleName());
        officer.setSurname(source.getSurname());
        officer.setNationality(source.getNationality());
        officer.setOccupation(source.getOccupation());
        officer.setDateOfBirth(parseDateString("dateOfBirth", source.getDateOfBirth()));
        officer.setOfficerRole(source.getOfficerRole());
        officer.setHonours(source.getHonours());

        officer.setServiceAddress(source.getServiceAddress());
        officer.setServiceAddressSameAsRegisteredOfficeAddress(
                parseYesOrNo(source.getServiceAddressSameAsRegisteredAddress()));
        officer.setCountryOfResidence(source.getUsualResidentialCountry());

        officer.setIdentificationData(idTransform.transform(source.getIdentification()));

        return officer;
    }
}


