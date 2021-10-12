package uk.gov.companieshouse.officer.delta.processor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.deserialization.AvroDeserializer;
import uk.gov.companieshouse.kafka.deserialization.DeserializerFactory;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;


/**
 * Configuration required for creating the consumers
 */
@Configuration
public class KafkaSerializerConfig {
    /**
     * Create a serializer factory for creating serializers
     *
     * @return the factory
     */
    @Bean
    SerializerFactory serializerFactory() {
        return new SerializerFactory();
    }

    /**
     * Create a deserializer factory for creating deserializers
     *
     * @return the factory
     */
    @Bean
    DeserializerFactory deserializerFactory() {
        return new DeserializerFactory();
    }

    /**
     * The serializer for serializing deltas as a new kafka message.
     * Actual delta is Json encoded within the data field.
     *
     * @param serializerFactory factory for creating serializers
     * @return a class that can serialize ChsDeltas
     */
    @Bean
    AvroSerializer<ChsDelta> chsDeltaAvroSerializer(SerializerFactory serializerFactory) {
        return serializerFactory.getSpecificRecordSerializer(ChsDelta.class);
    }

    /**
     * The deserializer for deserializing deltas received from kafka.
     * Actual delta is Json encoded within the data field.
     *
     * @param deserializerFactory factory for creating deserializers
     * @return a class that can deserialize ChsDeltas
     */
    @Bean
    AvroDeserializer<ChsDelta> chsDeltaAvroDeserializer(DeserializerFactory deserializerFactory) {
        return deserializerFactory.getSpecificRecordDeserializer(ChsDelta.class);
    }

}
