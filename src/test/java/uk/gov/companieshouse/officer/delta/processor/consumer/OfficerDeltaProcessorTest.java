package uk.gov.companieshouse.officer.delta.processor.consumer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.officer.delta.processor.config.KafkaTestContainerConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@Import(KafkaTestContainerConfig.class)
@ActiveProfiles({"test"})
class OfficerDeltaProcessorTest {

    @Autowired
    public KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${officer.delta.processor.topic}")
    private String mainTopic;

    @Test
    @Disabled("Pending POM refactor")
    void testSendingKafkaMessage() throws InterruptedException, ExecutionException, TimeoutException {
        ChsDelta chsDelta = new ChsDelta("{ \"key\": \"value\" }", 1, "some_id", false);
        chsDelta.setContextId(UUID.randomUUID().toString());
        var future = kafkaTemplate.send(mainTopic, chsDelta);
        future.get(10, TimeUnit.SECONDS);
        assertTrue(future.isDone());
    }
}