package uk.gov.companieshouse.officer.delta.processor.tranformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.IdentityVerificationDetails;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.model.DeltaIdentityVerificationDetails;

import java.time.LocalDate;

@Component
public class IdentityVerificationDetailsTransform implements Transformative<DeltaIdentityVerificationDetails, IdentityVerificationDetails>{
    @Override
    public IdentityVerificationDetails factory() {
        return new IdentityVerificationDetails();
    }

    @Override
    public IdentityVerificationDetails transform(DeltaIdentityVerificationDetails source, IdentityVerificationDetails output)
            throws NonRetryableErrorException {
        output.setAntiMoneyLaunderingSupervisoryBodies(source.getAntiMoneyLaunderingSupervisoryBodies());
        output.setAppointmentVerificationEndOn(LocalDate.parse(source.getAppointmentVerificationEndOn()));
        output.setAppointmentVerificationStatementDate(LocalDate.parse(source.getAppointmentVerificationStatementDate()));
        output.setAppointmentVerificationStatementDueOn(LocalDate.parse(source.getAppointmentVerificationStatementDueOn()));
        output.setAppointmentVerificationStartOn(LocalDate.parse(source.getAppointmentVerificationStartOn()));
        output.setAuthorisedCorporateServiceProviderName(source.getAuthorisedCorporateServiceProviderName());
        output.setIdentityVerifiedOn(LocalDate.parse(source.getIdentityVerifiedOn()));
        output.setPreferredName(source.getPreferredName());
        return output;
    }
}
