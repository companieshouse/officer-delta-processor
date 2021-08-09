package uk.gov.companieshouse.officer.delta.processor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = TestConsumerConfig.class)
@TestPropertySource(locations = "classpath:test.properties")
class OfficerDeltaProcessorApplicationTests {

    @Test
    void contextLoads() {
    }

}
