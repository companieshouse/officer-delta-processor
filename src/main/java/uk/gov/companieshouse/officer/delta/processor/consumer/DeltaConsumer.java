package uk.gov.companieshouse.officer.delta.processor.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.FixedDelayStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.processor.Processor;

import java.time.Duration;
import java.time.Instant;

import static java.lang.String.format;

@Component
public class DeltaConsumer {

    private final Processor<ChsDelta> processor;
    private final Logger logger;


    /**
     * Initialise the consumer
     * @param processor handles message transformation
     * @param logger the structured logger
     */
    @Autowired
    public DeltaConsumer(final Processor<ChsDelta> processor, final Logger logger) {
        this.processor = processor;
        this.logger = logger;
    }

    /**
     * Receives Main topic messages.
     */
    @RetryableTopic(attempts = "${officer.delta.processor.attempts}",
            backoff = @Backoff(delayExpression = "${officer.delta.processor.backoff-delay}"),
            fixedDelayTopicStrategy = FixedDelayStrategy.SINGLE_TOPIC,
            dltTopicSuffix = "-error",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "false",
            exclude = NonRetryableErrorException.class)
    @KafkaListener(topics = "${officer.delta.processor.topic}",
            groupId = "${kafka.odp.group.name}",
            containerFactory = "listenerContainerFactory")
    public void receiveMainMessages(org.springframework.messaging.Message<ChsDelta> message,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                    @Header(KafkaHeaders.RECEIVED_PARTITION_ID) String partition,
                                    @Header(KafkaHeaders.OFFSET) String offset) {
        var startTime = Instant.now();
        var chsDelta = message.getPayload();
        String contextId = chsDelta.getContextId();
        logger.info(format("A new message successfully picked up from topic: %s, "
                        + "partition: %s and offset: %s with contextId: %s",
                topic, partition, offset, contextId));

        try {
            if (Boolean.TRUE.equals(chsDelta.getIsDelete())) {
                processor.processDelete(chsDelta);
            } else {
                processor.process(chsDelta);
            }
            logger.info(format("Officer Delta message with contextId: %s is successfully "
                            + "processed in %d milliseconds", contextId,
                    Duration.between(startTime, Instant.now()).toMillis()));
        } catch (Exception exception) {
            logger.errorContext(contextId, format("Exception occurred while processing "
                    + "message on the topic: %s", topic), exception, null);
            throw exception;
        }
    }

}
