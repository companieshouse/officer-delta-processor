package uk.gov.companieshouse.officier.delta.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.consumer.CHKafkaPartitionConsumer;
import uk.gov.companieshouse.kafka.exceptions.DeserializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.officier.delta.processor.deserialise.OfficerDeltaDeserializer;

import java.util.function.Consumer;

@Component
public class DeltaConsumer {
    public static final int POLLING_DURATION_MS = 500;
    // TODO: add graceful shutdown https://www.oreilly.com/library/view/kafka-the-definitive/9781491936153/ch04.html#:~:text=But-,How,-Do%20We%20Exit
    CHKafkaPartitionConsumer consumer;
    OfficerDeltaDeserializer deserializer;

    @Autowired
    public DeltaConsumer(
            CHKafkaPartitionConsumer chKafkaPartitionConsumer,
            OfficerDeltaDeserializer deserializer) {
        consumer = chKafkaPartitionConsumer;
        this.deserializer = deserializer;
    }

    @Scheduled(fixedRate = POLLING_DURATION_MS)
    void processMessages(Consumer<ChsDelta> deltaConsumer) throws DeserializationException {
        for (Message message : consumer.consume()) {
            ChsDelta delta = deserializer.deserialize(message);
            deltaConsumer.accept(delta);
        }
    }

//    public void start() {
//        while ()
//    }
}

