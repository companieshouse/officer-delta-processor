package uk.gov.companieshouse.officer.delta.processor;

import com.github.stefanbirkner.systemlambda.SystemLambda;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.consumer.ConsumerConfig;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Objects;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.springframework.kafka.support.KafkaHeaders.EXCEPTION_CAUSE_FQCN;

public class Util {
    public static SystemLambda.WithEnvironmentVariables withKafkaEnvironment(ConsumerConfig config) {
        final String brokerAddr = String.join(",", config.getBrokerAddresses());
        final String topicString = String.join(",", config.getTopics());

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
        return new ProducerRecord<>(topic, 1,1L ,null, recordObj, headers);

    }

    public static Message<ChsDelta> createChsDeltaMessage(String filename, boolean isDelete) throws IOException {
        InputStreamReader exampleJsonPayload = new InputStreamReader(
            Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResourceAsStream(filename)));
        String data = FileCopyUtils.copyToString(exampleJsonPayload);

        return buildMessage(data, isDelete);
    }

    private static Message<ChsDelta> buildMessage(String data, boolean isDelete) {
        ChsDelta mockChsDelta = ChsDelta.newBuilder()
                .setData(data)
                .setContextId("context_id")
                .setAttempt(1)
                .setIsDelete(isDelete)
                .build();
        return MessageBuilder
                .withPayload(mockChsDelta)
                .setHeader(KafkaHeaders.RECEIVED_TOPIC, "test")
                .setHeader("OFFICER_DELTA_RETRY_COUNT", 1)
                .build();
    }
}
