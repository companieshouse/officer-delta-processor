package uk.gov.companieshouse.officer.delta.processor.tranformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.parseLocalDateTime;

import uk.gov.companieshouse.api.appointment.ExternalData;
import uk.gov.companieshouse.api.appointment.FullRecordCompanyOfficerApi;
import uk.gov.companieshouse.api.appointment.InternalData;
import uk.gov.companieshouse.officer.delta.processor.config.OfficerRoleConfig;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;

@Component
public class AppointmentTransform implements Transformative<OfficersItem, FullRecordCompanyOfficerApi> {
    OfficerTransform officerTransform;
    SensitiveOfficerTransform sensitiveOfficerTransform;
    OfficerRoleConfig officerRoleConfig;

    @Autowired
    public AppointmentTransform(OfficerTransform officerTransform, 
            SensitiveOfficerTransform sensitiveOfficerTransform,
            OfficerRoleConfig officerRoleConfig) {
        this.officerTransform = officerTransform;
        this.sensitiveOfficerTransform = sensitiveOfficerTransform;
        this.officerRoleConfig = officerRoleConfig;
    }

    @Override
    public FullRecordCompanyOfficerApi factory() {
        return new FullRecordCompanyOfficerApi();
    }

    @Override
    public FullRecordCompanyOfficerApi transform(OfficersItem inputOfficer, FullRecordCompanyOfficerApi outputAppointment)
            throws NonRetryableErrorException {

        ExternalData externalData = new ExternalData();
        InternalData internalData = new InternalData();

        externalData.setInternalId(inputOfficer.getInternalId());

        final String encodedInternalId = TransformerUtils.encode(inputOfficer.getInternalId());
        externalData.setAppointmentId(encodedInternalId);

        final String encodedOfficerId = TransformerUtils.encode(inputOfficer.getOfficerId());
        externalData.setOfficerId(encodedOfficerId);

        final String encodedPreviousOfficerId = TransformerUtils.encode(inputOfficer.getPreviousOfficerId());
        externalData.setPreviousOfficerId(encodedPreviousOfficerId);

        externalData.setCompanyNumber(inputOfficer.getCompanyNumber());

        externalData.setData(officerTransform.transform(inputOfficer));
        externalData.setSensitiveData(sensitiveOfficerTransform.transform(inputOfficer));

        internalData.setOfficerRoleSortOrder(getOfficerSortOrder(outputAppointment, externalData));
        //UpdatedAt is in LocalDate format, should it be in LocalDateTime?
        internalData.setUpdatedAt(parseLocalDateTime("changedAt", inputOfficer.getChangedAt()));

        outputAppointment.setExternalData(externalData);
        outputAppointment.setInternalData(internalData);

        return outputAppointment;
    }

    private int getOfficerSortOrder(FullRecordCompanyOfficerApi outputAppointment, ExternalData externalData) {
        outputAppointment.setExternalData(externalData);

        String officerRole = outputAppointment.getExternalData().getData().getOfficerRole().toString();
        Integer order = outputAppointment.getExternalData().getData().getResignedOn() == null ?
                officerRoleConfig.getNonResigned().get(officerRole) : 
                officerRoleConfig.getResigned().get(officerRole);
        return order.intValue();
    }
}
