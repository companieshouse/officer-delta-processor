package uk.gov.companieshouse.officer.delta.processor.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
import uk.gov.companieshouse.officer.delta.processor.processor.DeltaProcessor;
import uk.gov.companieshouse.officer.delta.processor.processor.Processor;


/**
 * Configuration required for creating the consumer
 */
@Configuration
public class DeltaConsumerConfig {
    @Value("${kafka.odp.group.name}")
    public static final String KAFKA_GROUP_NAME = "officer-delta-processor";

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

    private ConsumerConfig createConsumerConfig() {
        ConsumerConfig config = ConsumerConfig.createConfigWithResilience(KAFKA_GROUP_NAME);

        config.setAutoCommit(false);

        return config;
    }

    /**
     * Config used by the ch-kafka library for creating resilient consumers.
     * It's initialized from environment variables
     * For testing, where environment variables are not set, a ContextConfiguration is needed to
     * create this config.
     *
     * @return the consumer config initialized from the environment.
     */
    @Bean
    @Profile("!test")
    ConsumerConfig mainConsumerConfig() {
        return createConsumerConfig();
    }

    /**
     * Config used by the ch-kafka library for creating resilient consumers.
     * It's initialized from environment variables
     * For testing, where environment variables are not set, a ContextConfiguration is needed to
     * create this config.
     *
     * @return the consumer config initialized from the environment.
     */
    @Bean
    @Profile("!test")
    ConsumerConfig retryConsumerConfig() {
        final ConsumerConfig retryConfig = createConsumerConfig();

        retryConfig.setGroupName(String.join("-", retryConfig.getGroupName(), "retry"));

        return retryConfig;
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
    CHKafkaResilientConsumerGroup mainKafkaConsumerGroup(
            @Qualifier("mainConsumerConfig") ConsumerConfig consumerConfig) {
        return new CHKafkaResilientConsumerGroup(consumerConfig, CHConsumerType.MAIN_CONSUMER);
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
    CHKafkaResilientConsumerGroup retryKafkaConsumerGroup(
            @Qualifier("retryConsumerConfig") ConsumerConfig consumerConfig) {
        return new CHKafkaResilientConsumerGroup(consumerConfig, CHConsumerType.RETRY_CONSUMER);

    }

    private DeltaConsumer createDeltaConsumer(
            @Qualifier("retryKafkaConsumerGroup") final CHKafkaResilientConsumerGroup consumerGroup,
            final ChsDeltaMarshaller marshaller, final Processor<ChsDelta> processor, final Logger logger) {
        logger.debug(String.format("Creating [%s]...", consumerGroup.getConsumerType()));

        return new DeltaConsumer(consumerGroup, marshaller, processor, logger);
    }

    /**
     * Creates a DeltaConsumer for the MAIN topic.
     * Creates its own kafka producer using variables from the environment. For testing a separate
     * ContextConfiguration is needed as the environment variables are not present to create a producer.
     *
     * @param consumerGroup the {@link CHKafkaResilientConsumerGroup}
     * @param marshaller    the {@link ChsDeltaMarshaller}
     * @param processor     the {@link DeltaProcessor}
     * @param logger        the logger
     * @return the DeltaConsumer
     */
    @Bean
    @Profile("!test")
    DeltaConsumer mainDeltaConsumer(
            @Qualifier("mainKafkaConsumerGroup") final CHKafkaResilientConsumerGroup consumerGroup,
            final ChsDeltaMarshaller marshaller, final Processor<ChsDelta> processor, final Logger logger) {
        return createDeltaConsumer(consumerGroup, marshaller, processor, logger);
    }

    /**
     * Creates a DeltaConsumer for the RETRY topic.
     * Creates its own kafka producer using variables from the environment. For testing a separate
     * ContextConfiguration is needed as the environment variables are not present to create a producer.
     *
     * @param consumerGroup the {@link CHKafkaResilientConsumerGroup}
     * @param marshaller    the {@link ChsDeltaMarshaller}
     * @param processor     the {@link DeltaProcessor}
     * @param logger        the logger
     * @return the DeltaConsumer
     */
    @Bean
    @Profile("!test")
    DeltaConsumer retryDeltaConsumer(
            @Qualifier("retryKafkaConsumerGroup") final CHKafkaResilientConsumerGroup consumerGroup,
            final ChsDeltaMarshaller marshaller, final Processor<ChsDelta> processor, final Logger logger) {
        return createDeltaConsumer(consumerGroup, marshaller, processor, logger);
    }
}
