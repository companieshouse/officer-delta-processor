package uk.gov.companieshouse.officer.delta.processor.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsNot.not;

public class ApplicationConfigTest {
    private ApplicationConfig applicationConfig;

    @BeforeEach
    void setUp() {
        applicationConfig = new ApplicationConfig();
    }
    @Test
    void serializerFactory() {
        assertThat(applicationConfig.serializerFactory(), is(not(nullValue())));
        assertThat(applicationConfig.serializerFactory(), isA(SerializerFactory.class));
    }

    @Test
    void environmentReader() {
        assertThat(applicationConfig.environmentReader(), is(not(nullValue())));
        assertThat(applicationConfig.environmentReader(), isA(EnvironmentReader.class));
    }
}
