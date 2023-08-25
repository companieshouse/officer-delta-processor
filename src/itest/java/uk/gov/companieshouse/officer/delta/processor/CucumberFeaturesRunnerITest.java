package uk.gov.companieshouse.officer.delta.processor;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasspathResource("features")
@CucumberContextConfiguration
@TestPropertySource(locations="classpath:test.properties")
public class CucumberFeaturesRunnerITest extends AbstractITest {

}
