package uk.gov.companieshouse.officer.delta.processor.tranformer;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.appointment.IdentityVerificationDetails;
import uk.gov.companieshouse.officer.delta.processor.model.DeltaIdentityVerificationDetails;

import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.companieshouse.officer.delta.processor.tranformer.TransformerUtils.parseLocalDate;

@ExtendWith(MockitoExtension.class)
class IdentityVerificationDetailsTransformTest {

    private IdentityVerificationDetailsTransform testTransform;

    @BeforeEach
    void setUp() { testTransform = new IdentityVerificationDetailsTransform(); }

    @Test
    void factory() {
        assertThat(testTransform.factory(), is(instanceOf(IdentityVerificationDetails.class)));
    }

    @Test
    void transformIdentityVerificationDetails() {
        final DeltaIdentityVerificationDetails identityVerificationDetails = getIdentityVerificationDetails();

        final IdentityVerificationDetails result = testTransform.transform(identityVerificationDetails);

        assertThat(result.getAntiMoneyLaunderingSupervisoryBodies(),
                is(identityVerificationDetails.getAntiMoneyLaunderingSupervisoryBodies()));
        assertThat(result.getAppointmentVerificationEndOn(),
                is(parseLocalDate("appointment_verification_end_on",
                        identityVerificationDetails.getAppointmentVerificationEndOn())));
        assertThat(result.getAppointmentVerificationStatementDate(),
                is(parseLocalDate("appointment_verification_statement_date",
                        identityVerificationDetails.getAppointmentVerificationStatementDate())));
        assertThat(result.getAppointmentVerificationStatementDueOn(),
                is(parseLocalDate("appointment_verification_statement_due_on",
                        identityVerificationDetails.getAppointmentVerificationStatementDueOn())));
        assertThat(result.getAppointmentVerificationStartOn(),
                is(parseLocalDate("appointment_verification_start_on",
                        identityVerificationDetails.getAppointmentVerificationStartOn())));
        assertThat(result.getAuthorisedCorporateServiceProviderName(),
                is(identityVerificationDetails.getAuthorisedCorporateServiceProviderName()));
        assertThat(result.getIdentityVerifiedOn(),
                is(parseLocalDate("identity_verified_on",
                        identityVerificationDetails.getIdentityVerifiedOn())));
        assertThat(result.getPreferredName(),
                is(identityVerificationDetails.getPreferredName()));
    }

    private static @NotNull DeltaIdentityVerificationDetails getIdentityVerificationDetails() {
        final DeltaIdentityVerificationDetails identityVerificationDetails = new DeltaIdentityVerificationDetails();
        identityVerificationDetails.setAntiMoneyLaunderingSupervisoryBodies(List.of("Supervisory Body"));
        identityVerificationDetails.setAppointmentVerificationEndOn("20240110");
        identityVerificationDetails.setAppointmentVerificationStatementDate("20240111");
        identityVerificationDetails.setAppointmentVerificationStatementDueOn("20240112");
        identityVerificationDetails.setAppointmentVerificationStartOn("20240113");
        identityVerificationDetails.setAuthorisedCorporateServiceProviderName("Provider");
        identityVerificationDetails.setIdentityVerifiedOn("20240114");
        identityVerificationDetails.setPreferredName("Preferred Name");
        return identityVerificationDetails;
    }
}
