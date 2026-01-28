package uk.gov.companieshouse.officer.delta.processor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import uk.gov.companieshouse.api.appointment.Identification;

/**
 * The type Delta identification.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeltaIdentification {

    @JsonProperty("EEA")
    private Identification eea;

    @JsonProperty("other_corporate_body_or_firm")
    private Identification otherCorporateBodyOrFirm;

    @JsonProperty("non_EEA")
    private Identification nonEeaApi;

    @JsonProperty("UK_limited_company")
    private Identification ukLimitedCompany;

    @JsonProperty("registered_overseas_entity_corporate_managing_officer")
    private Identification registeredOverseasEntityCorporateManagingOfficer;

    @JsonProperty("limited_partnership_corporate_partner")
    private Identification limitedPartnershipCorporatePartner;

    /**
     * Gets eea.
     *
     * @return the eea
     */
    public Identification getEea() {
        return eea;
    }

    /**
     * Sets eea.
     *
     * @param eea the eea
     */
    public void setEea(Identification eea) {
        this.eea = eea;
    }

    /**
     * Gets other corporate body or firm.
     *
     * @return the other corporate body or firm
     */
    public Identification getOtherCorporateBodyOrFirm() {
        return otherCorporateBodyOrFirm;
    }

    /**
     * Sets other corporate body or firm.
     *
     * @param otherCorporateBodyOrFirm the other corporate body or firm
     */
    public void setOtherCorporateBodyOrFirm(Identification otherCorporateBodyOrFirm) {
        this.otherCorporateBodyOrFirm = otherCorporateBodyOrFirm;
    }

    /**
     * Gets non eea api.
     *
     * @return the non eea api
     */
    public Identification getNonEeaApi() {
        return nonEeaApi;
    }

    /**
     * Sets non eea api.
     *
     * @param nonEeaApi the non eea api
     */
    public void setNonEeaApi(Identification nonEeaApi) {
        this.nonEeaApi = nonEeaApi;
    }

    /**
     * Gets uk limited company.
     *
     * @return the uk limited company
     */
    public Identification getUkLimitedCompany() {
        return ukLimitedCompany;
    }

    /**
     * Sets uk limited company.
     *
     * @param ukLimitedCompany the uk limited company
     */
    public void setUkLimitedCompany(Identification ukLimitedCompany) {
        this.ukLimitedCompany = ukLimitedCompany;
    }

    /**
     * Gets registered overseas entity corporate managing officer.
     *
     * @return the registered overseas entity corporate managing officer
     */
    public Identification getRegisteredOverseasEntityCorporateManagingOfficer() {
        return registeredOverseasEntityCorporateManagingOfficer;
    }

    /**
     * Sets registered overseas entity corporate managing officer.
     *
     * @param registeredOverseasEntityCorporateManagingOfficer the registered overseas entity
     *                                                         corporate managing officer
     */
    public void setRegisteredOverseasEntityCorporateManagingOfficer(
            Identification registeredOverseasEntityCorporateManagingOfficer) {
        this.registeredOverseasEntityCorporateManagingOfficer =
                registeredOverseasEntityCorporateManagingOfficer;
    }

    /**
     * Gets limited partnership corporate partner identification details.
     *
     * @return the limited partnership corporate partner identification details
     */
    public Identification getLimitedPartnershipCorporatePartner() {
        return limitedPartnershipCorporatePartner;
    }

    /**
     * Sets limited partnership corporate partner identification details.
     *
     * @param limitedPartnershipCorporatePartner the limited partnership corporate
     *                                           partner identification details
     */
    public void setLimitedPartnershipCorporatePartner(
            Identification limitedPartnershipCorporatePartner) {
        this.limitedPartnershipCorporatePartner = limitedPartnershipCorporatePartner;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DeltaIdentification that = (DeltaIdentification) obj;
        return Objects.equals(eea, that.eea) && Objects.equals(otherCorporateBodyOrFirm,
                that.otherCorporateBodyOrFirm) && Objects.equals(nonEeaApi, that.nonEeaApi)
                && Objects.equals(ukLimitedCompany, that.ukLimitedCompany) && Objects.equals(
                registeredOverseasEntityCorporateManagingOfficer,
                that.registeredOverseasEntityCorporateManagingOfficer)
                && Objects.equals(limitedPartnershipCorporatePartner,
                that.limitedPartnershipCorporatePartner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eea, otherCorporateBodyOrFirm, nonEeaApi, ukLimitedCompany,
                registeredOverseasEntityCorporateManagingOfficer, limitedPartnershipCorporatePartner);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
