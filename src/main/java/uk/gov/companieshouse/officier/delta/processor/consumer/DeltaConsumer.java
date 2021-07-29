package uk.gov.companieshouse.officier.delta.processor.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.consumer.resilience.CHKafkaResilientConsumerGroup;
import uk.gov.companieshouse.kafka.exceptions.DeserializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officier.delta.processor.deserialise.OfficerDeltaDeserializer;
import uk.gov.companieshouse.officier.delta.processor.exception.ProcessException;
import uk.gov.companieshouse.officier.delta.processor.processr.Processor;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

@Component
public class DeltaConsumer {
    public static final int POLLING_DURATION_MS = 5 * 1000;

    // TODO: add graceful shutdown https://www.oreilly.com/library/view/kafka-the-definitive/9781491936153/ch04.html#:~:text=But-,How,-Do%20We%20Exit
    private final CHKafkaResilientConsumerGroup consumer;
    private final OfficerDeltaDeserializer deserializer;
    private final Logger logger;
    private Processor processor;

    @Autowired
    public DeltaConsumer(
            CHKafkaResilientConsumerGroup chKafkaConsumerGroup,
            OfficerDeltaDeserializer deserializer,
            Logger logger,
            Processor processor) {

        this.consumer = chKafkaConsumerGroup;
        this.deserializer = deserializer;
        this.logger = logger;
        this.processor = processor;

        consumer.connect();
    }

    @Scheduled(fixedRate = POLLING_DURATION_MS)
    void pollKafka() {
        logger.trace("Polling");
        // TODO: log index and when commit
        // TODO: log retry and failure / error
        // TODO: retry and error topics

        for (Message message : consumer.consume()) {
            Map<String, Object> info = new HashMap<>();
            info.put("message", message);
            info.put("partition", message.getPartition());
            info.put("offset", message.getOffset());
            logger.debug("Received message", info);

            try {
                ChsDelta delta = deserializer.deserialize(message);
                processor.process(delta);
                consumer.commit(message);
            } catch (DeserializationException e) {
                // TODO: push message to error topic
                consumer.reprocess(message);
            } catch (ProcessException e) {
                // TODO: check if can retry, and push to relevant topic
            }


        }
    }

    public void setProcessor(Processor processor) {
        this.processor = processor;
    }

    @PreDestroy
    public void destroy() {
        consumer.close();
    }
}