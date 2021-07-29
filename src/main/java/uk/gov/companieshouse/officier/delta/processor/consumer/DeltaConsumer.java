package uk.gov.companieshouse.officier.delta.processor.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.kafka.consumer.resilience.CHKafkaResilientConsumerGroup;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officier.delta.processor.exception.ProcessException;
import uk.gov.companieshouse.officier.delta.processor.processor.Processor;

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
    @Value("${kafka.polling.duration.ms}")
    private int kafkaPollingDuration;
    private Processor<Message> processor;

    @Autowired
    public DeltaConsumer(
            CHKafkaResilientConsumerGroup chKafkaConsumerGroup,
            Logger logger,
            Processor<Message> processor) {

        this.consumer = chKafkaConsumerGroup;
        this.logger = logger;
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
        logger.debug("Polling Kafka", pollingInfo);

        // TODO: log index and when commit
        // TODO: log retry and failure / error
        // TODO: retry and error topics

        for (Message message : consumer.consume()) {
            Map<String, Object> info = new HashMap<>();
            info.put("partition", message.getPartition());
            info.put("offset", message.getOffset());
            logger.info("Received message from kafka", info);

            try {
                processor.process(message);
                consumer.commit(message);
                logger.info("Message committed", info);
            } catch (ProcessException e) {
                // TODO: check if can retry, and push to relevant topic
                if (e.canRetry()) {
                    sendMessageToRetryTopic(message);
                } else {
                    // TODO: handle error topic
                }
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
                success = true;
                logger.info("Message successfully sent to retry topic", loggingInfo);
            } catch (ExecutionException e) {
                logger.error("Error occurred while send message to retry topic", e);
            } catch (InterruptedException e) {
                logger.error("Interrupted while sending message to retry topic", e);
                break;
            }
        }

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
    public void setProcessor(Processor<Message> processor) {
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