package uk.gov.companieshouse.officer.delta.processor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreviousNameArray {

    @JsonProperty("previous_forename")
    private String previousForename;

    @JsonProperty("previous_timestamp")
    private String previousTimestamp;

    @JsonProperty("previous_surname")
    private String previousSurname;

    public void setPreviousForename(String previousForename) {
        this.previousForename = previousForename;
    }

    public String getPreviousForename() {
        return previousForename;
    }

    public void setPreviousTimestamp(String previousTimestamp) {
        this.previousTimestamp = previousTimestamp;
    }

    public String getPreviousTimestamp() {
        return previousTimestamp;
    }

    public void setPreviousSurname(String previousSurname) {
        this.previousSurname = previousSurname;
    }

    public String getPreviousSurname() {
        return previousSurname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PreviousNameArray that = (PreviousNameArray) o;
        return Objects.equals(getPreviousForename(), that.getPreviousForename())
                && Objects.equals(getPreviousTimestamp(), that.getPreviousTimestamp())
                && Objects.equals(getPreviousSurname(), that.getPreviousSurname());
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
