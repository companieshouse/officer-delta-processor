package uk.gov.companieshouse.officer.delta.processor.model;

import java.util.Objects;

public class DeleteAppointmentParameters {

    private final String encodedInternalId;
    private final String companyNumber;
    private final String deltaAt;
    private final String encodedOfficerId;

    public DeleteAppointmentParameters(String encodedInternalId, String companyNumber, String deltaAt,
            String encodedOfficerId) {
        this.encodedInternalId = encodedInternalId;
        this.companyNumber = companyNumber;
        this.deltaAt = deltaAt;
        this.encodedOfficerId = encodedOfficerId;
    }

    public String getEncodedInternalId() {
        return encodedInternalId;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public String getDeltaAt() {
        return deltaAt;
    }

    public String getEncodedOfficerId() {
        return encodedOfficerId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String encodedInternalId;
        private String companyNumber;
        private String deltaAt;
        private String encodedOfficerId;

        private Builder() {
        }

        public Builder encodedInternalId(String internalId) {
            this.encodedInternalId = internalId;
            return this;
        }

        public Builder companyNumber(String companyNumber) {
            this.companyNumber = companyNumber;
            return this;
        }

        public Builder deltaAt(String deltaAt) {
            this.deltaAt = deltaAt;
            return this;
        }

        public Builder encodedOfficerId(String encodedOfficerId) {
            this.encodedOfficerId = encodedOfficerId;
            return this;
        }

        public DeleteAppointmentParameters build() {
            return new DeleteAppointmentParameters(encodedInternalId, companyNumber, deltaAt, encodedOfficerId);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeleteAppointmentParameters that = (DeleteAppointmentParameters) o;
        return Objects.equals(encodedInternalId, that.encodedInternalId) && Objects.equals(
                companyNumber, that.companyNumber) && Objects.equals(deltaAt, that.deltaAt)
                && Objects.equals(encodedOfficerId, that.encodedOfficerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(encodedInternalId, companyNumber, deltaAt, encodedOfficerId);
    }
}
