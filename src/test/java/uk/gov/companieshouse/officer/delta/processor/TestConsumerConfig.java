package uk.gov.companieshouse.officer.delta.processor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import uk.gov.companieshouse.kafka.consumer.ConsumerConfig;
import uk.gov.companieshouse.kafka.consumer.factory.KafkaConsumerFactory;
import uk.gov.companieshouse.kafka.consumer.resilience.CHConsumerType;
import uk.gov.companieshouse.kafka.consumer.resilience.CHKafkaResilientConsumerGroup;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;

import java.util.List;

@SuppressWarnings("unused")
@TestConfiguration
public class TestConsumerConfig {
    private static final String OFFICER_DELTA_TOPIC = "officers-delta";
    @Value("${kafka.broker.addr}")
    private String kafkaBrokerAddress;

    @Bean
    ConsumerConfig consumerConfig() {
        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setBrokerAddresses(new String[]{kafkaBrokerAddress});
        consumerConfig.setAutoCommit(false);
        consumerConfig.setMaxRetries(10);
        consumerConfig.setTopics(List.of(OFFICER_DELTA_TOPIC));
        consumerConfig.setGroupName("officer-delta-processor");

        return consumerConfig;
    }

    @Bean
    ProducerConfig producerConfig() {
        ProducerConfig config = new ProducerConfig();

        config.setAcks(Acks.WAIT_FOR_LOCAL);
        config.setBrokerAddresses(new String[]{kafkaBrokerAddress});

        return config;
    }

    @Bean
    CHKafkaProducer producer(ProducerConfig producerConfig) {
        return new CHKafkaProducer(producerConfig);
    }


    @Bean
    CHKafkaResilientConsumerGroup chKafkaConsumerGroup(ConsumerConfig consumerConfig,
                                                       CHKafkaProducer producer) {
        return new CHKafkaResilientConsumerGroup(
                consumerConfig,
                CHConsumerType.MAIN_CONSUMER,
                new KafkaConsumerFactory(), producer);

    }
}