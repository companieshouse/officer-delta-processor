package uk.gov.companieshouse.officer.delta.processor.model;

import java.util.Objects;

/**
 * The type Delete appointment parameters.
 */
public class DeleteAppointmentParameters {

    private final String encodedInternalId;
    private final String companyNumber;
    private final String deltaAt;
    private final String encodedOfficerId;

    /**
     * Instantiates a new Delete appointment parameters.
     *
     * @param encodedInternalId the encoded internal id
     * @param companyNumber     the company number
     * @param deltaAt           the delta at
     * @param encodedOfficerId  the encoded officer id
     */
    public DeleteAppointmentParameters(String encodedInternalId, String companyNumber,
            String deltaAt, String encodedOfficerId) {
        this.encodedInternalId = encodedInternalId;
        this.companyNumber = companyNumber;
        this.deltaAt = deltaAt;
        this.encodedOfficerId = encodedOfficerId;
    }

    /**
     * Builder builder.
     *
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets encoded internal id.
     *
     * @return the encoded internal id
     */
    public String getEncodedInternalId() {
        return encodedInternalId;
    }

    /**
     * Gets company number.
     *
     * @return the company number
     */
    public String getCompanyNumber() {
        return companyNumber;
    }

    /**
     * Gets delta at.
     *
     * @return the delta at
     */
    public String getDeltaAt() {
        return deltaAt;
    }

    /**
     * Gets encoded officer id.
     *
     * @return the encoded officer id
     */
    public String getEncodedOfficerId() {
        return encodedOfficerId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DeleteAppointmentParameters that = (DeleteAppointmentParameters) obj;
        return Objects.equals(encodedInternalId, that.encodedInternalId) && Objects.equals(
                companyNumber, that.companyNumber) && Objects.equals(deltaAt, that.deltaAt)
                && Objects.equals(encodedOfficerId, that.encodedOfficerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(encodedInternalId, companyNumber, deltaAt, encodedOfficerId);
    }

    /**
     * The type Builder.
     */
    public static class Builder {

        private String encodedInternalId;
        private String companyNumber;
        private String deltaAt;
        private String encodedOfficerId;

        private Builder() {
        }

        /**
         * Encoded internal id builder.
         *
         * @param internalId the internal id
         * @return the builder
         */
        public Builder encodedInternalId(String internalId) {
            this.encodedInternalId = internalId;
            return this;
        }

        /**
         * Company number builder.
         *
         * @param companyNumber the company number
         * @return the builder
         */
        public Builder companyNumber(String companyNumber) {
            this.companyNumber = companyNumber;
            return this;
        }

        /**
         * Delta at builder.
         *
         * @param deltaAt the delta at
         * @return the builder
         */
        public Builder deltaAt(String deltaAt) {
            this.deltaAt = deltaAt;
            return this;
        }

        /**
         * Encoded officer id builder.
         *
         * @param encodedOfficerId the encoded officer id
         * @return the builder
         */
        public Builder encodedOfficerId(String encodedOfficerId) {
            this.encodedOfficerId = encodedOfficerId;
            return this;
        }

        /**
         * Build delete appointment parameters.
         *
         * @return the delete appointment parameters
         */
        public DeleteAppointmentParameters build() {
            return new DeleteAppointmentParameters(encodedInternalId, companyNumber, deltaAt,
                    encodedOfficerId);
        }
    }
}
