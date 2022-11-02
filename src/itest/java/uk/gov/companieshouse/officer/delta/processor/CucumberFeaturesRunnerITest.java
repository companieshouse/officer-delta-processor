package uk.gov.companieshouse.officer.delta.processor;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.runner.RunWith;
import org.springframework.test.context.TestPropertySource;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/itest/resources/features",
        plugin = {"pretty", "json:target/cucumber-report.json"})
@CucumberContextConfiguration
@TestPropertySource(locations="classpath:test.properties")
public class CucumberFeaturesRunnerITest extends AbstractIntegrationTest {

}
