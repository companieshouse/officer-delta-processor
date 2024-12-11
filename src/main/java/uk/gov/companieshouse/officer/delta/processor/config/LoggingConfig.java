package uk.gov.companieshouse.officer.delta.processor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.officer.delta.processor.OfficerDeltaProcessorApplication;

/**
 * Configuration class for logging.
 */
@Configuration
@PropertySource("classpath:logger.properties")
public class LoggingConfig {

    /**
     * Creates a logger with specified namespace.
     *
     * @return the {@link LoggerFactory} for the specified namespace
     */
    @Bean
    public Logger logger() {
        return LoggerFactory.getLogger(OfficerDeltaProcessorApplication.APPLICATION_NAME_SPACE);
    }
}