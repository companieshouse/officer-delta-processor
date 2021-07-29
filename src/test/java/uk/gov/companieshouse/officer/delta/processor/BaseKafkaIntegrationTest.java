package uk.gov.companieshouse.officer.delta.processor;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.Before;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;
import uk.gov.companieshouse.officier.delta.processor.OfficerDeltaProcessorApplication;
import uk.gov.companieshouse.officier.delta.processor.consumer.DeltaConsumer;
import uk.gov.companieshouse.officier.delta.processor.processr.Processor;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

@ExtendWith(SpringExtension.class)
@Testcontainers
@SpringBootTest(classes = OfficerDeltaProcessorApplication.class)
public abstract class BaseKafkaIntegrationTest {
    @Autowired
    ApplicationContext context;

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer();

    @Before
    public void setup() {

    }

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
        try (AdminClient adminClient = KafkaAdminClient.create(Collections.singletonMap(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, url))) {
            adminClient.createTopics(Collections.singletonList(new NewTopic("officers-delta", 1, (short) 1))).all().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    void waitUntil(BooleanSupplier waitFn, Duration timeoutDuration) throws TimeoutException {
        Instant timeout = Instant.now().plusMillis(timeoutDuration.toMillis());
        while (!waitFn.getAsBoolean()) {
            if (Instant.now().isAfter(timeout)) {
                throw new TimeoutException();
            }

            try {
                Thread.sleep(250);
            } catch (Exception e) {
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
            // TODO: get producer to work
            producer.send(message);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            producer.close();
        }
    }

    void setProcessor(Processor processor) {
        DeltaConsumer deltaConsumer = context.getBean(DeltaConsumer.class);
        deltaConsumer.setProcessor(processor);
    }
}
