package uk.gov.companieshouse.officer.delta.processor;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;
import uk.gov.companieshouse.officer.delta.processor.service.DeltaConsumerService;
import uk.gov.companieshouse.officer.delta.processor.processor.Processor;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

import static org.awaitility.Awaitility.await;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = OfficerDeltaProcessorApplication.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:test.properties")
@ContextConfiguration(classes = TestConsumerConfig.class)
@ExtendWith(KafkaExtension.class)
public abstract class BaseKafkaIntegrationTest {
    @Autowired
    DeltaConsumerService consumer;

    @Autowired
    Processor<ChsDelta> processor;

    List<ChsDelta> deltasConsumed;

    static String getBrokerAddress() {
        return System.getProperty("kafka.broker.url");
    }

    static void setupKafka(String url) {
        try (AdminClient adminClient = KafkaAdminClient.create(Collections.singletonMap(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, url))) {
            adminClient.createTopics(Collections.singletonList(new NewTopic("officers-delta", 1, (short) 1))).all().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    void waitUntil(BooleanSupplier waitFn, Duration timeoutDuration) throws TimeoutException {
        Instant timeout = Instant.now().plusMillis(timeoutDuration.toMillis());
        while (!waitFn.getAsBoolean()) {
            if (Instant.now().isAfter(timeout)) {
                throw new TimeoutException();
            }

            try {
                await().atMost(Duration.ofMillis(250));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    ProducerConfig createConfig() {
        ProducerConfig config = new ProducerConfig();

        config.setBrokerAddresses(new String[]{getBrokerAddress()});
        config.setAcks(Acks.WAIT_FOR_LOCAL);

        return config;
    }


    CHKafkaProducer createProducer() {
        return new CHKafkaProducer(createConfig());
    }

    void sendChsDelta(@SuppressWarnings("SameParameterValue") String topic, String json) {
        ChsDelta delta = new ChsDelta(json, 0, "context_id");

        AvroSerializer<ChsDelta> serializer = new SerializerFactory().getSpecificRecordSerializer(ChsDelta.class);
        byte[] data = new byte[0];
        try {
            data = serializer.toBinary(delta);
        } catch (SerializationException e) {
            e.printStackTrace();
        }

        sendMessage(topic, data);
    }

    void sendMessage(String topic, byte[] data) {
        CHKafkaProducer producer = createProducer();

        Message message = new Message();
        message.setTopic(topic);
        message.setValue(data);
        message.setTimestamp(Instant.now().getEpochSecond());

        try {
            producer.send(message);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            producer.close();
        }
    }

    void captureDeltas() {
        deltasConsumed = new ArrayList<>();

        consumer.setProcessor(message -> {
            processor.process(message);
            deltasConsumed.add(message);
        });
    }

    ChsDelta waitForDelta(Duration timeout) throws TimeoutException {
        waitUntil(() -> deltasConsumed.size() > 0, timeout);
        return deltasConsumed.stream().findFirst().orElseThrow();
    }
}
