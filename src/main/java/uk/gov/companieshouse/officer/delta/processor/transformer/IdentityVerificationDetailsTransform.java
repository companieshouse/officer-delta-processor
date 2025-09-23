package uk.gov.companieshouse.officer.delta.processor.transformer;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.appointment.IdentityVerificationDetails;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.model.DeltaIdentityVerificationDetails;

import java.util.function.Consumer;

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
        if (source == null) return output;

        setIfNotNull(output::setAntiMoneyLaunderingSupervisoryBodies, source.getAntiMoneyLaunderingSupervisoryBodies());

        if (source.getAppointmentVerificationEndOn() != null) {
            setIfNotNull(output::setAppointmentVerificationEndOn,
                    parseLocalDate("appointment_verification_end_on", source.getAppointmentVerificationEndOn()));
        }

        if (source.getAppointmentVerificationStatementDate() != null) {
            setIfNotNull(output::setAppointmentVerificationStatementDate,
                    parseLocalDate("appointment_verification_statement_date", source.getAppointmentVerificationStatementDate()));
        }

        if (source.getAppointmentVerificationStatementDueOn() != null) {
            setIfNotNull(output::setAppointmentVerificationStatementDueOn,
                    parseLocalDate("appointment_verification_statement_due_on", source.getAppointmentVerificationStatementDueOn()));
        }

        if (source.getAppointmentVerificationStartOn() != null) {
            setIfNotNull(output::setAppointmentVerificationStartOn,
                    parseLocalDate("appointment_verification_start_on", source.getAppointmentVerificationStartOn()));
        }

        setIfNotNull(output::setAuthorisedCorporateServiceProviderName, source.getAuthorisedCorporateServiceProviderName());

        if (source.getIdentityVerifiedOn() != null) {
            setIfNotNull(output::setIdentityVerifiedOn,
                    parseLocalDate("identity_verified_on", source.getIdentityVerifiedOn()));
        }

        setIfNotNull(output::setPreferredName, source.getPreferredName());

        return output;
    }

    private <T> void setIfNotNull(Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
