package uk.gov.companieshouse.officer.delta.processor.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

// NOTE: This code is temporary and will not be deployed to STAGING or LIVE. It is only to investigate a deployment
//       issue in the CiDev environment

@Component
public class LoggingHealthIndicator implements HealthIndicator {
    private static final Logger logger = LoggerFactory.getLogger(LoggingHealthIndicator.class);

    @Override
    public Health health() {
        logger.info("Health check logic is executing...");
        Health health = Health.up().build();
        logger.info("Health status: {}", health.getStatus());
        return health;
    }
}
