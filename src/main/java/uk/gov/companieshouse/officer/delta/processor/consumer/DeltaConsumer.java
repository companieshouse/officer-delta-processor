package uk.gov.companieshouse.officer.delta.processor.consumer;

import org.springframework.scheduling.annotation.Scheduled;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.consumer.resilience.CHConsumerType;
import uk.gov.companieshouse.kafka.consumer.resilience.CHKafkaResilientConsumerGroup;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officer.delta.processor.deserialise.ChsDeltaMarshaller;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.processor.Processor;

import javax.annotation.PreDestroy;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class DeltaConsumer {
    private final CHKafkaResilientConsumerGroup consumerGroup;
    private final ChsDeltaMarshaller marshaller;
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
    public DeltaConsumer(final CHKafkaResilientConsumerGroup consumerGroup, final ChsDeltaMarshaller marshaller,
            final Processor<ChsDelta> processor, final Logger logger) {
        this.consumerGroup = consumerGroup;
        this.marshaller = marshaller;
        this.processor = processor;
        this.logger = logger;

        consumerGroup.connect();
    }

    /**
     * Clean up consumer connections when the program ends.
     */
    @PreDestroy
    public void destroy() {
        consumerGroup.close();
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
    Optional<Message> getNextDeltaMessage() {
        if (messages.isEmpty()) {
            messages.addAll(consumerGroup.consume());
            logger.debug(String.format("Polled %d new message(s)", messages.size()));
        }

        return Optional.ofNullable(messages.pollFirst());
    }

    public int getPendingMessageCount() {
        return messages.size();
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
    void consumeMessage() {
        final Optional<Message> nextDeltaMessage = getNextDeltaMessage();

        nextDeltaMessage.ifPresent(deltaMessage -> {
            final String topic = deltaMessage.getTopic();
            final Long offset = deltaMessage.getOffset();
            ChsDelta delta = null;
            int attempt = 0;

            try {
                delta = marshaller.deserialize(deltaMessage);

                attempt = delta.getAttempt();
                logInfo("Consume message", attempt, topic, offset);
                processor.process(delta);
            }
            catch (NonRetryableErrorException e) {
                logError("Non-retryable error while processing message", e, attempt, topic, offset);
            }
            catch (Exception e) {
                // includes RetryableErrorException; and assume any other kind of exception is retryable
                logError("Retryable error while processing message", e, attempt, topic, offset);
                queueRetry(topic, offset, attempt, delta);
            }
            finally {
                logInfo("Commit message", attempt, topic, offset);
                commitOffset(deltaMessage);
            }
        });

    }

    private void logInfo(final String logContext, final Integer attempt, final String topic, final Long offset) {
        final Map<String, Object> logMap = new HashMap<>();

        logMap.put("attempt", attempt);
        logMap.put("topic", topic);
        logMap.put("offset", offset);
        logger.info(logContext, logMap);
    }

    private void logError(final String logContext, final Throwable cause, final Integer attempt, final String topic,
            final Long offset) {
        final Map<String, Object> logMap = new HashMap<>();

        logMap.put("error", cause);
        logMap.put("attempt", attempt);
        logMap.put("topic", topic);
        logMap.put("sourceOffset", offset);
        logger.error(logContext, logMap);
    }

    /**
     * Send the message to the appropriate topic because of a transient error:
     * - the retry topic if retries < max retries
     * - the error topic if retries >= max retries
     *
     * @param sourceTopic  the topic of the originating message
     * @param sourceOffset the offset of the originating message
     * @param attempt      the attempt counter
     * @param delta        the ChsDelta payload to queue
     */
    private void queueRetry(final String sourceTopic, long sourceOffset, int attempt, final ChsDelta delta) {
        try {
            logInfo("Retry for source message", attempt, sourceTopic, sourceOffset);

            final int nextAttempt = delta.getAttempt() >= getMaxRetryAttempts() ? 0 : delta.getAttempt() + 1;
            final Message retryMessage = createRetryMessage(delta, nextAttempt);

            consumerGroup.retry(nextAttempt, retryMessage);
            logInfo("Created retry message", nextAttempt, retryMessage.getTopic(), retryMessage.getOffset());
        }
        catch (ExecutionException e) {
            logError("Failed to produce message for Retry topic", e, attempt, sourceTopic, sourceOffset);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logError("Failed to produce message for Retry topic", e, attempt, sourceTopic, sourceOffset);
        }
    }

    private Message createRetryMessage(final ChsDelta delta, final Integer nextAttempt) {
        final Message retry = new Message();

        retry.setValue(marshaller.serialize(new ChsDelta(delta.getData(), nextAttempt, delta.getContextId())));

        return retry;
    }

}
