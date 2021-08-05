package uk.gov.companieshouse.officer.delta.processor.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsNot.not;

class LoggingConfigTest {

    LoggingConfig loggingConfig;

    @BeforeEach
    void setUp() {
        loggingConfig = new LoggingConfig();
    }


    @Test
    void logger() {
        assertThat(loggingConfig.logger(), is(not(nullValue())));
        assertThat(loggingConfig.logger(), isA(Logger.class));
    }
}