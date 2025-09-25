package uk.gov.companieshouse.officer.delta.processor.transformer;

import static uk.gov.companieshouse.officer.delta.processor.transformer.TransformerUtils.parseLocalDate;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.IdentityVerificationDetails;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.model.DeltaIdentityVerificationDetails;

@Component
public class IdentityVerificationDetailsTransform implements Transformative<DeltaIdentityVerificationDetails, IdentityVerificationDetails>{
    private static final String IDENTITY_VERIFIED_ON = "identity_verified_on";
    private static final String APPOINTMENT_VERIFICATION_START_ON = "appointment_verification_start_on";
    private static final String APPOINTMENT_VERIFICATION_STATEMENT_DUE_ON = "appointment_verification_statement_due_on";
    private static final String APPOINTMENT_VERIFICATION_STATEMENT_DATE = "appointment_verification_statement_date";
    private static final String APPOINTMENT_VERIFICATION_END_ON = "appointment_verification_end_on";

    @Override
    public IdentityVerificationDetails factory() {
        return new IdentityVerificationDetails();
    }

    @Override
    public IdentityVerificationDetails transform(DeltaIdentityVerificationDetails source, IdentityVerificationDetails output)
            throws NonRetryableErrorException {
        if (source == null) return output;

        Optional<String> appointmentVerificationEndOn = Optional.ofNullable(source.getAppointmentVerificationEndOn());
        Optional<String> appointmentVerificationStatementDate = Optional.ofNullable(source.getAppointmentVerificationStatementDate());
        Optional<String> appointmentVerificationStatementDueOn = Optional.ofNullable(source.getAppointmentVerificationStatementDueOn());
        Optional<String> appointmentVerificationStartOn = Optional.ofNullable(source.getAppointmentVerificationStartOn());
        Optional<String> identityVerifiedOn = Optional.ofNullable(source.getIdentityVerifiedOn());

        appointmentVerificationEndOn.ifPresent(dateString -> output.setAppointmentVerificationEndOn(parseLocalDate(APPOINTMENT_VERIFICATION_END_ON, dateString)));
        appointmentVerificationStatementDate.ifPresent(dateString -> output.setAppointmentVerificationStatementDate(parseLocalDate(APPOINTMENT_VERIFICATION_STATEMENT_DATE, dateString)));
        appointmentVerificationStatementDueOn.ifPresent(dateString -> output.setAppointmentVerificationStatementDueOn(parseLocalDate(APPOINTMENT_VERIFICATION_STATEMENT_DUE_ON, dateString)));
        appointmentVerificationStartOn.ifPresent(dateString -> output.setAppointmentVerificationStartOn(parseLocalDate(APPOINTMENT_VERIFICATION_START_ON, dateString)));
        identityVerifiedOn.ifPresent(dateString -> output.setIdentityVerifiedOn(parseLocalDate(IDENTITY_VERIFIED_ON, dateString)));

        output.setAuthorisedCorporateServiceProviderName(source.getAuthorisedCorporateServiceProviderName());
        output.setAntiMoneyLaunderingSupervisoryBodies(source.getAntiMoneyLaunderingSupervisoryBodies());
        output.setPreferredName(source.getPreferredName());

        return output;
    }
}
