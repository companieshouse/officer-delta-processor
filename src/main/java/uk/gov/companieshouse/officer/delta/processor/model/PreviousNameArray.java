package uk.gov.companieshouse.officer.delta.processor.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreviousNameArray {

    @JsonProperty("previous_forename")
    private String previousForename;

    @JsonProperty("previous_timestamp")
    private String previousTimestamp;

    @JsonProperty("previous_surname")
    private String previousSurname;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    public PreviousNameArray() {
    }

    public PreviousNameArray(String previousForename, String previousSurname) {
        this.previousForename = previousForename;
        this.previousSurname = previousSurname;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public String getPreviousForename() {
        return previousForename;
    }

    public void setPreviousForename(String previousForename) {
        this.previousForename = previousForename;
    }

    public String getPreviousTimestamp() {
        return previousTimestamp;
    }

    public void setPreviousTimestamp(String previousTimestamp) {
        this.previousTimestamp = previousTimestamp;
    }

    public String getPreviousSurname() {
        return previousSurname;
    }

    public void setPreviousSurname(String previousSurname) {
        this.previousSurname = previousSurname;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PreviousNameArray that = (PreviousNameArray) obj;
        return Objects.equals(getPreviousForename(), that.getPreviousForename()) && Objects.equals(
                getPreviousTimestamp(), that.getPreviousTimestamp()) && Objects.equals(
                getPreviousSurname(), that.getPreviousSurname());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPreviousForename(), getPreviousTimestamp(), getPreviousSurname());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
