package uk.gov.companieshouse.officer.delta.processor.tranformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.officer.delta.processor.config.OfficerRoleConfig;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;

@Component
public class AppointmentTransform implements Transformative<OfficersItem, AppointmentAPI> {
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
    public AppointmentAPI factory() {
        return new AppointmentAPI();
    }

    @Override
    public AppointmentAPI transform(OfficersItem inputOfficer, AppointmentAPI outputAppointment)
            throws NonRetryableErrorException {

        outputAppointment.setInternalId(inputOfficer.getInternalId());

        final String encodedInternalId = TransformerUtils.encode(inputOfficer.getInternalId());
        outputAppointment.setId(encodedInternalId);
        outputAppointment.setAppointmentId(encodedInternalId);

        final String encodedOfficerId = TransformerUtils.encode(inputOfficer.getOfficerId());
        outputAppointment.setOfficerId(encodedOfficerId);

        final String encodedPreviousOfficerId = TransformerUtils.encode(inputOfficer.getPreviousOfficerId());
        outputAppointment.setPreviousOfficerId(encodedPreviousOfficerId);

        outputAppointment.setCompanyNumber(inputOfficer.getCompanyNumber());

        outputAppointment.setData(officerTransform.transform(inputOfficer));
        outputAppointment.setSensitiveData(sensitiveOfficerTransform.transform(inputOfficer));
        
        outputAppointment.setOfficerRoleSortOrder(getOfficerSortOrder(outputAppointment));
        return outputAppointment;
    }

    private int getOfficerSortOrder(AppointmentAPI outputAppointment){
        String officerRole = outputAppointment.getData().getOfficerRole();
        Integer order = outputAppointment.getData().getResignedOn() == null ? officerRoleConfig.getNonResigned().get(officerRole) : officerRoleConfig.getResigned().get(officerRole);
        return order.intValue();
    }
}
