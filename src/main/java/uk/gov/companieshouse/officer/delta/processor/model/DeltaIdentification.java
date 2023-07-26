package uk.gov.companieshouse.officer.delta.processor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import uk.gov.companieshouse.api.appointment.Identification;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeltaIdentification {

    @JsonProperty("EEA")
    private Identification eea;

    @JsonProperty("other_corporate_body_or_firm")
    private Identification otherCorporateBodyOrFirm;

    @JsonProperty("non_EEA")
    private Identification nonEeaApi;

    @JsonProperty("UK_limited_company")
    private Identification ukLimitedCompany;

    @JsonProperty("registered_overseas_entity_corporate_managing_officer")
    private Identification registeredOverseasEntityCorporateManagingOfficer;

    public Identification getEea() {
        return eea;
    }

    public void setEea(Identification eea) {
        this.eea = eea;
    }

    public Identification getOtherCorporateBodyOrFirm() {
        return otherCorporateBodyOrFirm;
    }

    public void setOtherCorporateBodyOrFirm(
            Identification otherCorporateBodyOrFirm) {
        this.otherCorporateBodyOrFirm = otherCorporateBodyOrFirm;
    }

    public Identification getNonEeaApi() {
        return nonEeaApi;
    }

    public void setNonEeaApi(Identification nonEeaApi) {
        this.nonEeaApi = nonEeaApi;
    }

    public Identification getUKLimitedCompany() {
        return ukLimitedCompany;
    }

    public void setUKLimitedCompany(Identification ukLimitedCompany) {
        this.ukLimitedCompany = ukLimitedCompany;
    }

    public Identification getRegisteredOverseasEntityCorporateManagingOfficer() {
        return registeredOverseasEntityCorporateManagingOfficer;
    }

    public void setRegisteredOverseasEntityCorporateManagingOfficer(
            Identification registeredOverseasEntityCorporateManagingOfficer) {
        this.registeredOverseasEntityCorporateManagingOfficer = registeredOverseasEntityCorporateManagingOfficer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeltaIdentification that = (DeltaIdentification) o;
        return Objects.equals(eea, that.eea) && Objects.equals(otherCorporateBodyOrFirm,
                that.otherCorporateBodyOrFirm) && Objects.equals(nonEeaApi, that.nonEeaApi)
                && Objects.equals(ukLimitedCompany, that.ukLimitedCompany) && Objects.equals(
                registeredOverseasEntityCorporateManagingOfficer,
                that.registeredOverseasEntityCorporateManagingOfficer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eea, otherCorporateBodyOrFirm, nonEeaApi, ukLimitedCompany,
                registeredOverseasEntityCorporateManagingOfficer);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
