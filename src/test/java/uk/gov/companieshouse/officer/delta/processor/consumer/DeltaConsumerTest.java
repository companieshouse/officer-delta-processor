package uk.gov.companieshouse.officer.delta.processor.consumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.consumer.resilience.CHKafkaResilientConsumerGroup;
import uk.gov.companieshouse.kafka.exceptions.DeserializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officer.delta.processor.deserialise.ChsDeltaDeserializer;
import uk.gov.companieshouse.officer.delta.processor.exception.ProcessException;
import uk.gov.companieshouse.officer.delta.processor.processor.Processor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeltaConsumerTest {

    @Mock
    Logger logger;

    @Mock
    ChsDeltaDeserializer deserializer;

    @Mock
    Processor<ChsDelta> processor;

    @Mock
    CHKafkaResilientConsumerGroup chKafkaConsumerGroup;

    @Spy
    @InjectMocks
    DeltaConsumer consumer;

    private static <T> Answer<T> processException(boolean fatal) {
        return invocationOnMock -> {
            if (fatal) {
                throw ProcessException.fatal("", null);
            } else {
                throw ProcessException.nonFatal("", null);
            }
        };
    }

    @SuppressWarnings("SameParameterValue")
    private static ArgumentMatcher<Map<String, Object>> hasKey(String key) {
        return map -> map.containsKey(key);
    }

    @Test
    @DisplayName("Given no messages on the topic, pollKafka shouldn't do anything")
    void pollKafkaNoMessages() throws InterruptedException {
        // Given
        addKafkaMessages(List.of());

        consumer.pollKafka();

        verifyNoInteractions(processor, deserializer);
    }

    @Test
    @DisplayName("Given a message is on the topic, pollKafka should pass it to the deserializer and processor")
    void pollKafka() throws DeserializationException, ProcessException, InterruptedException {
        // Given
        Message message = new Message();
        addKafkaMessages(List.of(message));
        ChsDelta delta = expectDeserializeMessage(message);

        consumer.pollKafka();

        verify(deserializer).deserialize(message);
        verify(processor).process(delta);
        verify(chKafkaConsumerGroup).commit(message);
        verify(logger).info(contains("committed"), anyMap()); // Ensure commit is logged
    }

    @Test
    @DisplayName("Given an exception occurs when deserializing, the message is passed to the error topic")
    void pollKafkaDeserializeException() throws DeserializationException, InterruptedException {
        // Given
        Message message = new Message();
        addKafkaMessages(List.of(message));
        when(deserializer.deserialize(message)).thenThrow(new DeserializationException("", null));

        consumer.pollKafka();

        verify(deserializer).deserialize(message);
        verify(consumer).sendMessageToErrorTopic(message);
    }

    @Test
    @DisplayName("Given a non-fatal exception occurs when processing, the message is passed to the retry topic")
    void pollKafkaProcessorNonFatalException() throws DeserializationException, ProcessException, InterruptedException {
        // Given
        Message message = new Message();
        addKafkaMessages(List.of(message));
        ChsDelta delta = expectDeserializeMessage(message);
        doAnswer(processException(false)).when(processor).process(delta);

        consumer.pollKafka();

        verify(deserializer).deserialize(message);
        verify(consumer).sendMessageToRetryTopic(message);
    }

    @Test
    @DisplayName("Given a fatal exception occurs when processing, the message is passed to the error topic")
    void pollKafkaProcessorFatalException() throws DeserializationException, ProcessException, InterruptedException {
        // Given
        Message message = new Message();
        addKafkaMessages(List.of(message));
        ChsDelta delta = expectDeserializeMessage(message);
        doAnswer(processException(true)).when(processor).process(delta);

        consumer.pollKafka();

        verify(deserializer).deserialize(message);
        verify(consumer).sendMessageToErrorTopic(message);
    }

    private ChsDelta expectDeserializeMessage(Message message) throws DeserializationException {
        ChsDelta delta = new ChsDelta();
        when(deserializer.deserialize(message)).thenReturn(delta);
        return delta;
    }

    private void addKafkaMessages(List<Message> messages) {
        when(chKafkaConsumerGroup.consume()).thenReturn(messages);
    }

    @Test
    @DisplayName("Given a message is sent to the retry topic, the consumer should commit its offset")
    void sendMessageToRetryTopic() throws ExecutionException, InterruptedException {
        Message message = new Message();

        consumer.sendMessageToRetryTopic(message);

        verify(chKafkaConsumerGroup).retry(0, message);
        verify(chKafkaConsumerGroup).commit(message);
        verify(logger).info(contains("successfully"), anyMap());
    }

    @Test
    @DisplayName("Given a message is unable to be sent to the retry topic, its offset is not committed")
    void sendMessageToRetryTopicFailure() throws ExecutionException, InterruptedException {
        Message message = new Message();
        doThrow(new ExecutionException(null)).when(chKafkaConsumerGroup).retry(0, message);

        consumer.sendMessageToRetryTopic(message);

        verify(chKafkaConsumerGroup, atLeastOnce()).retry(0, message);
        verify(chKafkaConsumerGroup, never()).commit(message);
        verify(logger, atLeastOnce()).error(contains("Error"), any(ExecutionException.class),
                argThat(hasKey("kafkaMessage")));
    }

    @Test
    @DisplayName("Given the consumer is interrupted while trying to send a message to the retry topic, ")
    void sendMessageToRetryTopicInterrupt() throws ExecutionException, InterruptedException {
        Message message = new Message();
        doThrow(new InterruptedException(null)).when(chKafkaConsumerGroup).retry(0, message);

        consumer.sendMessageToRetryTopic(message);

        verify(chKafkaConsumerGroup, atLeastOnce()).retry(0, message);
        verify(chKafkaConsumerGroup, never()).commit(message);
        verify(logger, atLeastOnce()).error(contains("Interrupted"), any(InterruptedException.class),
                argThat(hasKey("kafkaMessage")));
    }

    @Test
    @SuppressWarnings("squid:S2699")
    void sendMessageToErrorTopic() {

    }

    @Test
    void destroy() {
        consumer.destroy();
        verify(chKafkaConsumerGroup).close();
    }
}