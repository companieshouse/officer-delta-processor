package uk.gov.companieshouse.officer.delta.processor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Identification {

    @JsonProperty("EEA")
    private uk.gov.companieshouse.api.appointment.Identification eea;

    @JsonProperty("other_corporate_body_or_firm")
    private uk.gov.companieshouse.api.appointment.Identification otherCorporateBodyOrFirm;

    @JsonProperty("non_EEA")
    private uk.gov.companieshouse.api.appointment.Identification nonEeaApi;

    @JsonProperty("UK_limited_company")
    private uk.gov.companieshouse.api.appointment.Identification uKLimitedCompany;

    public uk.gov.companieshouse.api.appointment.Identification getEea() {
        return eea;
    }

    public void setEea(uk.gov.companieshouse.api.appointment.Identification eea) {
        this.eea = eea;
    }

    public uk.gov.companieshouse.api.appointment.Identification getOtherCorporateBodyOrFirm() {
        return otherCorporateBodyOrFirm;
    }

    public void setOtherCorporateBodyOrFirm(uk.gov.companieshouse.api.appointment.Identification otherCorporateBodyOrFirm) {
        this.otherCorporateBodyOrFirm = otherCorporateBodyOrFirm;
    }

    public uk.gov.companieshouse.api.appointment.Identification getNonEeaApi() {
        return nonEeaApi;
    }

    public void setNonEeaApi(uk.gov.companieshouse.api.appointment.Identification nonEeaApi) {
        this.nonEeaApi = nonEeaApi;
    }

    public uk.gov.companieshouse.api.appointment.Identification getUKLimitedCompany() {
        return uKLimitedCompany;
    }

    public void setUKLimitedCompany(uk.gov.companieshouse.api.appointment.Identification uKLimitedCompany) {
        this.uKLimitedCompany = uKLimitedCompany;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Identification that = (Identification) o;
        return Objects.equals(getEea(), that.getEea())
                && Objects.equals(getOtherCorporateBodyOrFirm(),
                that.getOtherCorporateBodyOrFirm())
                && Objects.equals(getNonEeaApi(), that.getNonEeaApi())
                && Objects.equals(getUKLimitedCompany(), that.getUKLimitedCompany());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEea(), getOtherCorporateBodyOrFirm(), getNonEeaApi(), getUKLimitedCompany());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
