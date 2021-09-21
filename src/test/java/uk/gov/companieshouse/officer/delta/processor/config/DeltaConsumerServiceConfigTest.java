package uk.gov.companieshouse.officer.delta.processor.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.consumer.ConsumerConfig;
import uk.gov.companieshouse.kafka.consumer.resilience.CHKafkaResilientConsumerGroup;
import uk.gov.companieshouse.kafka.deserialization.AvroDeserializer;
import uk.gov.companieshouse.kafka.deserialization.DeserializerFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.companieshouse.officer.delta.processor.Util.withKafkaEnvironment;

class DeltaConsumerServiceConfigTest {

    DeltaConsumerConfig config;

    @BeforeEach
    void setUp() {
        config = new DeltaConsumerConfig();
    }

    @Test
    void deserializerFactory() {
        assertThat(config.deserializerFactory(), is(not(nullValue())));
        assertThat(config.deserializerFactory(), isA(DeserializerFactory.class));
    }

    @Test
    void chsAvroDeserializer() {
        final AvroDeserializer<ChsDelta> deserializer =
                config.chsDeltaAvroDeserializer(config.deserializerFactory());

        assertThat(deserializer, is(not(nullValue())));
        assertThat(deserializer, isA(AvroDeserializer.class));
    }

    @Test
    void consumerConfig() {
        ConsumerConfig consumerConfig = null;
        try {
            consumerConfig = withKafkaEnvironment()
                    .execute(() -> config.consumerConfig());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception when creating consumer config");
        }

        assertThat(consumerConfig, is(not(nullValue())));
        assertThat(consumerConfig, isA(ConsumerConfig.class));
    }

    @Test
    void resilientConsumerGroup() {
        CHKafkaResilientConsumerGroup chKafkaConsumerGroup = null;
        try {
            chKafkaConsumerGroup = withKafkaEnvironment()
                    .execute(() -> config.chKafkaConsumerGroup(config.consumerConfig()));

        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception when creating consumer group");
        }

        assertThat(chKafkaConsumerGroup, is(not(nullValue())));
        assertThat(chKafkaConsumerGroup, isA(CHKafkaResilientConsumerGroup.class));
    }
}
