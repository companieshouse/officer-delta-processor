package uk.gov.companieshouse.officer.delta.processor;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.delta.ChsDelta;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@Disabled
@ExtendWith(MockitoExtension.class)
class OfficerDeltaConsumerIT extends BaseKafkaIntegrationTest {
    @Test
    void testCanConsumeMessage() {
        // Given
        captureDeltas();

        // When
        sendChsDelta("officers-delta", "");

        final int maxWaitTimeSeconds = 10;
        try {
            waitForDelta(Duration.ofSeconds(maxWaitTimeSeconds));
        } catch (TimeoutException e) {
            fail(String.format("No delta was consumed after %d seconds", maxWaitTimeSeconds));
        }

        // Then
        // If it gets to this point a message has been consumed and the test has passed
    }

    @Test
    void testCanDeserializeChsDelta() {
        // Given
        captureDeltas();

        // When
        String message = "{\"Hello\":\"World!\"}";
        sendChsDelta("officers-delta", message);

        final int maxWaitTimeSeconds = 10;
        ChsDelta delta = null;
        try {
            delta = waitForDelta(Duration.ofSeconds(maxWaitTimeSeconds));
        } catch (TimeoutException e) {
            fail(String.format("No delta was consumed after %d seconds", maxWaitTimeSeconds));
        }

        // Then
        assertNotNull(delta, "ChsDelta wasn't initialized for waitForDelta returned null");
        assertEquals(message, delta.getData(),
                "Data from ChsDelta should match the data sent to Kafka");
    }
}
