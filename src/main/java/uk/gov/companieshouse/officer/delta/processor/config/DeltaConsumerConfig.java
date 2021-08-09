package uk.gov.companieshouse.officer.delta.processor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.consumer.ConsumerConfig;
import uk.gov.companieshouse.kafka.consumer.resilience.CHConsumerType;
import uk.gov.companieshouse.kafka.consumer.resilience.CHKafkaResilientConsumerGroup;
import uk.gov.companieshouse.kafka.deserialization.AvroDeserializer;
import uk.gov.companieshouse.kafka.deserialization.DeserializerFactory;


/**
 * Configuration required for creating the consumer
 */
@Configuration
public class DeltaConsumerConfig {
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
     * The deserializer for deserializing deltas received from kafka.
     * Actual delta is Json encoded within the data field.
     *
     * @param deserializerFactory factory fro creating deserializers
     * @return a class that can deserialize ChsDeltas
     */
    @Bean
    AvroDeserializer<ChsDelta> chsDeltaAvroDeserializer(DeserializerFactory deserializerFactory) {
        return deserializerFactory
                .getSpecificRecordDeserializer(ChsDelta.class);
    }

    /**
     * Config used by the the ch-kafka library for creating resilient consumers.
     * It's initialized from environment variables
     * For testing, where environment variables are not set, a ContextConfiguration is needed to
     * create this config.
     *
     * @return the consumer config initialized from the environment.
     */
    @Bean
    @Profile("!test")
    ConsumerConfig consumerConfig() {
        ConsumerConfig config = ConsumerConfig.createConfigWithResilience("officer-delta-processor");

        config.setAutoCommit(false);

        return config;
    }

    /**
     * Creates a consumer group with resilience.
     * Creates its own producer using variables from the environment. For testing a separate
     * ContextConfiguration is needed as the environment variables are not present to create a producer.
     *
     * @param consumerConfig the configuration for the consumer
     * @return the consumer group
     */
    @Bean
    @Profile("!test")
    CHKafkaResilientConsumerGroup chKafkaConsumerGroup(ConsumerConfig consumerConfig) {
        return new CHKafkaResilientConsumerGroup(
                consumerConfig,
                CHConsumerType.MAIN_CONSUMER);

    }
}
