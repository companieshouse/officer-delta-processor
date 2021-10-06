package uk.gov.companieshouse.officer.delta.processor.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import uk.gov.companieshouse.api.model.delta.officers.IdentificationAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Identification {

    @JsonProperty("EEA")
    private IdentificationAPI eea;

    @JsonProperty("other_corporate_body_or_firm")
    private IdentificationAPI otherCorporateBodyOrFirm;

    @JsonProperty("non_eea")
    private IdentificationAPI nonEeaApi;

    @JsonProperty("UK_limited_company")
    private IdentificationAPI uKLimitedCompany;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public IdentificationAPI getEea() {
        return eea;
    }

    public void setEea(IdentificationAPI eea) {
        this.eea = eea;
    }

    public IdentificationAPI getOtherCorporateBodyOrFirm() {
        return otherCorporateBodyOrFirm;
    }

    public void setOtherCorporateBodyOrFirm(IdentificationAPI otherCorporateBodyOrFirm) {
        this.otherCorporateBodyOrFirm = otherCorporateBodyOrFirm;
    }

    public IdentificationAPI getNonEeaApi() {
        return nonEeaApi;
    }

    public void setNonEeaApi(IdentificationAPI nonEeaApi) {
        this.nonEeaApi = nonEeaApi;
    }

    public IdentificationAPI getUKLimitedCompany() {
        return uKLimitedCompany;
    }

    public void setUKLimitedCompany(IdentificationAPI uKLimitedCompany) {
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
