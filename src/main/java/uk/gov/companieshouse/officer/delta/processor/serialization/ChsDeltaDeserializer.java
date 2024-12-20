package uk.gov.companieshouse.officer.delta.processor.serialization;

import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.logging.DataMapHolder;

/**
 * The type Chs delta deserializer.
 */
@Component
public class ChsDeltaDeserializer implements Deserializer<ChsDelta> {

    private final Logger logger;

    /**
     * Instantiates a new Chs delta deserializer.
     *
     * @param logger the logger
     */
    @Autowired
    public ChsDeltaDeserializer(Logger logger) {
        this.logger = logger;
    }

    @Override
    public ChsDelta deserialize(String topic, byte[] data) {
        try {
            Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            DatumReader<ChsDelta> reader = new ReflectDatumReader<>(ChsDelta.class);
            return reader.read(null, decoder);
        } catch (Exception ex) {
            logger.error("De-Serialization exception while converting to Avro schema object", ex,
                    DataMapHolder.getLogMap());
            throw new NonRetryableErrorException(ex);
        }
    }
}
