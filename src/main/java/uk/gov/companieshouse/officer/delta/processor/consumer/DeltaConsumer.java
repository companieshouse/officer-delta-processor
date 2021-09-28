package uk.gov.companieshouse.officer.delta.processor.consumer;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StopWatch;
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
        logger.info(String.format("Closing [%s] before service shutdown.", consumerGroup.getConsumerType()));
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
            logger.trace(
                    String.format("[%s] polled %d new message(s)", consumerGroup.getConsumerType(), messages.size()));
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
            final Integer partition = deltaMessage.getPartition();
            ChsDelta delta = null;
            int attempt = 0;
            String contextId;
            StopWatch stopWatch = new StopWatch(consumerGroup.getConsumerType().toString());

            stopWatch.setKeepTaskList(false); // avoid high memory usage for large number (millions) of task intervals
            try {
                stopWatch.start("Deserialize message");
                delta = marshaller.deserialize(deltaMessage);
                stopWatch.stop();

                attempt = delta.getAttempt();
                contextId = delta.getContextId();
                logInfo(contextId,
                        String.format("%s (ms): %d", stopWatch.getLastTaskName(), stopWatch.getLastTaskTimeMillis()),
                        attempt, topic, partition, offset, stopWatch.getLastTaskTimeMillis());
                stopWatch.start("Process message");

                processor.process(delta);

                stopWatch.stop();
                logInfo(contextId,
                        String.format("%s (ms): %d", stopWatch.getLastTaskName(), stopWatch.getLastTaskTimeMillis()),
                        attempt, topic, partition, offset, stopWatch.getLastTaskTimeMillis());
                logInfo(contextId, String.format("Total process (ms): %d", stopWatch.getTotalTimeMillis()), attempt,
                        topic, partition, offset, stopWatch.getLastTaskTimeMillis());
            }
            catch (NonRetryableErrorException e) {
                contextId = Optional.ofNullable(delta).map(ChsDelta::getContextId).orElse(null);
                logError(contextId, e, attempt, topic, partition, offset);
            }
            catch (Exception e) {
                // includes RetryableErrorException; and assume any other kind of exception is retryable
                contextId = Optional.ofNullable(delta).map(ChsDelta::getContextId).orElse(null);
                logError(contextId, e, attempt, topic, partition, offset);
                if (delta != null) {
                    queueRetry(topic, partition, offset, attempt, delta);
                }
            }
            finally {
                contextId = Optional.ofNullable(delta).map(ChsDelta::getContextId).orElse(null);
                logInfo(contextId, "Commit message offset", attempt, topic, partition, offset);
                commitOffset(deltaMessage);
            }
        });

    }

    private void logInfo(final String logContext, final String msg, final Integer attempt, final String topic,
            final Integer partition, final Long offset) {
        logInfo(logContext, msg, attempt, topic, partition, offset, null);
    }

    private void logInfo(final String logContext, final String msg, final Integer attempt, final String topic,
            final Integer partition, final Long offset, final Long duration) {
        final Map<String, Object> logMap = new HashMap<>();

        logMap.put("attempt", attempt);
        logMap.put("topic", topic);
        logMap.put("partition", partition);
        logMap.put("offset", offset);
        Optional.ofNullable(duration).ifPresent(d -> logMap.put("duration", d));
        logger.infoContext(logContext, msg, logMap);
    }

    private void logError(final String logContext, final Exception e, final Integer attempt, final String topic,
            final Integer partition, final Long offset) {
        final Map<String, Object> logMap = new HashMap<>();

        logMap.put("attempt", attempt);
        logMap.put("topic", topic);
        logMap.put("partition", partition);
        logMap.put("sourceOffset", offset);
        logger.errorContext(logContext, ExceptionUtils.getRootCauseMessage(e), e, logMap);
    }

    /**
     * Send the message to the appropriate topic because of a transient error:
     * - the retry topic if retries < max retries
     * - the error topic if retries >= max retries
     *
     * @param sourceTopic  the topic of the originating message
     * @param partition    the topic partition number
     * @param sourceOffset the offset of the originating message
     * @param attempt      the attempt counter
     * @param delta        the ChsDelta payload to queue
     */
    private void queueRetry(final String sourceTopic, final Integer partition, long sourceOffset, int attempt,
            final ChsDelta delta) {
        final String contextId = delta.getContextId();

        try {
            logInfo(contextId, "Retry for source message", attempt, sourceTopic, partition, sourceOffset);

            final int nextAttempt = delta.getAttempt() >= getMaxRetryAttempts() ? 0 : delta.getAttempt() + 1;
            final Message retryMessage = createRetryMessage(delta, nextAttempt);

            consumerGroup.retry(nextAttempt, retryMessage);
            logInfo(contextId, "Created retry message", nextAttempt, retryMessage.getTopic(), partition,
                    retryMessage.getOffset());
        }
        catch (ExecutionException e) {
            logError(contextId, e, attempt, sourceTopic, partition, sourceOffset);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logError(contextId, e, attempt, sourceTopic, partition, sourceOffset);
        }
    }

    private Message createRetryMessage(final ChsDelta delta, final Integer nextAttempt) {
        final Message retry = new Message();

        retry.setValue(marshaller.serialize(new ChsDelta(delta.getData(), nextAttempt, delta.getContextId())));

        return retry;
    }

}
