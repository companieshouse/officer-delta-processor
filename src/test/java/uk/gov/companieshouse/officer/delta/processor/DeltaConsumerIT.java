package uk.gov.companieshouse.officer.delta.processor;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.delta.ChsDelta;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class DeltaConsumerIT extends BaseKafkaIntegrationTest {

    @Test
    void simpleTest() throws TimeoutException {
        // Given
        List<ChsDelta> deltasProcessed = new ArrayList<>();
        setProcessor(deltasProcessed::add);

        // When
        String message = "{\"Hello\":\"World!\"}";
        sendChsDelta("officers-delta", message);

        waitUntil(() -> deltasProcessed.size() > 0, Duration.ofSeconds(10));

        // Then
        assertTrue(deltasProcessed.size() > 0);
        assertEquals(deltasProcessed.get(0).getData(), message);
    }
}
