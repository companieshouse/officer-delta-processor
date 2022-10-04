package uk.gov.companieshouse.officer.delta.processor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import uk.gov.companieshouse.officer.delta.processor.service.api.ApiClientService;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = TestConsumerConfig.class)
@TestPropertySource(locations = "classpath:test.properties")
class OfficerDeltaProcessorApplicationTests {
    @Autowired
    private ApiClientService testApiClientService;

    @Test
    void contextLoads() throws Exception {
        assertThat(testApiClientService, is(notNullValue()));
    }

}
