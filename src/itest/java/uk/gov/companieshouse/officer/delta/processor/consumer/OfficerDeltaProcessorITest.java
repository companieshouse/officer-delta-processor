package uk.gov.companieshouse.officer.delta.processor.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.officer.delta.processor.AbstractIntegrationTest;

class OfficerDeltaProcessorITest extends AbstractIntegrationTest {

    @Autowired
    public KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${officer.delta.processor.topic}")
    private String mainTopic;

    @Test
    void testSendingKafkaMessage() {
        ChsDelta chsDelta = new ChsDelta("{ \"key\": \"value\" }", 1, "some_id", false);
        kafkaTemplate.send(mainTopic, chsDelta);
    }

}