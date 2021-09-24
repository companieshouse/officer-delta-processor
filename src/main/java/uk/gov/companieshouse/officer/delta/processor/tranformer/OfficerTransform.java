package uk.gov.companieshouse.officer.delta.processor.tranformer;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.officer.delta.processor.exception.ProcessException;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithCountryOfResidence;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithPre1992Appointment;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithDateOfBirth;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithOccupation;

import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.parseDateString;
import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.parseDateTimeString;
import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.parseYesOrNo;
import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.lookupOfficeRole;

import java.time.Instant;

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

        officer.setUpdatedAt(
                parseDateTimeString("changedAt", source.getChangedAt()));
        officer.setAppointedOn(
                parseDateString("appointmentDate", source.getAppointmentDate()));

        if (source.getResignationDate() != null) {
            officer.setResignedOn(
                    parseDateString("resignation_date", source.getResignationDate()));
        }
        officer.setCompanyNumber(source.getCompanyNumber());
        officer.setTitle(source.getTitle());
        officer.setForename(source.getForename());
        officer.setOtherForenames(source.getMiddleName());
        officer.setSurname(source.getSurname());
        officer.setHonours(source.getHonours());

        final String officerRole = lookupOfficeRole(source.getKind());
        officer.setOfficerRole(officerRole);

        // Occupation and Nationality are in the same set of Roles
        if (RolesWithOccupation.includes(officerRole)) {
            officer.setNationality(source.getNationality());
            officer.setOccupation(source.getOccupation());
        }

        final Instant appointmentDate = parseDateString(
            "appointmentDate", source.getAppointmentDate());

        if (RolesWithPre1992Appointment.includes(officerRole)) {
            officer.setIsPre1992Appointment(parseYesOrNo(source.getApptDatePrefix()));
            if (officer.isPre1992Appointment()) {
                officer.setAppointedOn(null);
                officer.setAppointedBefore(appointmentDate);
            } else {
                officer.setAppointedOn(appointmentDate);
            }
        } else {
            officer.setAppointedOn(appointmentDate);
        }

        officer.setServiceAddress(source.getServiceAddress());
        officer.setServiceAddressSameAsRegisteredOfficeAddress(
                parseYesOrNo(source.getServiceAddressSameAsRegisteredAddress()));
        officer.setUsualResidentialAddress(source.getUsualResidentialAddress());
        officer.setResidentialAddressSameAsServiceAddress(
                parseYesOrNo(source.getResidentialAddressSameAsServiceAddress()));

        if (RolesWithCountryOfResidence.includes(officerRole)) {
            officer.setCountryOfResidence(source.getUsualResidentialCountry());
        }

        officer.setIdentificationData(idTransform.transform(source.getIdentification()));

        if (RolesWithDateOfBirth.includes(officerRole)) {
            officer.setDateOfBirth(parseDateString("dateOfBirth", source.getDateOfBirth()));
        }

        return officer;
    }
}


