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
    void consumerConfig() throws Exception {
        ConsumerConfig consumerConfig = withKafkaEnvironment().execute(() -> config.consumerConfig());

        assertThat(consumerConfig, isA(ConsumerConfig.class));
    }

    @Test
    void mainKafkaConsumerGroup() throws Exception {
        CHKafkaResilientConsumerGroup chKafkaConsumerGroup =
                withKafkaEnvironment().execute(() -> config.mainKafkaConsumerGroup(config.consumerConfig()));

        assertThat(chKafkaConsumerGroup, isA(CHKafkaResilientConsumerGroup.class));
    }

    @Test
    void mainDeltaConsumer() throws Exception {
        CHKafkaResilientConsumerGroup chKafkaConsumerGroup =
                withKafkaEnvironment().execute(() -> config.mainKafkaConsumerGroup(config.consumerConfig()));
        final AvroDeserializer<ChsDelta> deserializer = config.chsDeltaAvroDeserializer(config.deserializerFactory());
        final AvroSerializer<ChsDelta> serializer = config.chsDeltaAvroSerializer(config.serializerFactory());
        final ChsDeltaMarshaller marshaller = new ChsDeltaMarshaller(deserializer, serializer);

        final DeltaConsumer mainDeltaConsumer =
                config.mainDeltaConsumer(chKafkaConsumerGroup, marshaller, processor, logger);

        assertThat(mainDeltaConsumer, isA(DeltaConsumer.class));
        assertThat(mainDeltaConsumer.getConsumerType(), is(CHConsumerType.MAIN_CONSUMER));
    }
}
