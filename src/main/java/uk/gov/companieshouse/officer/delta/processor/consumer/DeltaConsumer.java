package uk.gov.companieshouse.officer.delta.processor.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.consumer.resilience.CHKafkaResilientConsumerGroup;
import uk.gov.companieshouse.kafka.exceptions.DeserializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officer.delta.processor.deserialise.ChsDeltaDeserializer;
import uk.gov.companieshouse.officer.delta.processor.exception.ProcessException;
import uk.gov.companieshouse.officer.delta.processor.processor.Processor;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Consumes officer deltas from kafka, processes them, and handles any errors.
 * Messages are consumed via polling kafka for new messages.
 * Processing is delegated to a processor, which handles the business logic.
 */
@Component
public class DeltaConsumer {
    private final CHKafkaResilientConsumerGroup consumer;
    private final Logger logger;
    private final ChsDeltaDeserializer deserializer;
    @Value("${kafka.polling.duration.ms}")
    private int kafkaPollingDuration;
    private Processor<ChsDelta> processor;

    @SuppressWarnings("unused")
    @Autowired
    public DeltaConsumer(
            CHKafkaResilientConsumerGroup chKafkaConsumerGroup,
            Logger logger,
            ChsDeltaDeserializer deserializer,
            Processor<ChsDelta> processor) {

        this.consumer = chKafkaConsumerGroup;
        this.logger = logger;
        this.deserializer = deserializer;
        this.processor = processor;

        consumer.connect();
    }

    /**
     * Periodically polls kafka kafka for new ChsDelta messages. Poll rate configured through
     * the POLLING_RATE_MS variable.
     */
    @Scheduled(
            fixedRateString = "${kafka.polling.duration.ms}",
            initialDelayString = "${kafka.polling.initial.delay.ms}")
    void pollKafka() {
        Map<String, Object> pollingInfo = new HashMap<>();
        pollingInfo.put("duration", String.format("%dms", kafkaPollingDuration));
        logger.trace("Polling Kafka", pollingInfo);

        for (Message message : consumer.consume()) {
            Map<String, Object> info = new HashMap<>();
            info.put("partition", message.getPartition());
            info.put("offset", message.getOffset());
            logger.info("Received message from kafka", info);

            try {
                ChsDelta delta = deserializer.deserialize(message);
                processor.process(delta);
                consumer.commit(message);
                logger.info("Message committed", info);
            } catch (ProcessException e) {
                if (e.canRetry()) {
                    sendMessageToRetryTopic(message);
                } else {
                    sendMessageToErrorTopic(message);
                }
            } catch (DeserializationException e) {
                logger.error("Unable to deserialize message", e, info);
                sendMessageToErrorTopic(message);
            }
        }
    }

    void sendMessageToRetryTopic(Message message) {
        Map<String, Object> loggingInfo = new HashMap<>();
        loggingInfo.put("key", message.getKey());
        loggingInfo.put("topic", message.getTopic());
        loggingInfo.put("partition", message.getPartition());
        loggingInfo.put("offset", message.getOffset());

        int attemptsToRetry = 0;
        boolean success = false;

        while (!success && attemptsToRetry < 8) {
            attemptsToRetry++;
            loggingInfo.put("attempts to retry", attemptsToRetry);
            try {
                consumer.retry(0, message);
                consumer.commit(message);
                success = true;
                logger.info("Message successfully sent to retry topic", loggingInfo);
            } catch (ExecutionException e) {
                logger.error("Error occurred while send message to retry topic", e, loggingInfo);
            } catch (InterruptedException e) {
                logger.error("Interrupted while sending message to retry topic", e, loggingInfo);
                break;
            }
        }

        loggingInfo.put("kafkaMessage", message);
        logger.error(
                "Unable to add message to retry topic. Reached max attempts. Attempting to send to error topic",
                loggingInfo);
    }

    void sendMessageToErrorTopic(Message message) {

    }

    /**
     * Sets the processor that will be called with the deserialized ChsDelta
     *
     * @param processor the processor that will process the delta
     */
    public void setProcessor(Processor<ChsDelta> processor) {
        this.processor = processor;
    }

    /**
     * Clean up consumer connections when the program ends.
     */
    @PreDestroy
    public void destroy() {
        consumer.close();
    }
}