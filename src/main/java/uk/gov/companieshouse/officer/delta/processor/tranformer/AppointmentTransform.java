package uk.gov.companieshouse.officer.delta.processor.tranformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.delta.officers.AppointmentAPI;
import uk.gov.companieshouse.officer.delta.processor.model.OfficersItem;

@Component
public class AppointmentTransform implements Transformative<OfficersItem, AppointmentAPI> {
    OfficerTransform officerTransform;

    @Autowired
    public AppointmentTransform(OfficerTransform officerTransform) {
        this.officerTransform = officerTransform;
    }

    @Override
    public AppointmentAPI factory() {
        return new AppointmentAPI();
    }

    @Override
    public AppointmentAPI transform(OfficersItem inputOfficer, AppointmentAPI outputAppointment) {

        outputAppointment.setInternalId(inputOfficer.getInternalId());

        final String encodedInternalId = TransformerUtils.encode(inputOfficer.getInternalId());
        outputAppointment.setId(encodedInternalId);
        outputAppointment.setAppointmentId(encodedInternalId);

        final String encodedOfficerId = TransformerUtils.encode(inputOfficer.getOfficerId());
        outputAppointment.setOfficerId(encodedOfficerId);

        final String encodedPreviousOfficerId = TransformerUtils.encode(inputOfficer.getPreviousOfficerId());
        outputAppointment.setPreviousOfficerId(encodedPreviousOfficerId);

        outputAppointment.setData(officerTransform.transform(inputOfficer));

        return outputAppointment;
    }
}
