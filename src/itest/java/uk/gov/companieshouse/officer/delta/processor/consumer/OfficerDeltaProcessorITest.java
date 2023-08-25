package uk.gov.companieshouse.officer.delta.processor.consumer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.officer.delta.processor.OfficerDeltaProcessorApplication;
import uk.gov.companieshouse.officer.delta.processor.config.KafkaTestContainerConfig;

@SpringBootTest(classes = {OfficerDeltaProcessorApplication.class, KafkaTestContainerConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
@ActiveProfiles({"test"})
class OfficerDeltaProcessorITest {

    @Autowired
    public KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    private CountDownLatch latch;

    @Value("${officer.delta.processor.topic}")
    private String mainTopic;

    @Test
    void testSendingKafkaMessage() throws Exception {
        ChsDelta chsDelta = new ChsDelta("{ \"key\": \"value\" }", 1, "some_id", false);
        chsDelta.setContextId(UUID.randomUUID().toString());
        kafkaTemplate.send(mainTopic, chsDelta);
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
    }
}