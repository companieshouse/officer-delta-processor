package uk.gov.companieshouse.officer.delta.processor.tranformer;


import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.lookupOfficeRole;
import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.parseDateString;
import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.parseDateTimeString;
import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.parseYesOrNo;
import static uk.gov.companieshouse.officer.delta.processor.tranformer.rules.ReferenceData.isPre1992Role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.officer.delta.processor.exception.ProcessException;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;

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
        officer.setUpdatedAt(parseDateTimeString("changedAt", source.getChangedAt()));

        if (source.getResignationDate() != null) {
            officer.setResignedOn(parseDateString("resignation_date", source.getResignationDate()));
        }
        officer.setCompanyNumber(source.getCompanyNumber());
        officer.setTitle(source.getTitle());
        officer.setForename(source.getForename());
        officer.setOtherForenames(source.getMiddleName());
        officer.setSurname(source.getSurname());
        officer.setNationality(source.getNationality());
        officer.setOccupation(source.getOccupation());
        officer.setDateOfBirth(parseDateString("dateOfBirth", source.getDateOfBirth()));
        officer.setOfficerRole(lookupOfficeRole(source.getKind()));
        officer.setHonours(source.getHonours());

        final Instant appointmentDate = parseDateString("appointmentDate", source.getAppointmentDate());

        if (isPre1992Role(officer.getOfficerRole())) {
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
        officer.setCountryOfResidence(source.getUsualResidentialCountry());

        officer.setIdentificationData(idTransform.transform(source.getIdentification()));

        return officer;
    }
}


