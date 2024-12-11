package uk.gov.companieshouse.officer.delta.processor.serialization;

import java.nio.charset.StandardCharsets;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.logging.DataMapHolder;

/**
 * The type Chs delta serializer.
 */
@Component
public class ChsDeltaSerializer implements Serializer<Object> {

    private final Logger logger;

    /**
     * Instantiates a new Chs delta serializer.
     *
     * @param logger the logger
     */
    @Autowired
    public ChsDeltaSerializer(Logger logger) {
        this.logger = logger;
    }

    @Override
    public byte[] serialize(String topic, Object payload) {
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

                AvroSerializer<ChsDelta> avroSerializer = new AvroSerializer<>(writer,
                        encoderFactory);

                return avroSerializer.toBinary(chsDelta);
            }

            return payload.toString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception ex) {
            logger.error("Serialization exception while writing to byte array", ex,
                    DataMapHolder.getLogMap());
            throw new NonRetryableErrorException(ex);
        }
    }
}
