package uk.gov.companieshouse.officer.delta.processor.tranformer;

import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.lookupOfficeRole;
import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.parseDateString;
import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.parseDateTimeString;
import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.parseYesOrNo;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.delta.officers.FormerNamesAPI;
import uk.gov.companieshouse.api.model.delta.officers.OfficerAPI;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithCountryOfResidence;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithDateOfBirth;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithFormerNames;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithOccupation;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithPre1992Appointment;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithResidentialAddress;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public OfficerAPI transform(OfficersItem source, OfficerAPI officer) {

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

        if (RolesWithFormerNames.includes(officerRole) && source.getPreviousNameArray() != null) {
            officer.setFormerNameData(source.getPreviousNameArray().stream()
                    .map(s -> new FormerNamesAPI(
                            s.getPreviousForename(), s.getPreviousSurname())).collect(Collectors.toList()));
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

        if (RolesWithResidentialAddress.includes(officerRole)) {
            officer.setUsualResidentialAddress(source.getUsualResidentialAddress());
            officer.setResidentialAddressSameAsServiceAddress(
                    BooleanUtils.toBooleanObject(source.getResidentialAddressSameAsServiceAddress()));
            officer.setSecureOfficer(BooleanUtils.toBooleanObject(source.getSecureDirector()));

            //Prevent it from being stored in URA within the appointments collection.
            officer.getUsualResidentialAddress().setUsualCountryOfResidence(null);
        }

        if (RolesWithCountryOfResidence.includes(officerRole)) {
            officer.setCountryOfResidence(source.getServiceAddress().getUsualCountryOfResidence());
        }

        //Prevent it from being stored in URA within the appointments collection.
        officer.getServiceAddress().setUsualCountryOfResidence(null);

        Optional.ofNullable(source.getIdentification())
                .ifPresent(i -> officer.setIdentificationData(idTransform.transform(i)));

        if (RolesWithDateOfBirth.includes(officerRole) && isNotEmpty(source.getDateOfBirth())) {
            officer.setDateOfBirth(parseDateString("dateOfBirth", source.getDateOfBirth()));
        }

        return officer;
    }
}


