package uk.gov.companieshouse.officer.delta.processor.config;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.deserialization.AvroDeserializer;
import uk.gov.companieshouse.kafka.deserialization.DeserializerFactory;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;

@ExtendWith(MockitoExtension.class)
class KafkaSerializerConfigTest {
    private KafkaSerializerConfig testConfig;

    @BeforeEach
    void setUp() {
        testConfig = new KafkaSerializerConfig();
    }

    @Test
    void serializerFactory() {
        assertThat(testConfig.serializerFactory(), isA(SerializerFactory.class));
    }

    @Test
    void deserializerFactory() {
        assertThat(testConfig.deserializerFactory(), isA(DeserializerFactory.class));
    }

    @Test
    void chsAvroDeserializer() {
        final AvroDeserializer<ChsDelta> deserializer =
                testConfig.chsDeltaAvroDeserializer(testConfig.deserializerFactory());

        assertThat(deserializer, isA(AvroDeserializer.class));
    }

    @Test
    void chsDeltaAvroSerializer() {
        final AvroSerializer<ChsDelta> serializer = testConfig.chsDeltaAvroSerializer(testConfig.serializerFactory());

        assertThat(serializer, isA(AvroSerializer.class));
    }

}
