package uk.gov.companieshouse.officer.delta.processor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Identification {

    @JsonProperty("EEA")
    private EEAIdentification eEA;

    @JsonProperty("other_corporate_body_or_firm")
    private EEAIdentification otherCorporateBodyOrFirm;

    @JsonProperty("non_eea")
    private EEAIdentification nonEeaApi;

    @JsonProperty("UK_limited_company")
    private EEAIdentification uKLimitedCompany;

    public EEAIdentification geteEA() {
        return eEA;
    }

    public void seteEA(EEAIdentification eEA) {
        this.eEA = eEA;
    }

    public EEAIdentification getOtherCorporateBodyOrFirm() {
        return otherCorporateBodyOrFirm;
    }

    public void setOtherCorporateBodyOrFirm(EEAIdentification otherCorporateBodyOrFirm) {
        this.otherCorporateBodyOrFirm = otherCorporateBodyOrFirm;
    }

    public EEAIdentification getNonEeaApi() {
        return nonEeaApi;
    }

    public void setNonEeaApi(EEAIdentification nonEeaApi) {
        this.nonEeaApi = nonEeaApi;
    }

    public EEAIdentification getuKLimitedCompany() {
        return uKLimitedCompany;
    }

    public void setuKLimitedCompany(EEAIdentification uKLimitedCompany) {
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
        return Objects.equals(geteEA(), that.geteEA())
                && Objects.equals(getOtherCorporateBodyOrFirm(),
                that.getOtherCorporateBodyOrFirm())
                && Objects.equals(getNonEeaApi(), that.getNonEeaApi())
                && Objects.equals(getuKLimitedCompany(), that.getuKLimitedCompany());
    }

    @Override
    public int hashCode() {
        return Objects.hash(geteEA(), getOtherCorporateBodyOrFirm(), getNonEeaApi(), getuKLimitedCompany());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
