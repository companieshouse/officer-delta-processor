package uk.gov.companieshouse.officer.delta.processor.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.FixedDelayStrategy;
import org.springframework.messaging.Message;
import org.springframework.retry.annotation.Backoff;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.logging.DataMapHolder;
import uk.gov.companieshouse.officer.delta.processor.processor.Processor;

import java.time.Duration;
import java.time.Instant;


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
    public void receiveMainMessages(Message<ChsDelta> message) {
        var startTime = Instant.now();
        var chsDelta = message.getPayload();
        logger.info("Starting processing an officers delta", DataMapHolder.getLogMap());

        try {
            if (Boolean.TRUE.equals(chsDelta.getIsDelete())) {
                processor.processDelete(chsDelta);
            } else {
                processor.process(chsDelta);
            }
            logger.info(String.format("Officer Delta message successfully processed in %d milliseconds",
                    Duration.between(startTime, Instant.now()).toMillis()), DataMapHolder.getLogMap());
        } catch (Exception exception) {
            logger.errorContext(chsDelta.getContextId(), "Exception occurred while processing message",
                    exception, DataMapHolder.getLogMap());
            throw exception;
        }
    }
}
