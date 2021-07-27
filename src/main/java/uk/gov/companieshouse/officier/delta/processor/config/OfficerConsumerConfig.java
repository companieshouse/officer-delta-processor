package uk.gov.companieshouse.officier.delta.processor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.consumer.CHKafkaPartitionConsumer;
import uk.gov.companieshouse.kafka.consumer.ConsumerConfig;
import uk.gov.companieshouse.kafka.deserialization.AvroDeserializer;
import uk.gov.companieshouse.kafka.deserialization.DeserializerFactory;

import java.util.List;

@Configuration
public class OfficerConsumerConfig {

    public static final String OFFICER_DELTA_TOPIC = "officer-delta";

    @Bean
    ConsumerConfig consumerConfig() {
        ConsumerConfig config = new ConsumerConfig();

        config.setAutoCommit(false);
        config.setTopics(List.of(OFFICER_DELTA_TOPIC));
        config.setBrokerAddresses(new String[]{"kafka:9092"});

        return config;
    }

    @Bean
    DeserializerFactory deserializerFactory() {
        return new DeserializerFactory();
    }

    @Bean
    AvroDeserializer<ChsDelta> chsDeltaAvroDeserializer(DeserializerFactory deserializerFactory) {
        return deserializerFactory
                .getSpecificRecordDeserializer(ChsDelta.class);
    }

    @Bean
    CHKafkaPartitionConsumer chKafkaPartitionConsumer(ConsumerConfig consumerConfig) {
        return new CHKafkaPartitionConsumer(consumerConfig);
    }
}
