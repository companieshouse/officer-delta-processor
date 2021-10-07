package uk.gov.companieshouse.officer.delta.processor.config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.companieshouse.officer.delta.processor.Util.withKafkaEnvironment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.consumer.ConsumerConfig;
import uk.gov.companieshouse.kafka.consumer.resilience.CHConsumerType;
import uk.gov.companieshouse.kafka.consumer.resilience.CHKafkaResilientConsumerGroup;
import uk.gov.companieshouse.kafka.deserialization.AvroDeserializer;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officer.delta.processor.consumer.DeltaConsumer;
import uk.gov.companieshouse.officer.delta.processor.deserialise.ChsDeltaMarshaller;
import uk.gov.companieshouse.officer.delta.processor.processor.Processor;

@ExtendWith(MockitoExtension.class)
class DeltaConsumerConfigTest {
    DeltaConsumerConfig config;

    @Mock
    private AvroDeserializer<ChsDelta> deserializer;
    @Mock
    private AvroSerializer<ChsDelta> serializer;
    @Mock
    private Processor<ChsDelta> processor;
    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        config = new DeltaConsumerConfig();
    }

    @Test
    void mainConsumerConfig() throws Exception {
        ConsumerConfig consumerConfig = withKafkaEnvironment().execute(() -> config.mainConsumerConfig());

        assertThat(consumerConfig, isA(ConsumerConfig.class));
        assertThat(consumerConfig.getGroupName(), is("group"));
        assertThat(consumerConfig.isAutoCommit(), is(false));
    }

    @Test
    void retryConsumerConfig() throws Exception {
        ConsumerConfig consumerConfig = withKafkaEnvironment().execute(() -> config.retryConsumerConfig());

        assertThat(consumerConfig, isA(ConsumerConfig.class));
        assertThat(consumerConfig.getGroupName(), is("group-retry"));
        assertThat(consumerConfig.isAutoCommit(), is(false));
    }

    @Test
    void mainKafkaConsumerGroup() throws Exception {
        CHKafkaResilientConsumerGroup mainConsumerGroup =
                withKafkaEnvironment().execute(() -> config.mainKafkaConsumerGroup(config.mainConsumerConfig()));

        assertThat(mainConsumerGroup, isA(CHKafkaResilientConsumerGroup.class));
        assertThat(mainConsumerGroup.getConsumerType(), is(CHConsumerType.MAIN_CONSUMER));
    }

    @Test
    void retryKafkaConsumerGroup() throws Exception {
        CHKafkaResilientConsumerGroup retryConsumerGroup =
                withKafkaEnvironment().execute(() -> config.retryKafkaConsumerGroup(config.retryConsumerConfig()));

        assertThat(retryConsumerGroup, isA(CHKafkaResilientConsumerGroup.class));
        assertThat(retryConsumerGroup.getConsumerType(), is(CHConsumerType.RETRY_CONSUMER));
    }

    @Test
    void mainDeltaConsumer() throws Exception {
        CHKafkaResilientConsumerGroup chKafkaConsumerGroup =
                withKafkaEnvironment().execute(() -> config.mainKafkaConsumerGroup(config.mainConsumerConfig()));
        final ChsDeltaMarshaller marshaller = new ChsDeltaMarshaller(deserializer, serializer);

        final DeltaConsumer mainDeltaConsumer =
                config.mainDeltaConsumer(chKafkaConsumerGroup, marshaller, processor, logger);

        assertThat(mainDeltaConsumer, isA(DeltaConsumer.class));
        assertThat(mainDeltaConsumer.getConsumerType(), is(CHConsumerType.MAIN_CONSUMER));
    }

    @Test
    void retryDeltaConsumer() throws Exception {
        CHKafkaResilientConsumerGroup retryConsumerGroup =
                withKafkaEnvironment().execute(() -> config.retryKafkaConsumerGroup(config.retryConsumerConfig()));
        final ChsDeltaMarshaller marshaller = new ChsDeltaMarshaller(deserializer, serializer);

        final DeltaConsumer retryDeltaConsumer =
                config.retryDeltaConsumer(retryConsumerGroup, marshaller, processor, logger);

        assertThat(retryDeltaConsumer, isA(DeltaConsumer.class));
        assertThat(retryDeltaConsumer.getConsumerType(), is(CHConsumerType.RETRY_CONSUMER));
    }
}
