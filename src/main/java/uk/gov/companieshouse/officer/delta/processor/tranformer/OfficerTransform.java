package uk.gov.companieshouse.officer.delta.processor.tranformer;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.officer.delta.processor.exception.ProcessException;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;

import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.parseBackwardsDate;
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
        officer.setAppointedOn(parseBackwardsDate(source.getAppointmentDate()));
        officer.setCompanyNumber(source.getCompanyNumber());
        officer.setTitle(source.getTitle());
        officer.setForename(source.getForename());
        officer.setOtherForenames(source.getMiddleName());
        officer.setSurname(source.getSurname());
        officer.setNationality(source.getNationality());
        officer.setOccupation(source.getOccupation());
        officer.setDateOfBirth(parseBackwardsDate(source.getDateOfBirth()));
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


