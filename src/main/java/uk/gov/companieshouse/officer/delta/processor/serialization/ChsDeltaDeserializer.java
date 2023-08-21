package uk.gov.companieshouse.officer.delta.processor.serialization;

import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officer.delta.processor.logging.DataMapHolder;

@Component
public class ChsDeltaDeserializer implements Deserializer<ChsDelta> {

    private final Logger logger;

    @Autowired
    public ChsDeltaDeserializer(Logger logger) {
        this.logger = logger;
    }

    @Override
    public ChsDelta deserialize(String topic, byte[] data) {
        try {
            logger.trace(String.format("Message picked up from topic: %s", topic), DataMapHolder.getLogMap());
            Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            DatumReader<ChsDelta> reader = new ReflectDatumReader<>(ChsDelta.class);
            var chsDelta = reader.read(null, decoder);
            logger.trace("Message successfully de-serialised into Avro ChsDelta object", DataMapHolder.getLogMap());
            return chsDelta;
        } catch (Exception ex) {
            logger.error("De-Serialization exception while converting to Avro schema object", ex, DataMapHolder.getLogMap());
            throw new NonRetryableErrorException(ex);
        }
    }

}
