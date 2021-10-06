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
import uk.gov.companieshouse.kafka.deserialization.DeserializerFactory;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officer.delta.processor.consumer.DeltaConsumer;
import uk.gov.companieshouse.officer.delta.processor.deserialise.ChsDeltaMarshaller;
import uk.gov.companieshouse.officer.delta.processor.processor.Processor;

@ExtendWith(MockitoExtension.class)
class DeltaConsumerConfigTest {
    DeltaConsumerConfig config;

    @Mock
    private Processor<ChsDelta> processor;

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        config = new DeltaConsumerConfig();
    }

    @Test
    void deserializerFactory() {
        assertThat(config.deserializerFactory(), isA(DeserializerFactory.class));
    }

    @Test
    void serializerFactory() {
        assertThat(config.serializerFactory(), isA(SerializerFactory.class));
    }

    @Test
    void chsAvroDeserializer() {
        final AvroDeserializer<ChsDelta> deserializer = config.chsDeltaAvroDeserializer(config.deserializerFactory());

        assertThat(deserializer, isA(AvroDeserializer.class));
    }

    @Test
    void chsDeltaAvroSerializer() {
        final AvroSerializer<ChsDelta> serializer = config.chsDeltaAvroSerializer(config.serializerFactory());

        assertThat(serializer, isA(AvroSerializer.class));
    }

    @Test
    void mainConsumerConfig() throws Exception {
        ConsumerConfig consumerConfig = withKafkaEnvironment().execute(() -> config.mainConsumerConfig());

        assertThat(consumerConfig, isA(ConsumerConfig.class));
        assertThat(consumerConfig.getGroupName(), is("group"));
    }

    @Test
    void retryConsumerConfig() throws Exception {
        ConsumerConfig consumerConfig = withKafkaEnvironment().execute(() -> config.retryConsumerConfig());

        assertThat(consumerConfig, isA(ConsumerConfig.class));
        assertThat(consumerConfig.getGroupName(), is("group-retry"));
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
        final AvroDeserializer<ChsDelta> deserializer = config.chsDeltaAvroDeserializer(config.deserializerFactory());
        final AvroSerializer<ChsDelta> serializer = config.chsDeltaAvroSerializer(config.serializerFactory());
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
        final AvroDeserializer<ChsDelta> deserializer = config.chsDeltaAvroDeserializer(config.deserializerFactory());
        final AvroSerializer<ChsDelta> serializer = config.chsDeltaAvroSerializer(config.serializerFactory());
        final ChsDeltaMarshaller marshaller = new ChsDeltaMarshaller(deserializer, serializer);

        final DeltaConsumer retryDeltaConsumer =
                config.retryDeltaConsumer(retryConsumerGroup, marshaller, processor, logger);

        assertThat(retryDeltaConsumer, isA(DeltaConsumer.class));
        assertThat(retryDeltaConsumer.getConsumerType(), is(CHConsumerType.RETRY_CONSUMER));
    }
}
