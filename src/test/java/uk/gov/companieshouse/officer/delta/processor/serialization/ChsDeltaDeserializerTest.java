package uk.gov.companieshouse.officer.delta.processor.serialization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ChsDeltaDeserializerTest {

    @Mock
    private Logger logger;
    private ChsDeltaDeserializer deserializer;

    @BeforeEach
    public void init() {
        deserializer = new ChsDeltaDeserializer(logger);
    }

    @Test
    void When_deserialize_Expect_ValidChsDeltaObject() {
        ChsDelta chsDelta = new ChsDelta("{\"key\": \"value\"}", 1, "context_id");
        byte[] data = encodedData(chsDelta);

        ChsDelta deserializedObject = deserializer.deserialize("", data);

        assertThat(deserializedObject).isEqualTo(chsDelta);
    }

    @Test
    void When_deserializeFails_throwsNonRetryableError() {
        byte[] data = "Invalid message".getBytes();
        assertThrows(NonRetryableErrorException.class, () -> deserializer.deserialize("", data));
    }

    private byte[] encodedData(ChsDelta chsDelta) {
        ChsDeltaSerializer serializer = new ChsDeltaSerializer(this.logger);
        return serializer.serialize("", chsDelta);
    }
}