package uk.gov.companieshouse.officier.delta.processor.config;

import org.apache.kafka.common.KafkaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.consumer.CHKafkaConsumerGroup;
import uk.gov.companieshouse.kafka.consumer.ConsumerConfig;
import uk.gov.companieshouse.kafka.deserialization.AvroDeserializer;
import uk.gov.companieshouse.kafka.deserialization.DeserializerFactory;
import uk.gov.companieshouse.logging.Logger;

import java.util.List;

@Configuration
public class OfficerConsumerConfig {

    public static final String OFFICER_DELTA_TOPIC = "officers-delta";

    @Value("${kafka.broker.url}")
    private String kafkaURL;

    private final Logger logger;

    @Autowired
    public OfficerConsumerConfig(Logger logger) {
        this.logger = logger;
    }


    @Bean
    ConsumerConfig consumerConfig() {
        ConsumerConfig config = new ConsumerConfig();

        config.setAutoCommit(false);
        config.setTopics(List.of(OFFICER_DELTA_TOPIC));
        config.setBrokerAddresses(new String[]{kafkaURL});
        config.setGroupName("officer-delta-processor");
        config.setAutoCommit(false);
        config.setMaxRetries(50);
//        config.setRetryThrottle();

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
    CHKafkaConsumerGroup chKafkaConsumerGroup(ConsumerConfig consumerConfig) {
        try {
            logger.info("Kafka url: " + kafkaURL);
            return new CHKafkaConsumerGroup(consumerConfig);
        } catch (KafkaException ke) {
            logger.error("Unable to create kafka consumer group: " + ke.getCause().getMessage(), ke);
            throw ke;
        }
    }
}
