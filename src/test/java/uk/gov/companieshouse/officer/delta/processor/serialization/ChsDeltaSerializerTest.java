package uk.gov.companieshouse.officer.delta.processor.serialization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChsDeltaSerializerTest {

    @Mock
    private Logger logger;
    private ChsDeltaSerializer serializer;

    @BeforeEach
    void init() {
        serializer = new ChsDeltaSerializer(logger);
    }

    @Test
    void whenSerializeExpectChsDeltaBytes() {
        ChsDelta chsDelta = new ChsDelta("{\"key\": \"value\"}", 1, "context_id", false);

        byte[] result = serializer.serialize("", chsDelta);

        assertThat(decodedData(result)).isEqualTo(chsDelta);
    }

    @Test
    void whenSerializeNullReturnsNull() {
        byte[] serialize = serializer.serialize("", null);
        assertThat(serialize).isEqualTo("".getBytes());
    }

    @Test
    void whenSerializeReceivesBytesReturnsBytes() {
        byte[] byteExample = "Example bytes".getBytes();
        byte[] serialize = serializer.serialize("", byteExample);
        assertThat(serialize).isEqualTo(byteExample);
    }

    private ChsDelta decodedData(byte[] chsDelta) {
        ChsDeltaDeserializer deltaSerializer = new ChsDeltaDeserializer(this.logger);
        return deltaSerializer.deserialize("", chsDelta);
    }

    @Test
    void whenSerializationExceptionOccursThrowsNonRetryableErrorException() {
        Object invalidPayload = new Object() {
            @Override
            public String toString() {
                throw new RuntimeException("toString failure");
            }
        };

        NonRetryableErrorException thrown = assertThrows(
            NonRetryableErrorException.class,
            () -> serializer.serialize("", invalidPayload)
        );

        assertThat(thrown.getCause()).isInstanceOf(RuntimeException.class);
        verify(logger).error(eq("Serialization exception while writing to byte array"), any(), any());
    }

}
