package uk.gov.companieshouse.officer.delta.processor.transformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.IdentityVerificationDetails;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.model.DeltaIdentityVerificationDetails;

import static uk.gov.companieshouse.officer.delta.processor.transformer.TransformerUtils.parseLocalDate;

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
        output.setAppointmentVerificationEndOn(parseLocalDate("appointment_verification_end_on",
                source.getAppointmentVerificationEndOn()));
        output.setAppointmentVerificationStatementDate(parseLocalDate(
                "appointment_verification_statement_date", source.getAppointmentVerificationStatementDate()));
        output.setAppointmentVerificationStatementDueOn(parseLocalDate(
                "appointment_verification_statement_due_on", source.getAppointmentVerificationStatementDueOn()));
        output.setAppointmentVerificationStartOn(parseLocalDate("appointment_verification_start_on",
                source.getAppointmentVerificationStartOn()));
        output.setAuthorisedCorporateServiceProviderName(source.getAuthorisedCorporateServiceProviderName());
        output.setIdentityVerifiedOn(parseLocalDate("identity_verified_on", source.getIdentityVerifiedOn()));
        output.setPreferredName(source.getPreferredName());
        return output;
    }
}
