package uk.gov.companieshouse.officer.delta.processor.consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

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
        Message<ChsDelta> message = Util.createChsDeltaMessage("officer_delta_example.json", false);
        consumer.receiveMainMessages(message);

        verify(processor).process(any());
    }

    @Test
    void When_consumer_receives_valid_delete_payload_processDelete_is_called() throws IOException {
        Message<ChsDelta> message = Util.createChsDeltaMessage("officer_delete_delta.json", true);
        consumer.receiveMainMessages(message);

        verify(processor).processDelete(any());
    }

    @Test
    void When_processor_throws_exception_consumer_throws_exception() throws IOException {
        Message<ChsDelta> message = Util.createChsDeltaMessage("broken_delta.json", false);
        ChsDelta brokenMessage = message.getPayload();

        doThrow(new NonRetryableErrorException(new Exception()))
                .when(processor).process(brokenMessage);

        Assert.assertThrows(Exception.class, ()->consumer
                .receiveMainMessages(message));
    }
}