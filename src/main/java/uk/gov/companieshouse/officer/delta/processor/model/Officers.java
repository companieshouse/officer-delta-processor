package uk.gov.companieshouse.officer.delta.processor.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Officers {

    @JsonProperty(value = "CreatedTime", required = true)
    private String createdTime;

    @JsonProperty(value = "delta_at")
    private String deltaAt;

    @JsonProperty(value = "officers", required = true)
    private List<OfficersItem> officers;

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

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setDeltaAt(String deltaAt) {
        this.deltaAt = deltaAt;
    }

    public String getDeltaAt() {
        return deltaAt;
    }

    public void setOfficers(List<OfficersItem> officers) {
        this.officers = officers;
    }

    public List<OfficersItem> getOfficers() {
        return officers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Officers that = (Officers) o;
        return Objects.equals(getCreatedTime(), that.getCreatedTime()) && Objects.equals(getDeltaAt(),
                that.getDeltaAt()) && Objects.equals(getOfficers(), that.getOfficers());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCreatedTime(), getDeltaAt(), getOfficers());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
