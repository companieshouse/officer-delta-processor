package uk.gov.companieshouse.officer.delta.processor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SonarQubeClassITest {

    @Test
    void test() {
        // given
        SonarQubeClass sonarQubeClass = new SonarQubeClass();

        // when
        int result = sonarQubeClass.calculate(1,1);

        // then
        assertEquals(2, result);
    }
}
