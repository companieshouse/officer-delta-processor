package uk.gov.companieshouse.officer.delta.processor;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.consumer.ConsumerConfig;
import uk.gov.companieshouse.kafka.consumer.factory.KafkaConsumerFactory;
import uk.gov.companieshouse.kafka.consumer.resilience.CHConsumerType;
import uk.gov.companieshouse.kafka.consumer.resilience.CHKafkaResilientConsumerGroup;
import uk.gov.companieshouse.kafka.exceptions.DeserializationException;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;
import uk.gov.companieshouse.officier.delta.processor.OfficerDeltaProcessorApplication;
import uk.gov.companieshouse.officier.delta.processor.consumer.DeltaConsumer;
import uk.gov.companieshouse.officier.delta.processor.deserialise.ChsDeltaDeserializer;
import uk.gov.companieshouse.officier.delta.processor.processor.Processor;

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
@Testcontainers
@SpringBootTest(classes = OfficerDeltaProcessorApplication.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:test.properties")
@ContextConfiguration(classes = BaseKafkaIntegrationTest.ContextConfiguration.class)
public abstract class BaseKafkaIntegrationTest {
    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka"));

    @Autowired
    DeltaConsumer consumer;

    @Autowired
    Processor<Message> processor;

    @Autowired
    ChsDeltaDeserializer chsDeltaDeserializer;

    List<Message> messagesConsumed;

    @DynamicPropertySource
    static void kafkaBrokerProperties(DynamicPropertyRegistry registry) {
        registry.add("kafka.broker.url",
                () -> {
                    String url = getBrokerAddress();
                    setupKafka(url);
                    return url;
                });
    }

    static String getBrokerAddress() {
        return kafkaContainer.getHost() + ":" + kafkaContainer.getFirstMappedPort();
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

    void sendChsDelta(String topic, String json) {
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

    void captureMessages() {
        messagesConsumed = new ArrayList<>();

        consumer.setProcessor(message -> {
            processor.process(message);
            messagesConsumed.add(message);
        });
    }

    Message waitForMessage(Duration timeout) throws TimeoutException {
        waitUntil(() -> messagesConsumed.size() > 0, timeout);
        return messagesConsumed.stream().findFirst().orElseThrow();
    }

    ChsDelta waitForDelta(Duration timeout) throws TimeoutException, DeserializationException {
        Message message = waitForMessage(timeout);
        return chsDeltaDeserializer.deserialize(message);
    }

    @TestConfiguration
    static class ContextConfiguration {
        private static final String OFFICER_DELTA_TOPIC = "officers-delta";
        @Value("${kafka.broker.url}")
        private String kafkaBrokerAddress;

        @Bean
        ConsumerConfig consumerConfig() {
            ConsumerConfig consumerConfig = new ConsumerConfig();
            consumerConfig.setBrokerAddresses(new String[]{kafkaBrokerAddress});
            consumerConfig.setAutoCommit(false);
            consumerConfig.setMaxRetries(10);
            consumerConfig.setTopics(List.of(OFFICER_DELTA_TOPIC));
            consumerConfig.setGroupName("officer-delta-processor");

            return consumerConfig;
        }

        @Bean
        ProducerConfig producerConfig() {
            ProducerConfig config = new ProducerConfig();

            config.setAcks(Acks.WAIT_FOR_LOCAL);
            config.setBrokerAddresses(new String[]{kafkaBrokerAddress});

            return config;
        }

        @Bean
        CHKafkaProducer producer(ProducerConfig producerConfig) {
            return new CHKafkaProducer(producerConfig);
        }


        @Bean
        CHKafkaResilientConsumerGroup chKafkaConsumerGroup(ConsumerConfig consumerConfig,
                                                           CHKafkaProducer producer) {
            return new CHKafkaResilientConsumerGroup(
                    consumerConfig,
                    CHConsumerType.MAIN_CONSUMER,
                    new KafkaConsumerFactory(), producer);

        }
    }
}
