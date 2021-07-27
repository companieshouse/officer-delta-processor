package uk.gov.companieshouse.officier.delta.processor.deserialise;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.deserialization.AvroDeserializer;
import uk.gov.companieshouse.kafka.exceptions.DeserializationException;
import uk.gov.companieshouse.kafka.message.Message;

@Component
public class OfficerDeltaDeserializer {
    AvroDeserializer<ChsDelta> deserializer;

    @Autowired
    public OfficerDeltaDeserializer(AvroDeserializer<ChsDelta> chsDeltaAvroDeserializer) {
        deserializer = chsDeltaAvroDeserializer;
    }

    public ChsDelta deserialize(Message kafkaMessage) throws DeserializationException {
        return deserializer.fromBinary(kafkaMessage, ChsDelta.getClassSchema());
    }
}
