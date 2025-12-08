package uk.gov.companieshouse.officer.delta.processor.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class DeltaIdentityVerificationDetailsTest {

    @Test
    void equalsShouldReturnTrueForSameObject() {
        DeltaIdentityVerificationDetails details = new DeltaIdentityVerificationDetails();
        assertEquals(details, details);
    }

    @Test
    void equalsShouldReturnTrueForEqualObjects() {
        DeltaIdentityVerificationDetails details1 = new DeltaIdentityVerificationDetails();
        DeltaIdentityVerificationDetails details2 = new DeltaIdentityVerificationDetails();
        assertEquals(details1, details2);
    }

    @Test
    void equalsShouldReturnFalseForDifferentObjects() {
        DeltaIdentityVerificationDetails details1 = new DeltaIdentityVerificationDetails();
        DeltaIdentityVerificationDetails details2 = new DeltaIdentityVerificationDetails();
        details2.setPreferredName("Different Name");
        assertNotEquals(details1, details2);
    }

    @Test
    void hashCodeShouldBeEqualForEqualObjects() {
        DeltaIdentityVerificationDetails details1 = new DeltaIdentityVerificationDetails();
        DeltaIdentityVerificationDetails details2 = new DeltaIdentityVerificationDetails();
        assertEquals(details1.hashCode(), details2.hashCode());
    }

    @Test
    void hashCodeShouldBeDifferentForDifferentObjects() {
        DeltaIdentityVerificationDetails details1 = new DeltaIdentityVerificationDetails();
        DeltaIdentityVerificationDetails details2 = new DeltaIdentityVerificationDetails();
        details2.setPreferredName("Different Name");
        assertNotEquals(details1.hashCode(), details2.hashCode());
    }

    @Test
    void toStringShouldReturnNonNullString() {
        DeltaIdentityVerificationDetails details = new DeltaIdentityVerificationDetails();
        assertNotNull(details.toString());
    }

    @Test
    void equalsShouldReturnFalseWhenObjIsNull() {
        DeltaIdentityVerificationDetails details = new DeltaIdentityVerificationDetails();
        assertNotEquals(null, details);
    }

    @Test
    void equalsShouldReturnFalseForDifferentType() {
        DeltaIdentityVerificationDetails details = new DeltaIdentityVerificationDetails();
        Object other = new Object();
        assertNotEquals(details, other);
    }

    // antiMoneyLaunderingSupervisoryBodies different
    @Test
    void equalsShouldReturnFalseWhenAntiMoneyLaunderingSupervisoryBodiesDiffer() {
        DeltaIdentityVerificationDetails d1 = new DeltaIdentityVerificationDetails();
        DeltaIdentityVerificationDetails d2 = new DeltaIdentityVerificationDetails();
        d2.setAntiMoneyLaunderingSupervisoryBodies(List.of("Body1"));
        assertNotEquals(d1, d2);
    }

    // appointmentVerificationEndOn different
    @Test
    void equalsShouldReturnFalseWhenAppointmentVerificationEndOnDiffers() {
        DeltaIdentityVerificationDetails d1 = new DeltaIdentityVerificationDetails();
        DeltaIdentityVerificationDetails d2 = new DeltaIdentityVerificationDetails();
        d2.setAppointmentVerificationEndOn("2024-01-01");
        assertNotEquals(d1, d2);
    }

    // appointmentVerificationStatementDate different
    @Test
    void equalsShouldReturnFalseWhenAppointmentVerificationStatementDateDiffers() {
        DeltaIdentityVerificationDetails d1 = new DeltaIdentityVerificationDetails();
        DeltaIdentityVerificationDetails d2 = new DeltaIdentityVerificationDetails();
        d2.setAppointmentVerificationStatementDate("2024-02-01");
        assertNotEquals(d1, d2);
    }

    // appointmentVerificationStatementDueOn different
    @Test
    void equalsShouldReturnFalseWhenAppointmentVerificationStatementDueOnDiffers() {
        DeltaIdentityVerificationDetails d1 = new DeltaIdentityVerificationDetails();
        DeltaIdentityVerificationDetails d2 = new DeltaIdentityVerificationDetails();
        d2.setAppointmentVerificationStatementDueOn("2024-03-01");
        assertNotEquals(d1, d2);
    }

    // appointmentVerificationStartOn different
    @Test
    void equalsShouldReturnFalseWhenAppointmentVerificationStartOnDiffers() {
        DeltaIdentityVerificationDetails d1 = new DeltaIdentityVerificationDetails();
        DeltaIdentityVerificationDetails d2 = new DeltaIdentityVerificationDetails();
        d2.setAppointmentVerificationStartOn("2024-04-01");
        assertNotEquals(d1, d2);
    }

    // authorisedCorporateServiceProviderName different
    @Test
    void equalsShouldReturnFalseWhenAuthorisedCorporateServiceProviderNameDiffers() {
        DeltaIdentityVerificationDetails d1 = new DeltaIdentityVerificationDetails();
        DeltaIdentityVerificationDetails d2 = new DeltaIdentityVerificationDetails();
        d2.setAuthorisedCorporateServiceProviderName("Provider Name");
        assertNotEquals(d1, d2);
    }

    // identityVerifiedOn different
    @Test
    void equalsShouldReturnFalseWhenIdentityVerifiedOnDiffers() {
        DeltaIdentityVerificationDetails d1 = new DeltaIdentityVerificationDetails();
        DeltaIdentityVerificationDetails d2 = new DeltaIdentityVerificationDetails();
        d2.setIdentityVerifiedOn("2024-05-01");
        assertNotEquals(d1, d2);
    }

    // preferredName different
    @Test
    void equalsShouldReturnFalseWhenPreferredNameDiffers() {
        DeltaIdentityVerificationDetails d1 = new DeltaIdentityVerificationDetails();
        DeltaIdentityVerificationDetails d2 = new DeltaIdentityVerificationDetails();
        d2.setPreferredName("Preferred Name");
        assertNotEquals(d1, d2);
    }

    @Test
    void settersAndGettersShouldWorkCorrectly() {
        DeltaIdentityVerificationDetails details = new DeltaIdentityVerificationDetails();

        details.setAntiMoneyLaunderingSupervisoryBodies(List.of("Body1", "Body2"));
        assertEquals(List.of("Body1", "Body2"), details.getAntiMoneyLaunderingSupervisoryBodies());

        details.setAppointmentVerificationEndOn("2024-01-01");
        assertEquals("2024-01-01", details.getAppointmentVerificationEndOn());

        details.setAppointmentVerificationStatementDate("2024-02-01");
        assertEquals("2024-02-01", details.getAppointmentVerificationStatementDate());

        details.setAppointmentVerificationStatementDueOn("2024-03-01");
        assertEquals("2024-03-01", details.getAppointmentVerificationStatementDueOn());

        details.setAppointmentVerificationStartOn("2024-04-01");
        assertEquals("2024-04-01", details.getAppointmentVerificationStartOn());

        details.setAuthorisedCorporateServiceProviderName("Provider Name");
        assertEquals("Provider Name", details.getAuthorisedCorporateServiceProviderName());

        details.setIdentityVerifiedOn("2024-05-01");
        assertEquals("2024-05-01", details.getIdentityVerifiedOn());

        details.setPreferredName("Preferred Name");
        assertEquals("Preferred Name", details.getPreferredName());
    }
}