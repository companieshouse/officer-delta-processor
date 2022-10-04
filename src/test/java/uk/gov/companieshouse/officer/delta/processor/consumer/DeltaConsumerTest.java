package uk.gov.companieshouse.officer.delta.processor.consumer;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officer.delta.processor.Util;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.processor.Processor;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeltaConsumerTest {

    @Mock
    private Logger logger;
    @Mock
    Processor<ChsDelta> processor;
    private DeltaConsumer consumer;

    @BeforeEach
    void init() {
        consumer = new DeltaConsumer(processor,logger);
    }

    @Test
    void When_consumer_receives_valid_payload_process_is_called() throws IOException {
        Message<ChsDelta> message = Util.createChsDeltaMessage("officer_delta_example.json");
        consumer.receiveMainMessages(message, "topic", "partition", "offset");

        verify(processor).process(any());
    }

    @Test
    void When_processor_throws_exception_consumer_throws_exception() throws IOException {
        Message<ChsDelta> message = Util.createChsDeltaMessage("broken_delta.json");
        ChsDelta brokenMessage = message.getPayload();

        doThrow(new NonRetryableErrorException(new Exception()))
                .when(processor).process(eq(brokenMessage));

        Assert.assertThrows(Exception.class, ()->consumer
                .receiveMainMessages(message, "topic", "partition", "offset"));
    }
}