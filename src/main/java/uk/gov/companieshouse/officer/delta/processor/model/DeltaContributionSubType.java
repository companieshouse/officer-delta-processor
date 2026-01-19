package uk.gov.companieshouse.officer.delta.processor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeltaContributionSubType {

    @JsonProperty("sub_type")
    private String subType;

    public DeltaContributionSubType() {
    }

    public DeltaContributionSubType(String subType) {
        this.subType = subType;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DeltaContributionSubType that = (DeltaContributionSubType) obj;
        return Objects.equals(getSubType(), that.getSubType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSubType());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
