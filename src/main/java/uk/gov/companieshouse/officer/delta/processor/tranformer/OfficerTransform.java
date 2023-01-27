package uk.gov.companieshouse.officer.delta.processor.tranformer;

import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.parseLocalDate;
import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.parseYesOrNo;
import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.lookupOfficerRole;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.*;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithCountryOfResidence;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithFormerNames;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithOccupation;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithPre1992Appointment;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithResidentialAddress;

import java.time.LocalDate;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class OfficerTransform implements Transformative<OfficersItem, Data> {
    public static final String COMPANY = "/company";
    public static final String APPOINTMENTS = "/appointments";
    public static final String OFFICERS = "/officers";
    IdentificationTransform idTransform;

    ServiceAddressTransform serviceAddressTransform;

    FormerNameTransform formerNameTransform;

    @Autowired
    public OfficerTransform(IdentificationTransform idTransform, ServiceAddressTransform serviceAddressTransform, FormerNameTransform formerNameTransform) {
        this.idTransform = idTransform;
        this.serviceAddressTransform = serviceAddressTransform;
        this.formerNameTransform = formerNameTransform;

    }

    @Override
    public Data factory() {
        return new Data();
    }

    @Override
    public Data transform(OfficersItem source, Data officer) throws NonRetryableErrorException {

        officer.setAppointedOn(parseLocalDate("appointmentDate", source.getAppointmentDate()));

        if (source.getResignationDate() != null) {
            officer.setResignedOn(parseLocalDate("resignation_date", source.getResignationDate()));
        }

        final String officerRole = lookupOfficerRole(source.getKind(), source.getCorporateInd());
        officer.setOfficerRole(Data.OfficerRoleEnum.fromValue(officerRole));

        if (source.getCorporateInd().equalsIgnoreCase("Y")) {
            officer.setCompanyName(source.getSurname());
        } else {
            officer.setTitle(source.getTitle());
            officer.setForename(source.getForename());
            officer.setOtherForenames(source.getMiddleName());
            officer.setSurname(source.getSurname());
            officer.setHonours(source.getHonours());
        }

        officer.setCompanyNumber(source.getCompanyNumber());
        officer.setPersonNumber(source.getPersonNumber());

        // Occupation and Nationality are in the same set of Roles
        if (RolesWithOccupation.includes(officerRole)) {
            officer.setNationality(source.getNationality());
            officer.setOccupation(source.getOccupation());
        }

        if (RolesWithFormerNames.includes(officerRole) && source.getPreviousNameArray() != null) {
            officer.setFormerNames(source.getPreviousNameArray().stream()
                    .map(formerNameTransform::transform).collect(Collectors.toList()));
        }

        final LocalDate appointmentDate = parseLocalDate(
                "appointmentDate", source.getAppointmentDate());

        if (RolesWithPre1992Appointment.includes(officerRole)) {
            officer.setIsPre1992Appointment(parseYesOrNo(source.getApptDatePrefix()));
            if (officer.getIsPre1992Appointment()) {
                officer.setAppointedOn(null);
                officer.setAppointedBefore(appointmentDate);
            } else {
                officer.setAppointedOn(appointmentDate);
            }
        } else {
            //setPre1992Appointment to false because previous default value (boolean) is false but new default value (Boolean) is null
            officer.setIsPre1992Appointment(false);
            officer.setAppointedOn(appointmentDate);
        }

        officer.setServiceAddress(serviceAddressTransform.transform(source.getServiceAddress()));
        officer.setServiceAddressSameAsRegisteredOfficeAddress(
                parseYesOrNo(source.getServiceAddressSameAsRegisteredAddress()));

        if (RolesWithResidentialAddress.includes(officerRole)) {
            officer.setIsSecureOfficer(BooleanUtils.toBooleanObject(source.getSecureDirector()));
        }

        if (RolesWithCountryOfResidence.includes(officerRole)) {
            officer.setCountryOfResidence(source.getServiceAddress().getUsualCountryOfResidence());
        }

        //Prevent it from being stored in URA within the appointments collection.
        //UPDATED: Commented out for tests, does this need to be here?
        //officer.setCountryOfResidence(null);

        if (source.getIdentification() != null)
            officer.setIdentification(idTransform.transform(source.getIdentification()));

        String selfLink = COMPANY.concat("/")
            .concat(source.getCompanyNumber())
            .concat(APPOINTMENTS).concat("/")
            .concat(TransformerUtils.encode(source.getInternalId()));

        String officerSelf = OFFICERS.concat("/")
            .concat(TransformerUtils.encode(source.getOfficerId()));

        String officerAppointments = OFFICERS.concat("/")
            .concat(TransformerUtils.encode(source.getOfficerId()))
            .concat(APPOINTMENTS);

        ItemLinkTypes itemLinkTypes = new ItemLinkTypes();
        OfficerLinkTypes officerLinkTypes = new OfficerLinkTypes();

        itemLinkTypes.setSelf(selfLink);
        itemLinkTypes.setOfficer(officerLinkTypes);
        itemLinkTypes.getOfficer().setSelf(officerSelf);
        itemLinkTypes.getOfficer().setAppointments(officerAppointments);
        officer.setLinks(Collections.singletonList(itemLinkTypes));

        return officer;
    }
}


