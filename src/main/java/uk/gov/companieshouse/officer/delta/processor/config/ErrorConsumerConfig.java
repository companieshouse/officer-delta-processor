package uk.gov.companieshouse.officer.delta.processor.config;

import static uk.gov.companieshouse.officer.delta.processor.config.DeltaConsumerConfig.KAFKA_GROUP_NAME;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.consumer.ConsumerConfig;
import uk.gov.companieshouse.kafka.consumer.resilience.CHConsumerType;
import uk.gov.companieshouse.kafka.consumer.resilience.CHKafkaResilientConsumerGroup;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officer.delta.processor.consumer.DeltaConsumer;
import uk.gov.companieshouse.officer.delta.processor.deserialise.ChsDeltaMarshaller;
import uk.gov.companieshouse.officer.delta.processor.processor.DeltaProcessor;
import uk.gov.companieshouse.officer.delta.processor.processor.Processor;


/**
 * Configuration required for creating the consumers
 * Requires configuration beans from KafkaSerializerConfig
 */
@Configuration
@ConditionalOnProperty(value = "kafka.error.consumer.mode", havingValue = "true")
public class ErrorConsumerConfig {

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
    ConsumerConfig errorTopicConsumerConfig() {
        ConsumerConfig config = ConsumerConfig.createConfigWithResilience(KAFKA_GROUP_NAME);

        config.setAutoCommit(false);
        config.setGroupName(String.join("-", config.getGroupName(), "error"));

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
    CHKafkaResilientConsumerGroup errorKafkaConsumerGroup(
            @Qualifier("errorTopicConsumerConfig") ConsumerConfig consumerConfig) {
        return new CHKafkaResilientConsumerGroup(consumerConfig, CHConsumerType.ERROR_CONSUMER);
    }

    private DeltaConsumer createDeltaConsumer(final CHKafkaResilientConsumerGroup consumerGroup,
            final ChsDeltaMarshaller marshaller, final Processor<ChsDelta> processor, final Logger logger) {
        logger.debug(String.format("Creating [%s]...", consumerGroup.getConsumerType()));

        return new DeltaConsumer(consumerGroup, marshaller, processor, logger);
    }

    /**
     * Creates a DeltaConsumer for the ERROR topic.
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
    DeltaConsumer errorDeltaConsumer(
            @Qualifier("errorKafkaConsumerGroup") final CHKafkaResilientConsumerGroup consumerGroup,
            final ChsDeltaMarshaller marshaller, final Processor<ChsDelta> processor, final Logger logger) {
        return createDeltaConsumer(consumerGroup, marshaller, processor, logger);
    }
}
