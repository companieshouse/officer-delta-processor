package uk.gov.companieshouse.officer.delta.processor;

import com.github.stefanbirkner.systemlambda.SystemLambda;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.logging.log4j.util.Strings;
import uk.gov.companieshouse.kafka.consumer.ConsumerConfig;

import java.util.Arrays;
import java.util.Collections;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.springframework.kafka.support.KafkaHeaders.EXCEPTION_CAUSE_FQCN;

public class Util {
    public static SystemLambda.WithEnvironmentVariables withKafkaEnvironment(ConsumerConfig config) {
        final String brokerAddr = Strings.join(Arrays.asList(config.getBrokerAddresses()), ',');
        final String topicString = Strings.join(config.getTopics(), ',');

        return withEnvironmentVariable("KAFKA_BROKER_ADDR", brokerAddr)
                .and("KAFKA_TOPICS_LIST", topicString)
                .and("KAFKA_AUTO_COMMIT", Boolean.toString(config.isAutoCommit()))
                .and("KAFKA_GROUP_NAME", config.getGroupName())
                .and("KAFKA_RESET_OFFSET", Boolean.toString(config.isResetOffset()))
                .and("MAXIMUM_RETRY_ATTEMPTS", Integer.toString(config.getMaxRetries()))
                .and("RETRY_THROTTLE_RATE_SECONDS", Long.toString(config.getRetryThrottle()));
    }

    public static SystemLambda.WithEnvironmentVariables withKafkaEnvironment() {
        return withKafkaEnvironment(defaultConfig());
    }

    public static ConsumerConfig defaultConfig() {
        ConsumerConfig consumerConfig = new ConsumerConfig("g");

        consumerConfig.setBrokerAddresses(new String[]{"example.org:9092"});
        consumerConfig.setTopics(Collections.singletonList("topic"));
        consumerConfig.setGroupName("group");

        return consumerConfig;
    }

    public static ProducerRecord<String, Object> createRecord(String topic, String header) {
        Object recordObj = new Object();
        RecordHeaders headers = new RecordHeaders();
        headers.add(EXCEPTION_CAUSE_FQCN, header.getBytes());
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, 1,1L ,null, recordObj, headers);

        return record;
    }

}
