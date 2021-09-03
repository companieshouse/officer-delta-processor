package uk.gov.companieshouse.officer.delta.processor.transformer;

import static uk.gov.companieshouse.officer.delta.processor.transformer.TransformerUtils.parseBackwardsDate;
import static uk.gov.companieshouse.officer.delta.processor.transformer.TransformerUtils.parseYesOrNo;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.api.model.delta.officers.IdentificationAPI;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;

@Component
public class OfficerTransformer extends AbstractTransformer<OfficersItem, AppointmentAPI> {
    public OfficerTransformer() {
        super(source -> transformOfficer(source));
    }

    private static AppointmentAPI transformOfficer(final OfficersItem source) {
        OfficerAPI officer = new OfficerAPI();
        IdentificationAPI identification = new IdentificationAPI();
        AppointmentAPI appointment = new AppointmentAPI();

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
        officer.setServiceAddressSameAsRegisteredOfficeAddress(parseYesOrNo(source.getServiceAddressSameAsRegisteredAddress()));
        officer.setCountryOfResidence(source.getUsualResidentialCountry());

        identification.setIdentificationType(source.getIdentification().getEea().getIdentificationType());
        identification.setPlaceRegistered(source.getIdentification().getEea().getPlaceRegistered());
        identification.setRegistrationNumber(source.getIdentification().getEea().getRegistrationNumber());
        identification.setLegalAuthority(source.getIdentification().getEea().getLegalAuthority());
        identification.setLegalForm(source.getIdentification().getEea().getLegalForm());
        officer.setIdentificationData(identification);

        appointment.setInternalId(source.getInternalId());
        appointment.setId(TransformerUtils.base64Encode(source.getInternalId()));
        appointment.setData(officer);

        return appointment;
    }

}
