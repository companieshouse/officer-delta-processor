package uk.gov.companieshouse.officer.delta.processor.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.FixedDelayStrategy;
import org.springframework.messaging.Message;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.processor.Processor;


@Component
public class DeltaConsumer {

    private final Processor<ChsDelta> processor;


    /**
     * Initialise the consumer
     *
     * @param processor handles message transformation
     */
    @Autowired
    public DeltaConsumer(final Processor<ChsDelta> processor) {
        this.processor = processor;
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
        var chsDelta = message.getPayload();

        if (Boolean.TRUE.equals(chsDelta.getIsDelete())) {
            processor.processDelete(chsDelta);
        } else {
            processor.process(chsDelta);
        }
    }
}
