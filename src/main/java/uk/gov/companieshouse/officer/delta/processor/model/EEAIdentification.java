package uk.gov.companieshouse.officer.delta.processor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EEAIdentification {
    @JsonProperty("legal_authority")
    private String legalAuthority;

    @JsonProperty("registration_number")
    private String registrationNumber;

    @JsonProperty("place_registered")
    private String placeRegistered;

    @JsonProperty("legal_form")
    private String legalForm;

    public void setLegalAuthority(String legalAuthority) {
        this.legalAuthority = legalAuthority;
    }

    public String getLegalAuthority() {
        return legalAuthority;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setPlaceRegistered(String placeRegistered) {
        this.placeRegistered = placeRegistered;
    }

    public String getPlaceRegistered() {
        return placeRegistered;
    }

    public void setLegalForm(String legalForm) {
        this.legalForm = legalForm;
    }

    public String getLegalForm() {
        return legalForm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EEAIdentification that = (EEAIdentification) o;
        return Objects.equals(getLegalAuthority(), that.getLegalAuthority())
                && Objects.equals(getRegistrationNumber(),
                that.getRegistrationNumber())
                && Objects.equals(getPlaceRegistered(), that.getPlaceRegistered())
                && Objects.equals(getLegalForm(), that.getLegalForm());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLegalAuthority(), getRegistrationNumber(), getPlaceRegistered(), getLegalForm());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
