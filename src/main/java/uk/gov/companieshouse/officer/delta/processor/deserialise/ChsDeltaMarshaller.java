package uk.gov.companieshouse.officer.delta.processor.deserialise;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.deserialization.AvroDeserializer;
import uk.gov.companieshouse.kafka.exceptions.DeserializationException;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;

/**
 * Serializes and deserializes kafka messages using Apache Avro.
 * The serializer and deserializer are generated by the kafka-models
 * project using the schema from chs-kafka-schemas.
 */
@Component
public class ChsDeltaMarshaller {
    final AvroDeserializer<ChsDelta> deserializer;
    final AvroSerializer<ChsDelta> serializer;

    @Autowired
    public ChsDeltaMarshaller(AvroDeserializer<ChsDelta> deserializer, final AvroSerializer<ChsDelta> serializer) {
        this.deserializer = deserializer;
        this.serializer = serializer;
    }

    /**
     * Deserializes kafka message into a ChsDelta class
     *
     * @param kafkaMessage the message from kafka
     * @return the model populated by data from the message
     */
    public ChsDelta deserialize(Message kafkaMessage) {
        try {
            return deserializer.fromBinary(kafkaMessage, ChsDelta.getClassSchema());
        }
        catch (DeserializationException e) {
            throw new NonRetryableErrorException("Unable to deserialize message", e);
        }
    }

    /**
     * Serializes ChsDelta class into a sequence of bytes
     *
     * @param delta the ChsDelta
     * @return the bytes representing the data from the ChsDelta
     */
    public byte[] serialize(ChsDelta delta) {
        try {
            return serializer.toBinary(delta);
        }
        catch (SerializationException e) {
            throw new NonRetryableErrorException("Unable to serialize message", e);
        }
    }
}
