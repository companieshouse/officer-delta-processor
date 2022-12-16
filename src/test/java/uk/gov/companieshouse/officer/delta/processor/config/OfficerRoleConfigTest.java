package uk.gov.companieshouse.officer.delta.processor.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OfficerRoleConfigTest {

    private static final Map<String, Integer> nonResigned = new HashMap<>();
    private static final Map<String, Integer> resigned = new HashMap<>();

    private OfficerRoleConfig config;

    @BeforeEach
    void setup() {
        config = new OfficerRoleConfig(nonResigned, resigned);
    }

    @Test
    void getConfigs() {
        assertEquals(nonResigned, config.getNonResigned());
        assertEquals(resigned, config.getResigned());
    }
}
