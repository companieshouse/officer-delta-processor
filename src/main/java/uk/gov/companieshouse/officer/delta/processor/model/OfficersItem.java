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
import uk.gov.companieshouse.api.appointment.ContactDetails;
import uk.gov.companieshouse.api.model.delta.officers.AddressAPI;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OfficersItem {
    @JsonProperty("company_number")
    private String companyNumber;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("status")
    private String companyStatus;

    @JsonProperty("occupation")
    private String occupation;

    @JsonProperty("officer_role")
    private String officerRole;

    @JsonProperty("internal_id")
    private String internalId;

    @JsonProperty("kind")
    private String kind;

    @JsonProperty("date_of_birth")
    private String dateOfBirth;

    @JsonProperty(value = "service_address_same_as_registered_address", required = true)
    private String serviceAddressSameAsRegisteredAddress;

    @JsonProperty(value = "residential_address_same_as_service_address", required = true)
    private String residentialAddressSameAsServiceAddress;

    @JsonProperty(value = "appointment_date", required = true)
    private String appointmentDate;

    @JsonProperty(value = "appt_date_prefix")
    private String apptDatePrefix;

    @JsonProperty(value = "resignation_date")
    private String resignationDate;

    @JsonProperty(value = "officer_detail_id", required = true)
    private String officerDetailId;

    @JsonProperty("changed_at")
    private String changedAt;

    @JsonProperty("title")
    private String title;

    @JsonProperty("honours")
    private String honours;

    @JsonProperty("middle_name")
    private String middleName;

    @JsonProperty(value = "corporate_ind", required = true)
    private String corporateInd;

    @JsonProperty("service_address")
    private AddressAPI serviceAddress;

    @JsonProperty("usual_residential_address")
    private AddressAPI usualResidentialAddress;

    @JsonProperty("principal_office_address")
    private AddressAPI principalOfficeAddress;

    @JsonProperty("responsibilities")
   private String responsibilities;

    @JsonProperty("contact_details")
    private ContactDetails contactDetails;

    @JsonProperty("forename")
    private String forename;

    @JsonProperty(value = "officer_id", required = true)
    private String officerId;

    @JsonProperty("previous_officer_id")
    private String previousOfficerId;

    @JsonProperty("identification")
    private DeltaIdentification identification;

    @JsonProperty("nationality")
    private String nationality;

    @JsonProperty("surname")
    private String surname;

    @JsonProperty("person_number")
    private String personNumber;

    @JsonProperty(value = "secure_director", required = true)
    private String secureDirector;

    @JsonProperty("previous_name_array")
    private List<PreviousNameArray> previousNameArray;

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

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOfficerRole(String officerRole) {
        this.officerRole = officerRole;
    }

    public String getOfficerRole() {
        return officerRole;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public String getInternalId() {
        return internalId;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getKind() {
        return kind;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setServiceAddressSameAsRegisteredAddress(String serviceAddressSameAsRegisteredAddress) {
        this.serviceAddressSameAsRegisteredAddress = serviceAddressSameAsRegisteredAddress;
    }

    public String getServiceAddressSameAsRegisteredAddress() {
        return serviceAddressSameAsRegisteredAddress;
    }

    public String getResidentialAddressSameAsServiceAddress() {
        return residentialAddressSameAsServiceAddress;
    }

    public void setResidentialAddressSameAsServiceAddress(
        final String residentialAddressSameAsServiceAddress) {
        this.residentialAddressSameAsServiceAddress = residentialAddressSameAsServiceAddress;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public String getApptDatePrefix() {
        return apptDatePrefix;
    }

    public void setApptDatePrefix(String apptDatePrefix) {
        this.apptDatePrefix = apptDatePrefix;
    }

    public String getResignationDate() {
        return resignationDate;
    }

    public void setResignationDate(final String resignationDate) {
        this.resignationDate = resignationDate;
    }

    public void setOfficerDetailId(String officerDetailId) {
        this.officerDetailId = officerDetailId;
    }

    public String getOfficerDetailId() {
        return officerDetailId;
    }

    public void setChangedAt(String changedAt) {
        this.changedAt = changedAt;
    }

    public String getChangedAt() {
        return changedAt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setHonours(String honours) {
        this.honours = honours;
    }

    public String getHonours() {
        return honours;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setCorporateInd(String corporateInd) {
        this.corporateInd = corporateInd;
    }

    public String getCorporateInd() {
        return corporateInd;
    }

    public void setServiceAddress(AddressAPI serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public AddressAPI getServiceAddress() {
        return serviceAddress;
    }

    public void setUsualResidentialAddress(AddressAPI usualResidentialAddress) {
        this.usualResidentialAddress = usualResidentialAddress;
    }

    public AddressAPI getUsualResidentialAddress() {
        return usualResidentialAddress;
    }

    public void setPrincipalOfficeAddress(AddressAPI principalOfficeAddress) {
        this.principalOfficeAddress = principalOfficeAddress;
    }

    public AddressAPI getPrincipalOfficeAddress() {
        return principalOfficeAddress;
    }

    public void setResponsibilities(String responsibilities) {
        this.responsibilities = responsibilities;
    }

    public String getResponsibilities() {
        return responsibilities;
    }

    public void setContactDetails(ContactDetails contactDetails) {
        this.contactDetails = contactDetails;
    }

    public ContactDetails getContactDetails() {
        return contactDetails;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public String getForename() {
        return forename;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setOfficerId(String officerId) {
        this.officerId = officerId;
    }

    public String getOfficerId() {
        return officerId;
    }

    public String getPreviousOfficerId() {
        return previousOfficerId;
    }

    public void setPreviousOfficerId(String previousOfficerId) {
        this.previousOfficerId = previousOfficerId;
    }

    public void setIdentification(DeltaIdentification identification) {
        this.identification = identification;
    }

    public DeltaIdentification getIdentification() {
        return identification;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getNationality() {
        return nationality;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getSurname() {
        return surname;
    }

    public void setSecureDirector(String secureDirector) {
        this.secureDirector = secureDirector;
    }

    public String getSecureDirector() {
        return secureDirector;
    }

    public List<PreviousNameArray> getPreviousNameArray() {
        return previousNameArray;
    }

    public void setPreviousNameArray(final List<PreviousNameArray> previousNameArray) {
        this.previousNameArray = previousNameArray;
    }

    public String getPersonNumber() { return personNumber; }

    public void setPersonNumber(String personNumber) { this.personNumber = personNumber; }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyStatus() {
        return companyStatus;
    }

    public void setCompanyStatus(String companyStatus) {
        this.companyStatus = companyStatus;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final OfficersItem that = (OfficersItem) o;
        return Objects.equals(getCompanyNumber(), that.getCompanyNumber())
                && Objects.equals(getOccupation(),
                that.getOccupation())
                && Objects.equals(getOfficerRole(), that.getOfficerRole())
                && Objects.equals(getInternalId(),
                that.getInternalId())
                && Objects.equals(getKind(), that.getKind())
                && Objects.equals(getDateOfBirth(),
                that.getDateOfBirth())
                && Objects.equals(getServiceAddressSameAsRegisteredAddress(),
                that.getServiceAddressSameAsRegisteredAddress())
               && Objects.equals(getResidentialAddressSameAsServiceAddress(),
            that.getResidentialAddressSameAsServiceAddress())
                && Objects.equals(getAppointmentDate(),
                that.getAppointmentDate())
                && Objects.equals(getApptDatePrefix(), that.getApptDatePrefix())
                && Objects.equals(getResignationDate(), that.getResignationDate())
                && Objects.equals(getOfficerDetailId(), that.getOfficerDetailId())
                && Objects.equals(getChangedAt(), that.getChangedAt())
                && Objects.equals(getTitle(), that.getTitle())
                && Objects.equals(getHonours(), that.getHonours())
                && Objects.equals(getMiddleName(), that.getMiddleName())
                && Objects.equals(getCorporateInd(), that.getCorporateInd())
                && Objects.equals(getServiceAddress(), that.getServiceAddress())
                && Objects.equals(getUsualResidentialAddress(), that.getUsualResidentialAddress())
                && Objects.equals(getPrincipalOfficeAddress(), that.getPrincipalOfficeAddress())
                && Objects.equals(getResponsibilities(), that.getResponsibilities())
                && Objects.equals(getContactDetails(), that.getContactDetails())
                && Objects.equals(getForename(), that.getForename())
                && Objects.equals(getOfficerId(), that.getOfficerId())
                && Objects.equals(getPreviousOfficerId(), that.getPreviousOfficerId())
                && Objects.equals(getIdentification(), that.getIdentification())
                && Objects.equals(getNationality(), that.getNationality())
                && Objects.equals(getSurname(), that.getSurname())
                && Objects.equals(getPersonNumber(), that.getPersonNumber())
                && Objects.equals(getSecureDirector(), that.getSecureDirector())
                && Objects.equals(getPreviousNameArray(), that.getPreviousNameArray())
                && Objects.equals(getAdditionalProperties(), that.getAdditionalProperties())
                && Objects.equals(getCompanyName(), that.getCompanyName())
                && Objects.equals(getCompanyStatus(), that.getCompanyStatus());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCompanyNumber(),
                getOccupation(),
                getOfficerRole(),
                getInternalId(),
                getKind(),
                getDateOfBirth(),
                getServiceAddressSameAsRegisteredAddress(),
                getResidentialAddressSameAsServiceAddress(),
                getAppointmentDate(),
                getApptDatePrefix(),
                getResignationDate(),
                getOfficerDetailId(),
                getChangedAt(),
                getTitle(),
                getHonours(),
                getMiddleName(),
                getCorporateInd(),
                getServiceAddress(),
                getUsualResidentialAddress(),
                getPrincipalOfficeAddress(),
                getResponsibilities(),
                getContactDetails(),
                getForename(),
                getOfficerId(),
                getPreviousOfficerId(),
                getIdentification(),
                getNationality(),
                getSurname(),
                getPersonNumber(),
                getSecureDirector(),
                getPreviousNameArray(),
                getAdditionalProperties(),
                getCompanyName(),
                getCompanyStatus());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
