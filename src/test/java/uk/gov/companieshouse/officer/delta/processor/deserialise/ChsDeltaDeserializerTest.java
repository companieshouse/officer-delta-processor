package uk.gov.companieshouse.officer.delta.processor.deserialise;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.deserialization.AvroDeserializer;
import uk.gov.companieshouse.kafka.deserialization.DeserializerFactory;
import uk.gov.companieshouse.kafka.exceptions.DeserializationException;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChsDeltaDeserializerTest {

    public static final String data = "{\"Hello\": \"World!\"}";
    public static final int attempt = 0;
    public static final String CONTEXT_ID = "context_id";


    final AvroDeserializer<ChsDelta> chsDeltaAvroDeserializer = new DeserializerFactory()
            .getSpecificRecordDeserializer(ChsDelta.class);

    ChsDeltaDeserializer deserializer;

    private static Stream<Arguments> deserializeParameters() {
        return Stream.of(
                Arguments.of(data, attempt, CONTEXT_ID)
        );
    }

    private static Message messageOf(String data, int attempt, String contextId) throws SerializationException {
        Message message = new Message();
        AvroSerializer<ChsDelta> serializer = new SerializerFactory()
                .getSpecificRecordSerializer(ChsDelta.class);

        message.setValue(serializer.toBinary(new ChsDelta(data, attempt, contextId)));

        return message;
    }

    @BeforeEach
    void setUp() {
        deserializer = new ChsDeltaDeserializer(chsDeltaAvroDeserializer);
    }

    @ParameterizedTest
    @MethodSource("deserializeParameters")
    @DisplayName("Deserializes messages into ChsDeltas")
    void deserialize(String data, int attempt, String contextId) throws SerializationException, DeserializationException {
        Message message = messageOf(data, attempt, contextId);

        ChsDelta delta = deserializer.deserialize(message);

        assertEquals(delta.getAttempt(), attempt);
        assertEquals(delta.getData(), data);
        assertEquals(delta.getContextId(), contextId);
    }
}