package uk.gov.companieshouse.officer.delta.processor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Address {

    @JsonProperty("care_of")
    private String careOf;

    @JsonProperty("country")
    private String country;

    @JsonProperty("usual_country_of_residence")
    private String usualCountryOfResidence;

    @JsonProperty("premise")
    private String premise;

    @JsonProperty("address_line_1")
    private String addressLine1;

    @JsonProperty("locality")
    private String locality;

    @JsonProperty("address_line_2")
    private String addressLine2;

    @JsonProperty("supplied_company_name")
    private String suppliedCompanyName;

    @JsonProperty("region")
    private String region;

    @JsonProperty("po_box")
    private String poBox;

    @JsonProperty("postal_code")
    private String postalCode;

    public void setCareOf(String careOf) {
        this.careOf = careOf;
    }

    public String getCareOf() {
        return careOf;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public void setUsualCountryOfResidence(String usualCountryOfResidence) {
        this.usualCountryOfResidence = usualCountryOfResidence;
    }

    public String getUsualCountryOfResidence() {
        return usualCountryOfResidence;
    }

    public void setPremise(String premise) {
        this.premise = premise;
    }

    public String getPremise() {
        return premise;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getLocality() {
        return locality;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setSuppliedCompanyName(String suppliedCompanyName) {
        this.suppliedCompanyName = suppliedCompanyName;
    }

    public String getSuppliedCompanyName() {
        return suppliedCompanyName;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getRegion() {
        return region;
    }

    public void setPoBox(String poBox) {
        this.poBox = poBox;
    }

    public String getPoBox() {
        return poBox;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPostalCode() {
        return postalCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Address that = (Address) o;
        return Objects.equals(getCareOf(), that.getCareOf())
                && Objects.equals(getCountry(), that.getCountry())
                && Objects.equals(getUsualCountryOfResidence(), that.getUsualCountryOfResidence())
                && Objects.equals(getPremise(), that.getPremise())
                && Objects.equals(getAddressLine1(), that.getAddressLine1())
                && Objects.equals(getLocality(), that.getLocality())
                && Objects.equals(getAddressLine2(), that.getAddressLine2())
                && Objects.equals(getSuppliedCompanyName(), that.getSuppliedCompanyName())
                && Objects.equals(getRegion(), that.getRegion())
                && Objects.equals(getPoBox(), that.getPoBox())
                && Objects.equals(getPostalCode(), that.getPostalCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCareOf(),
                getCountry(),
                getUsualCountryOfResidence(),
                getPremise(),
                getAddressLine1(),
                getLocality(),
                getAddressLine2(),
                getSuppliedCompanyName(),
                getRegion(),
                getPoBox(),
                getPostalCode());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
