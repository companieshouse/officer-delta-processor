package uk.gov.companieshouse.officer.delta.processor.model.enums;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompanyStatusTest {

    @ParameterizedTest
    @CsvSource({
            "0  , active",
            "1  , dissolved",
            "2  , liquidation",
            "3  , receivership",
            "4  , converted-closed",
            "5  , active",
            "7  , converted-closed",
            "8  , open",
            "9  , closed",
            "A  , receivership",
            "C  , insolvency-proceedings",
            "E  , insolvency-proceedings",
            "F  , receivership",
            "G  , receivership",
            "H  , insolvency-proceedings",
            "I  , voluntary-arrangement",
            "J  , insolvency-proceedings",
            "K  , insolvency-proceedings",
            "L  , insolvency-proceedings",
            "M  , administration",
            "N  , insolvency-proceedings",
            "O  , insolvency-proceedings",
            "P  , insolvency-proceedings",
            "Q  , active",
            "R  , dissolved",
            "S  , insolvency-proceedings",
            "T  , administration",
            "U  , insolvency-proceedings",
            "V  , insolvency-proceedings",
            "W  , insolvency-proceedings",
            "X  , converted-closed",
            "Z  , converted-closed",
            "AA  , active",
            "AB  , active",
            "AC  , registered",
            "AD  , removed" })
    void successfullyTransformCompanyStatus(String sourceStatus, String targetStatus) {
        assertEquals(targetStatus, CompanyStatus.statusFromKey(sourceStatus));
    }
}
