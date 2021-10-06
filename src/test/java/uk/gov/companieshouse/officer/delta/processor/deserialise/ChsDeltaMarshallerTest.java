package uk.gov.companieshouse.officer.delta.processor.deserialise;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import org.apache.avro.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.deserialization.AvroDeserializer;
import uk.gov.companieshouse.kafka.deserialization.DeserializerFactory;
import uk.gov.companieshouse.kafka.exceptions.DeserializationException;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;

import java.nio.charset.StandardCharsets;

@ExtendWith(MockitoExtension.class)
class ChsDeltaMarshallerTest {

    public static final String DATA = "{\"Hello\": \"World!\"}";
    public static final int ATTEMPT = 0;
    public static final String CONTEXT_ID = "context_id";


    private final AvroDeserializer<ChsDelta> chsDeltaAvroDeserializer =
            new DeserializerFactory().getSpecificRecordDeserializer(ChsDelta.class);
    private final AvroSerializer<ChsDelta> chsDeltaAvroSerializer =
            new SerializerFactory().getSpecificRecordSerializer(ChsDelta.class);

    ChsDeltaMarshaller testMarshaller;

    private static Message messageOf(String data, int attempt, String contextId) throws SerializationException {
        Message message = new Message();

        message.setValue(bytesOf(data, attempt, contextId));

        return message;
    }

    private static byte[] bytesOf(String data, int attempt, String contextId) throws SerializationException {
        AvroSerializer<ChsDelta> serializer = new SerializerFactory().getSpecificRecordSerializer(ChsDelta.class);

        return serializer.toBinary(deltaOf(data, attempt, contextId));
    }

    private static ChsDelta deltaOf(final String data, final int attempt, final String contextId) {
        return new ChsDelta(data, attempt, contextId);
    }

    @BeforeEach
    void setUp() {
        testMarshaller = new ChsDeltaMarshaller(chsDeltaAvroDeserializer, chsDeltaAvroSerializer);
    }

    @Test
    @DisplayName("Deserializes messages into ChsDeltas")
    void deserialize() throws SerializationException, NonRetryableErrorException {
        Message message = messageOf(DATA, ATTEMPT, CONTEXT_ID);

        ChsDelta delta = testMarshaller.deserialize(message);

        assertThat(delta.getAttempt(), is(ATTEMPT));
        assertThat(delta.getData(), is(DATA));
        assertThat(delta.getContextId(), is(CONTEXT_ID));
    }

    @Test
    void deserializeFailure() throws DeserializationException {
        final AvroDeserializer<ChsDelta> spyDeserializer = spy(chsDeltaAvroDeserializer);
        final ChsDeltaMarshaller spyMarshaller = new ChsDeltaMarshaller(spyDeserializer, chsDeltaAvroSerializer);
        final byte[] bytes = "deserialize_failure".getBytes(StandardCharsets.UTF_8);
        Message message = new Message();

        message.setValue(bytes);

        doThrow(new DeserializationException("deserialization failed", null)).when(spyDeserializer)
                .fromBinary(any(Message.class), any(Schema.class));

        assertThrows(NonRetryableErrorException.class, () -> spyMarshaller.deserialize(message));
    }

    @Test
    @DisplayName("Serializes ChsDeltas into byte[] message payload")
    void serialize() throws SerializationException, NonRetryableErrorException {
        final ChsDelta delta = deltaOf(DATA, ATTEMPT, CONTEXT_ID);
        final byte[] expected = bytesOf(DATA, ATTEMPT, CONTEXT_ID);

        byte[] bytes = testMarshaller.serialize(delta);

        assertThat(bytes, is(equalTo(expected)));
    }

    @Test
    void serializeFailure() throws SerializationException {
        final AvroSerializer<ChsDelta> spySerializer = spy(chsDeltaAvroSerializer);
        final ChsDeltaMarshaller spyMarshaller = new ChsDeltaMarshaller(chsDeltaAvroDeserializer, spySerializer);
        final byte[] bytes = "serialize_failure".getBytes(StandardCharsets.UTF_8);

        final ChsDelta delta = deltaOf(DATA, ATTEMPT, CONTEXT_ID);

        doThrow(new SerializationException("serialization failed", null)).when(spySerializer)
                .toBinary(delta);

        assertThrows(NonRetryableErrorException.class, () -> spyMarshaller.serialize(delta));
    }

}
