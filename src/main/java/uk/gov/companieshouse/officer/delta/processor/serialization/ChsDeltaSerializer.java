package uk.gov.companieshouse.officer.delta.processor.serialization;

import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.logging.Logger;

import java.nio.charset.StandardCharsets;

@Component
public class ChsDeltaSerializer implements Serializer<Object> {

    private final Logger logger;

    @Autowired
    public ChsDeltaSerializer(Logger logger) {
        this.logger = logger;
    }

    @Override
    public byte[] serialize(String topic, Object payload) {
        logger.trace("Payload serialised");

        try {
            if (payload == null) {
                return "".getBytes();
            }

            if (payload instanceof byte[]) {
                return (byte[]) payload;
            }

            if (payload instanceof ChsDelta) {
                var chsDelta = (ChsDelta) payload;
                DatumWriter<ChsDelta> writer = new SpecificDatumWriter<>();
                var encoderFactory = EncoderFactory.get();

                AvroSerializer<ChsDelta> avroSerializer =
                        new AvroSerializer<>(writer, encoderFactory);

                return avroSerializer.toBinary(chsDelta);
            }

            return payload.toString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception ex) {
            logger.error("Serialization exception while writing to byte array", ex);
            throw new NonRetryableErrorException(ex);
        }
    }
}
