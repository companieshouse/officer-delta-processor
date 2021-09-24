package uk.gov.companieshouse.officer.delta.processor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.consumer.resilience.CHKafkaResilientConsumerGroup;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officer.delta.processor.deserialise.ChsDeltaMarshaller;
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
@Deprecated(forRemoval = true)
// replaced by DeltaConsumer, but used by BaseKafkaIntegrationTest, OfficerDeltaConsumerIT which are @Disabled
@Service
public class DeltaConsumerService {
    private final CHKafkaResilientConsumerGroup consumer;
    private final Logger logger;
    private final ChsDeltaMarshaller marshaller;
    @Value("${kafka.polling.duration.ms}")
    private int kafkaPollingDuration;
    private Processor<ChsDelta> processor;

    @SuppressWarnings("unused")
    @Autowired
    public DeltaConsumerService(
            CHKafkaResilientConsumerGroup chKafkaConsumerGroup,
            Logger logger,
            ChsDeltaMarshaller marshaller,
            Processor<ChsDelta> processor) {

        this.consumer = chKafkaConsumerGroup;
        this.logger = logger;
        this.marshaller = marshaller;
        this.processor = processor;

        consumer.connect();
    }

    /**
     * Periodically polls kafka kafka for new ChsDelta messages. Poll rate configured through
     * the POLLING_RATE_MS variable.
     */
//    @Scheduled(
//            fixedRateString = "${kafka.polling.duration.ms}",
//            initialDelayString = "${kafka.polling.initial.delay.ms}")
    void pollKafka() throws InterruptedException {
        Map<String, Object> pollingInfo = new HashMap<>();
        pollingInfo.put("duration", String.format("%dms", kafkaPollingDuration));
        logger.trace("Polling Kafka", pollingInfo);

        for (Message message : consumer.consume()) {
            Map<String, Object> info = new HashMap<>();
            info.put("partition", message.getPartition());
            info.put("offset", message.getOffset());
            logger.info("Received message from kafka", info);

//            try {
                ChsDelta delta = marshaller.deserialize(message);
                processor.process(delta);
                consumer.commit(message);
                logger.info("Message committed", info);
//            } catch (ProcessException e) {
//                if (e.canRetry()) {
//                    sendMessageToRetryTopic(message);
//                } else {
//                    info.put("stackTrace", ExceptionUtils.getStackTrace(e));
//                    logger.error("Received fatal exception from processor.", e, info);
//                    sendMessageToErrorTopic(message);
//                }
//            } catch (DeserializationException e) {
//                logger.error("Unable to deserialize message", e, info);
//                sendMessageToErrorTopic(message);
//            }
        }
    }

    void sendMessageToRetryTopic(Message message) throws InterruptedException {
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
            }
        }

        loggingInfo.put("kafkaMessage", message);
        logger.error(
                "Unable to add message to retry topic. Reached max attempts. Attempting to send to error topic",
                loggingInfo);
    }

    @SuppressWarnings("java:S1186")
        // Remove once method
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