package uk.gov.companieshouse.officer.delta.processor.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Officers {

    @JsonProperty(value = "CreatedTime", required = true)
    private String createdTime;

    @JsonProperty(value = "delta_at")
    private String deltaAt;

    @JsonProperty(value = "officers", required = true)
    private List<OfficersItem> officerList;

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

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getDeltaAt() {
        return deltaAt;
    }

    public void setDeltaAt(String deltaAt) {
        this.deltaAt = deltaAt;
    }

    public List<OfficersItem> getOfficers() {
        return officerList;
    }

    public void setOfficers(List<OfficersItem> officers) {
        this.officerList = officers;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Officers that = (Officers) obj;
        return Objects.equals(getCreatedTime(), that.getCreatedTime()) && Objects.equals(
                getDeltaAt(), that.getDeltaAt()) && Objects.equals(getOfficers(),
                that.getOfficers());
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
