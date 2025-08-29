package uk.gov.companieshouse.officer.delta.processor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;
import java.util.Objects;

/**
 * The type Delta identity verification details.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeltaIdentityVerificationDetails {

    @JsonProperty("anti_money_laundering_supervisory_bodies")
    private List<String> antiMoneyLaunderingSupervisoryBodies;

    @JsonProperty("appointment_verification_end_on")
    private String appointmentVerificationEndOn;

    @JsonProperty("appointment_verification_statement_date")
    private String appointmentVerificationStatementDate;

    @JsonProperty("appointment_verification_statement_due_on")
    private String appointmentVerificationStatementDueOn;

    @JsonProperty("appointment_verification_start_on")
    private String appointmentVerificationStartOn;

    @JsonProperty("authorised_corporate_service_provider_name")
    private String authorisedCorporateServiceProviderName;

    @JsonProperty("identity_verified_on")
    private String identityVerifiedOn;

    @JsonProperty("preferred_name")
    private String preferredName;



    public List<String> getAntiMoneyLaunderingSupervisoryBodies() {
        return antiMoneyLaunderingSupervisoryBodies;
    }

    public void setAntiMoneyLaunderingSupervisoryBodies(List<String> antiMoneyLaunderingSupervisoryBodies) {
        this.antiMoneyLaunderingSupervisoryBodies = antiMoneyLaunderingSupervisoryBodies;
    }

    public String getAppointmentVerificationEndOn() {
        return appointmentVerificationEndOn;
    }

    public void setAppointmentVerificationEndOn(String appointmentVerificationEndOn) {
        this.appointmentVerificationEndOn = appointmentVerificationEndOn;
    }

    public String getAppointmentVerificationStatementDate() {
        return appointmentVerificationStatementDate;
    }

    public void setAppointmentVerificationStatementDate(String appointmentVerificationStatementDate) {
        this.appointmentVerificationStatementDate = appointmentVerificationStatementDate;
    }

    public String getAppointmentVerificationStatementDueOn() {
        return appointmentVerificationStatementDueOn;
    }

    public void setAppointmentVerificationStatementDueOn(String appointmentVerificationStatementDueOn) {
        this.appointmentVerificationStatementDueOn = appointmentVerificationStatementDueOn;
    }

    public String getAppointmentVerificationStartOn() {
        return appointmentVerificationStartOn;
    }

    public void setAppointmentVerificationStartOn(String appointmentVerificationStartOn) {
        this.appointmentVerificationStartOn = appointmentVerificationStartOn;
    }

    public String getAuthorisedCorporateServiceProviderName() {
        return authorisedCorporateServiceProviderName;
    }

    public void setAuthorisedCorporateServiceProviderName(String authorisedCorporateServiceProviderName) {
        this.authorisedCorporateServiceProviderName = authorisedCorporateServiceProviderName;
    }

    public String getIdentityVerifiedOn() {
        return identityVerifiedOn;
    }

    public void setIdentityVerifiedOn(String identityVerifiedOn) {
        this.identityVerifiedOn = identityVerifiedOn;
    }

    public String getPreferredName() {
        return preferredName;
    }

    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final DeltaIdentityVerificationDetails that = (DeltaIdentityVerificationDetails) obj;
        return Objects.equals(
                getAntiMoneyLaunderingSupervisoryBodies(), that.getAntiMoneyLaunderingSupervisoryBodies()) &&
                Objects.equals(getAppointmentVerificationEndOn(), that.getAppointmentVerificationEndOn()) &&
                Objects.equals(
                        getAppointmentVerificationStatementDate(), that.getAppointmentVerificationStatementDate()) &&
                Objects.equals(
                        getAppointmentVerificationStatementDueOn(), that.getAppointmentVerificationStatementDueOn()) &&
                Objects.equals(getAppointmentVerificationStartOn(), that.getAppointmentVerificationStartOn()) &&
                Objects.equals(
                        getAuthorisedCorporateServiceProviderName(), that.getAuthorisedCorporateServiceProviderName()) &&
                Objects.equals(getIdentityVerifiedOn(), that.getIdentityVerifiedOn()) &&
                Objects.equals(getPreferredName(), that.getPreferredName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAntiMoneyLaunderingSupervisoryBodies(), getAppointmentVerificationEndOn(),
                getAppointmentVerificationStatementDate(), getAppointmentVerificationStatementDueOn(),
                getAppointmentVerificationStartOn(), getAuthorisedCorporateServiceProviderName(),
                getIdentityVerifiedOn(), getPreferredName());
    }
}
