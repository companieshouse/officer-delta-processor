package uk.gov.companieshouse.officer.delta.processor.transformer;

import static uk.gov.companieshouse.officer.delta.processor.transformer.TransformerUtils.parseBackwardsDate;
import static uk.gov.companieshouse.officer.delta.processor.transformer.TransformerUtils.parseYesOrNo;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.api.model.delta.officers.IdentificationAPI;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;

@Component
public class Restructure implements Transform {
    @Override
    public void transform(OfficersItem inputOfficer, AppointmentAPI outputAppointment) {

        OfficerAPI officer = new OfficerAPI();
        IdentificationAPI identification = new IdentificationAPI();

        officer.setAppointedOn(parseBackwardsDate(inputOfficer.getAppointmentDate()));
        officer.setCompanyNumber(inputOfficer.getCompanyNumber());
        officer.setTitle(inputOfficer.getTitle());
        officer.setForename(inputOfficer.getForename());
        officer.setOtherForenames(inputOfficer.getMiddleName());
        officer.setSurname(inputOfficer.getSurname());
        officer.setNationality(inputOfficer.getNationality());
        officer.setOccupation(inputOfficer.getOccupation());
        officer.setDateOfBirth(parseBackwardsDate(inputOfficer.getDateOfBirth()));
        officer.setOfficerRole(inputOfficer.getOfficerRole());
        officer.setHonours(inputOfficer.getHonours());

        officer.setServiceAddress(inputOfficer.getServiceAddress());
        officer.setServiceAddressSameAsRegisteredOfficeAddress(
            parseYesOrNo(inputOfficer.getServiceAddressSameAsRegisteredAddress()));
        officer.setCountryOfResidence(inputOfficer.getUsualResidentialCountry());

        identification.setIdentificationType(inputOfficer.getIdentification().getEea()
            .getIdentificationType());
        identification.setPlaceRegistered(inputOfficer.getIdentification().getEea().getPlaceRegistered());
        identification.setRegistrationNumber(inputOfficer.getIdentification().getEea().getRegistrationNumber());
        identification.setLegalAuthority(inputOfficer.getIdentification().getEea().getLegalAuthority());
        identification.setLegalForm(inputOfficer.getIdentification().getEea().getLegalForm());
        officer.setIdentificationData(identification);

        outputAppointment.setInternalId(inputOfficer.getInternalId());
        outputAppointment.setData(officer);
    }


}
