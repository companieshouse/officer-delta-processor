package uk.gov.companieshouse.officier.delta.processor.processor;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.exceptions.DeserializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.officier.delta.processor.deserialise.ChsDeltaDeserializer;
import uk.gov.companieshouse.officier.delta.processor.exception.ProcessException;

/**
 *
 */
@Component
public class DeserializeProcessor implements Processor<Message> {
    Processor<ChsDelta> deltaProcessor;
    ChsDeltaDeserializer deserializer;

    public DeserializeProcessor(Processor<ChsDelta> deltaProcessor,
                                ChsDeltaDeserializer deserializer) {
        this.deltaProcessor = deltaProcessor;
        this.deserializer = deserializer;
    }

    @Override
    public void process(Message message) throws ProcessException {
        try {
            ChsDelta delta = deserializer.deserialize(message);
            deltaProcessor.process(delta);
        } catch (DeserializationException e) {
            throw ProcessException.fatal(
                    "Unable to deserialize kafka message into a ChsDelta", e);
        }
    }
}
