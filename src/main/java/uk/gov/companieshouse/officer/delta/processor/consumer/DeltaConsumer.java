package uk.gov.companieshouse.officer.delta.processor.consumer;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.consumer.resilience.CHConsumerType;
import uk.gov.companieshouse.kafka.consumer.resilience.CHKafkaResilientConsumerGroup;
import uk.gov.companieshouse.kafka.exceptions.DeserializationException;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officer.delta.processor.deserialise.ChsDeltaDeSerializer;
import uk.gov.companieshouse.officer.delta.processor.exception.ProcessException;
import uk.gov.companieshouse.officer.delta.processor.processor.Processor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class DeltaConsumer {
    private final CHKafkaResilientConsumerGroup consumerGroup;
    private final ChsDeltaDeSerializer deserializer;
    private final Processor<ChsDelta> processor;
    private final Logger logger;

    /**
     * Deque of messages waiting to be processed.
     * Faster performance of remove first element than ArrayList
     */
    private Deque<Message> messages = new ArrayDeque<>();

    /**
     * Initialise the consumer by connecting to the consumer group
     */
    public DeltaConsumer(final CHKafkaResilientConsumerGroup consumerGroup, final ChsDeltaDeSerializer deserializer,
            final Processor<ChsDelta> processor, final Logger logger) {
        this.consumerGroup = consumerGroup;
        this.deserializer = deserializer;
        this.processor = processor;
        this.logger = logger;

        consumerGroup.connect();
    }

    /**
     * Commits the offset into Kafka for the message.
     *
     * @param message the message
     */
    public void commitOffset(Message message) {
        consumerGroup.commit();
    }

    /**
     * Retrieve an optional message from the message list.<br>
     * Return the next message from the cache; poll the topic for new
     * messages if the cache is empty.
     * If no messages exist, return Optional.empty().
     *
     * @return message
     */
    public Optional<Message> getNextDeltaMessage() {
        if (messages.isEmpty()) {
            messages.addAll(consumerGroup.consume());
            logger.debug(String.format("Polled %d new message(s)", messages.size()));
        }

        return Optional.ofNullable(messages.pollFirst());
    }

    public long getRetryThrottle() {
        return consumerGroup.getConfig().getRetryThrottle();
    }

    public int getMaxRetryAttempts() {
        return consumerGroup.getConfig().getMaxRetries();
    }

    public long stopAtOffset() {
        return consumerGroup.stopAtOffset();
    }

    public CHConsumerType getConsumerType() {
        return consumerGroup.getConsumerType();
    }

    @Scheduled(fixedDelayString = "${kafka.polling.delay.ms}", initialDelayString = "${kafka.polling.initial.delay.ms}")
    void consumeMessage() throws InterruptedException, ExecutionException {
        final Optional<Message> nextDeltaMessage = getNextDeltaMessage();

        if (nextDeltaMessage.isPresent()) {
            final Message deltaMessage = nextDeltaMessage.get();
            final Long offset = deltaMessage.getOffset();
            Map<String, Object> info = new HashMap<>();

            info.put("topic", deltaMessage.getTopic());
            info.put("partition", deltaMessage.getPartition());
            info.put("offset", offset);
            logger.info("Consume message", info);

            ChsDelta delta = null;
            int attempt = 0;

            try {
                delta = deserializer.deserialize(deltaMessage);

                attempt = delta.getAttempt();
                processor.process(delta);
            }
            catch (ProcessException e) {
                logger.error("Processor failed on message: " + e.getMessage());
                if (e.canRetry()) {
                    queueRetry(offset, attempt, delta);
                }
                else {
                    info.put("stackTrace", ExceptionUtils.getStackTrace(e));
                    logger.error("Received fatal exception from processor.", e, info);
                }
            }
            catch (DeserializationException e) {
                logger.error("Unable to deserialize message", e, info);
            }
            catch (Exception e) {
                info.put("stackTrace", ExceptionUtils.getStackTrace(e));
                logger.error("Processor failed on message", e, info);
                queueRetry(offset, attempt, delta);
            }
            finally {
                logger.info("Commit message", info);
                commitOffset(deltaMessage);
            }
        }
    }

    /**
     * Send the message to the appropriate topic because of a transient error:
     * - the retry topic if retries < max retries
     * - the error topic if retries >= max retries
     *
     * @param attempt      the attempt counter
     * @param delta        the ChsDelta payload to queue
     * @param sourceOffset the offset of the originating message
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void queueRetry(long sourceOffset, int attempt, final ChsDelta delta)
            throws ExecutionException, InterruptedException {
        consumerGroup.retry(attempt, createRetryMessage(delta, sourceOffset));
    }

    /**
     * Send the message to the error topic because of a non-transient error.
     *
     * @param message the message to queue
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void queueError(final Message message) throws ExecutionException, InterruptedException {
        consumerGroup.retry(Integer.MAX_VALUE, message);
    }

    private void logRetryFailure(final String logContext, final Throwable cause, final String topic,
            final Long offset) {
        final Map<String, Object> logMap = new HashMap<>();

        logMap.put("error", cause);
        logMap.put("topic", topic);
        logMap.put("offset", offset);
        logger.error(logContext, logMap);
    }

    private Message createRetryMessage(final ChsDelta delta, final Long sourceOffset) {
        final Message retry = new Message();
        final ChsDelta payload = new ChsDelta();
        int nextAttempt = delta.getAttempt() >= getMaxRetryAttempts() ? 0 : delta.getAttempt() + 1;

        payload.setAttempt(nextAttempt);
        payload.setContextId(delta.getContextId());
        payload.setData(delta.getData());
        try {
            retry.setValue(deserializer.serialize(payload));
        }
        catch (SerializationException e) {
            logRetryFailure("Failed to create new retry message", e, retry.getTopic(), sourceOffset);
        }

        return retry;
    }

}
