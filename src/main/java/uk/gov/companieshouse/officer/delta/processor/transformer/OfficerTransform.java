package uk.gov.companieshouse.officer.delta.processor.transformer;

import static uk.gov.companieshouse.officer.delta.processor.transformer.TransformerUtils.lookupOfficerRole;
import static uk.gov.companieshouse.officer.delta.processor.transformer.TransformerUtils.parseLocalDate;
import static uk.gov.companieshouse.officer.delta.processor.transformer.TransformerUtils.parseYesOrNo;

import java.time.LocalDate;
import java.util.Collections;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.Data;
import uk.gov.companieshouse.api.appointment.ItemLinkTypes;
import uk.gov.companieshouse.api.appointment.OfficerLinkTypes;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;
import uk.gov.companieshouse.officer.delta.processor.model.enums.OfficerRole;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithCountryOfResidence;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithFormerNames;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithOccupation;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithPre1992Appointment;
import uk.gov.companieshouse.officer.delta.processor.model.enums.RolesWithResidentialAddress;

/**
 * The type Officer transform.
 */
@Component
public class OfficerTransform implements Transformative<OfficersItem, Data> {

    /**
     * The constant COMPANY.
     */
    public static final String COMPANY = "/company";
    /**
     * The constant APPOINTMENTS.
     */
    public static final String APPOINTMENTS = "/appointments";
    /**
     * The constant OFFICERS.
     */
    public static final String OFFICERS = "/officers";
    private final IdentificationTransform idTransform;

    private final IdentityVerificationDetailsTransform identityVerificationDetailsTransform;

    private final ServiceAddressTransform serviceAddressTransform;

    private final FormerNameTransform formerNameTransform;

    private final ContributionSubTypeTransform contributionSubTypeTransform;

    private final PrincipalOfficeAddressTransform principalOfficeAddressTransform;

    /**
     * Instantiates a new Officer transform.
     *
     * @param idTransform                     the id transform
     * @param serviceAddressTransform         the service address transform
     * @param formerNameTransform             the former name transform
     * @param principalOfficeAddressTransform the principal office address transform
     */
    @Autowired
    public OfficerTransform(IdentificationTransform idTransform,
                            IdentityVerificationDetailsTransform identityVerificationDetailsTransform,
                            ServiceAddressTransform serviceAddressTransform,
                            FormerNameTransform formerNameTransform,
                            ContributionSubTypeTransform contributionSubTypeTransform,
                            PrincipalOfficeAddressTransform principalOfficeAddressTransform) {
        this.idTransform = idTransform;
        this.identityVerificationDetailsTransform = identityVerificationDetailsTransform;
        this.serviceAddressTransform = serviceAddressTransform;
        this.formerNameTransform = formerNameTransform;
        this.contributionSubTypeTransform = contributionSubTypeTransform;
        this.principalOfficeAddressTransform = principalOfficeAddressTransform;
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
        officer.setPersonNumber(source.getExternalNumber());

        // Occupation and Nationality are in the same set of Roles
        if (RolesWithOccupation.includes(officerRole)) {
            officer.setNationality(source.getNationality());
            officer.setOccupation(source.getOccupation());
        }

        if (RolesWithFormerNames.includes(officerRole) && source.getPreviousNameArray() != null) {
            officer.setFormerNames(
                    source.getPreviousNameArray().stream().map(formerNameTransform::transform)
                        .toList());
        }

        final var appointmentDate = parseLocalDate("appointmentDate", source.getAppointmentDate());

        handleManagingOfficerFields(source, officer, officerRole);

        handleRolesWithPre1992Appointment(source, officer, officerRole, appointmentDate);

        officer.setServiceAddress(serviceAddressTransform.transform(source.getServiceAddress()));
        officer.serviceAddressIsSameAsRegisteredOfficeAddress(
                parseYesOrNo(source.getServiceAddressSameAsRegisteredAddress()));

        if (RolesWithResidentialAddress.includes(officerRole)) {
            officer.setIsSecureOfficer(BooleanUtils.toBooleanObject(source.getSecureDirector()));
        }

        if (RolesWithCountryOfResidence.includes(officerRole)
                && source.getServiceAddress() != null) {
            officer.setCountryOfResidence(source.getServiceAddress().getUsualCountryOfResidence());
        }

        if (source.getIdentification() != null) {
            officer.setIdentification(idTransform.transform(source.getIdentification()));
        }

        if (source.getIdentityVerificationDetails() != null) {
            officer.setIdentityVerificationDetails(
                    identityVerificationDetailsTransform.transform(source.getIdentityVerificationDetails()));
        }

        var itemLinkTypes = new ItemLinkTypes();
        String selfLink = COMPANY.concat("/").concat(source.getCompanyNumber()).concat(APPOINTMENTS)
                .concat("/").concat(TransformerUtils.encode(source.getInternalId()));
        itemLinkTypes.setSelf(selfLink);

        var officerLinkTypes = new OfficerLinkTypes();
        itemLinkTypes.setOfficer(officerLinkTypes);

        String officerSelf = OFFICERS.concat("/")
                .concat(TransformerUtils.encode(source.getOfficerId()));
        itemLinkTypes.getOfficer().setSelf(officerSelf);

        String officerAppointments = OFFICERS.concat("/")
                .concat(TransformerUtils.encode(source.getOfficerId())).concat(APPOINTMENTS);
        itemLinkTypes.getOfficer().setAppointments(officerAppointments);
        officer.setLinks(Collections.singletonList(itemLinkTypes));

        officer.setContributionCurrencyValue(source.getContributionCurrencyValue());
        officer.setContributionCurrencyType(source.getContributionCurrencyType());

        if (source.getContributionSubTypes() != null) {
            officer.setContributionSubTypes(
                    source.getContributionSubTypes().stream().map(contributionSubTypeTransform::transform).toList());
        }

        return officer;
    }

    private static void handleRolesWithPre1992Appointment(OfficersItem source, Data officer,
                                                          String officerRole, LocalDate appointmentDate) {
        if (RolesWithPre1992Appointment.includes(officerRole)) {
            officer.setIsPre1992Appointment(parseYesOrNo(source.getApptDatePrefix()));
            if (Boolean.TRUE.equals(officer.getIsPre1992Appointment())) {
                officer.setAppointedOn(null);
                officer.setAppointedBefore(appointmentDate);
            } else {
                officer.setAppointedOn(appointmentDate);
            }
        } else {
            officer.setIsPre1992Appointment(false);
            officer.setAppointedOn(appointmentDate);
        }
    }

    private void handleManagingOfficerFields(OfficersItem source, Data officer,
            String officerRole) {
        if (officerRole.contains(OfficerRole.MANOFF.getValue())) {
            officer.setResponsibilities(source.getResponsibilities());
        }

        if (OfficerRole.MANOFFCORP.getValue().equals(officerRole)) {
            officer.setPrincipalOfficeAddress(
                    principalOfficeAddressTransform.transform(source.getPrincipalOfficeAddress()));
            officer.setContactDetails(source.getContactDetails());
        }
    }
}


