package uk.gov.companieshouse.officer.delta.processor.consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.consumer.resilience.CHConsumerType;
import uk.gov.companieshouse.kafka.consumer.resilience.CHKafkaResilientConsumerGroup;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officer.delta.processor.deserialise.ChsDeltaMarshaller;
import uk.gov.companieshouse.officer.delta.processor.exception.NonRetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.exception.RetryableErrorException;
import uk.gov.companieshouse.officer.delta.processor.processor.Processor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class DeltaConsumerTest {
    private static final Long EXPECTED = 5L;
    private static final String MAIN_TOPIC = "MAIN";
    private static final int MSG_COUNT = 3;
    private static final String CONTEXT_ID = "context_id";
    private static final long RETRY_THROTTLE_RATE_SECONDS = 1;

    private DeltaConsumer testConsumer;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS) // for stubbing chained calls
    private CHKafkaResilientConsumerGroup consumerGroup;
    @Mock
    private ChsDeltaMarshaller marshaller;
    @Mock
    private Processor<ChsDelta> processor;
    @Mock
    private Logger logger;
    @Mock
    private Message message;
    @Captor
    ArgumentCaptor<Message> captor;

    private List<Message> messageList;

    @BeforeEach
    void setUp() {
        testConsumer = new DeltaConsumer(consumerGroup, marshaller, processor, logger);
        messageList = generateMessageList(MAIN_TOPIC, 0, MSG_COUNT);
    }

    @Test
    void constructorConnects() {
        verify(consumerGroup).connect();
    }

    @Test
    void commitOffset() {
        testConsumer.commitOffset();

        verify(consumerGroup).commit();
    }

    @Test
    void getPendingMessageCount() {
        assertThat(testConsumer.getPendingMessageCount(), is(0));
    }

    @Test
    void getNextDeltaMessageWhenMessagesEmpty() {
        when(consumerGroup.consume()).thenReturn(messageList);

        final Optional<Message> next = testConsumer.getNextDeltaMessage();

        assertThat(next.isPresent(), is(true));
        assertThat(next.get().getOffset(), is(0L));
        assertThat(testConsumer.getPendingMessageCount(), is(MSG_COUNT - 1));
    }

    @Test
    void getNextDeltaMessageWhenMessagesNonEmpty() {

        when(consumerGroup.consume()).thenReturn(messageList);
        testConsumer.getNextDeltaMessage();

        Optional<Message> next = testConsumer.getNextDeltaMessage();

        assertThat(next.isPresent(), is(true));
        assertThat(next.get().getOffset(), is(1L));
        assertThat(testConsumer.getPendingMessageCount(), is(MSG_COUNT - 2));
    }

    @Test
    void getRetryThrottle() {
        when(consumerGroup.getConfig().getRetryThrottle()).thenReturn(RETRY_THROTTLE_RATE_SECONDS);

        assertThat(testConsumer.getRetryThrottle(), is(RETRY_THROTTLE_RATE_SECONDS));
    }

    @Test
    void getMaxRetryAttempts() {
        when(consumerGroup.getConfig().getMaxRetries()).thenReturn(EXPECTED.intValue());

        assertThat(testConsumer.getMaxRetryAttempts(), is(EXPECTED.intValue()));
    }

    @Test
    void stopAtOffset() {
        when(consumerGroup.stopAtOffset()).thenReturn(EXPECTED);

        assertThat(testConsumer.stopAtOffset(), is(EXPECTED));
    }

    @Test
    void getConsumerType() {
        when(consumerGroup.getConsumerType()).thenReturn(CHConsumerType.MAIN_CONSUMER);
        assertThat(testConsumer.getConsumerType(), is(CHConsumerType.MAIN_CONSUMER));
    }

    @Test
    void consumeMessageWhenMessagesEmpty() {
        when(consumerGroup.consume()).thenReturn(Collections.emptyList());

        testConsumer.consumeMessage();

        verify(consumerGroup).connect();
        verify(consumerGroup).getConsumerType();
        verifyNoMoreInteractions(consumerGroup);
        verifyNoInteractions(marshaller, processor);
    }

    @ParameterizedTest
    @EnumSource(value = CHConsumerType.class, names = {"MAIN_CONSUMER", "RETRY_CONSUMER"})
    void consumeMessageWhenMessagesNonEmpty(final CHConsumerType consumerType) {
        when(consumerGroup.getConsumerType()).thenReturn(consumerType);
        when(consumerGroup.consume()).thenReturn(new ArrayList<>(messageList));

        final Message nextMessage = messageList.get(0);
        final ChsDelta delta = createDelta(5);
        when(marshaller.deserialize(nextMessage)).thenReturn(delta);

        testConsumer.consumeMessage();

        final InOrder inOrder = inOrder(consumerGroup, marshaller, processor, logger);

        inOrder.verify(consumerGroup).connect();
        inOrder.verify(marshaller).deserialize(nextMessage);
        if (CHConsumerType.RETRY_CONSUMER == consumerType) {
            inOrder.verify(logger).infoContext(eq(CONTEXT_ID), startsWith("Pausing thread"), isNull());
        }
        inOrder.verify(processor).process(delta);
        inOrder.verify(consumerGroup).commit();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void consumeMainMessageWhenDeltaNull() throws ExecutionException, InterruptedException {
        when(consumerGroup.getConsumerType()).thenReturn(CHConsumerType.MAIN_CONSUMER);
        when(consumerGroup.consume()).thenReturn(new ArrayList<>(messageList));

        final Message nextMessage = messageList.get(0);
        final ChsDelta delta = createDelta(5);
        when(marshaller.deserialize(nextMessage)).thenReturn(null);

        testConsumer.consumeMessage();

        final InOrder inOrder = inOrder(consumerGroup, marshaller, processor, logger);

        inOrder.verify(consumerGroup).connect();
        inOrder.verify(marshaller).deserialize(nextMessage);
        inOrder.verify(consumerGroup, never()).retry(anyInt(), any(Message.class));
        inOrder.verify(consumerGroup).commit();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void consumeRetryMessageWhenPauseInterrupted() {
        when(consumerGroup.getConsumerType()).thenReturn(CHConsumerType.RETRY_CONSUMER);
        when(consumerGroup.getConfig().getRetryThrottle()).thenReturn(1000L);
        when(consumerGroup.consume()).thenReturn(new ArrayList<>(messageList));

        final Message nextMessage = messageList.get(0);
        final ChsDelta delta = createDelta(5);
        when(marshaller.deserialize(nextMessage)).thenReturn(delta);

        Thread.currentThread().interrupt();
        testConsumer.consumeMessage();

        final InOrder inOrder = inOrder(consumerGroup, marshaller, processor, logger);

        inOrder.verify(consumerGroup).connect();
        inOrder.verify(marshaller).deserialize(nextMessage);
        inOrder.verify(logger).infoContext(eq(CONTEXT_ID), startsWith("Pausing thread"), isNull());
        inOrder.verify(logger).errorContext(eq(CONTEXT_ID), anyString(), isA(InterruptedException.class), isNull());
        inOrder.verify(processor).process(delta);
        inOrder.verify(consumerGroup).commit();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void consumeMessageWhenDeserializeFails() throws ExecutionException, InterruptedException {
        final Message nextMessage = messageList.get(0);

        when(consumerGroup.consume()).thenReturn(new ArrayList<>(messageList));
        when(marshaller.deserialize(nextMessage)).thenThrow(new NonRetryableErrorException("deserialize() failed", null));

        testConsumer.consumeMessage();

        final InOrder inOrder = inOrder(consumerGroup, marshaller, processor, logger);

        inOrder.verify(consumerGroup).connect();
        inOrder.verify(marshaller).deserialize(nextMessage);
        inOrder.verify(logger).errorContext(isNull(), anyString(), any(Exception.class), anyMap());
        inOrder.verify(consumerGroup, never()).retry(anyInt(), any(Message.class));
        inOrder.verify(consumerGroup).commit();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void consumeMessageWhenProcessingNonTransientError() {
        when(consumerGroup.consume()).thenReturn(new ArrayList<>(messageList));

        final Message nextMessage = messageList.get(0);
        final ChsDelta delta = createDelta(5);
        when(marshaller.deserialize(nextMessage)).thenReturn(delta);
        doThrow(new NonRetryableErrorException("process() failed", null)).when(processor).process(delta);

        testConsumer.consumeMessage();

        final InOrder inOrder = inOrder(consumerGroup, marshaller, processor, logger);

        inOrder.verify(consumerGroup).connect();
        inOrder.verify(marshaller).deserialize(nextMessage);
        inOrder.verify(processor).process(delta);
        inOrder.verify(logger).errorContext(eq(CONTEXT_ID), anyString(), any(Exception.class), anyMap());
        inOrder.verify(consumerGroup).commit();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void consumeMessageWhenUnexpectedException() throws Exception {
        when(consumerGroup.getConfig().getMaxRetries()).thenReturn(EXPECTED.intValue());
        when(consumerGroup.consume()).thenReturn(new ArrayList<>(messageList));

        final Message nextMessage = messageList.get(0);
        final ChsDelta delta = createDelta(4);
        final ChsDelta retry = createDelta(5);
        final byte[] messageBytes = "retry_serialized".getBytes(StandardCharsets.UTF_8);

        when(marshaller.deserialize(nextMessage)).thenReturn(delta);
        when(marshaller.serialize(retry)).thenReturn(messageBytes);
        doThrow(new IllegalStateException("unexpected")).when(processor).process(delta);

        testConsumer.consumeMessage();

        final InOrder inOrder = inOrder(consumerGroup, marshaller, processor, logger);

        inOrder.verify(consumerGroup).connect();
        inOrder.verify(marshaller).deserialize(nextMessage);
        inOrder.verify(processor).process(delta);
        inOrder.verify(logger).errorContext(eq(CONTEXT_ID), anyString(), any(Exception.class), anyMap());
        inOrder.verify(marshaller).serialize(retry);
        inOrder.verify(consumerGroup).retry(anyInt(), any(Message.class));
        inOrder.verify(consumerGroup).commit();
        inOrder.verifyNoMoreInteractions();
    }

    static private Stream<Arguments> provideRetries() {
        return Stream.of(Arguments.of(4, 5), Arguments.of(5, 0));
    }

    @ParameterizedTest(name="For max retries = 5, after {0} attempts, next attempt is {1}")
    @MethodSource("provideRetries")
    void consumeMessageWhenProcessingTransientError(final int attempt, final int nextAttempt)
            throws ExecutionException, InterruptedException {
        when(consumerGroup.getConfig().getMaxRetries()).thenReturn(EXPECTED.intValue());
        when(consumerGroup.consume()).thenReturn(new ArrayList<>(messageList));

        final Message nextMessage = messageList.get(0);
        final ChsDelta delta = createDelta(attempt);
        final ChsDelta retry = createDelta(nextAttempt);
        final byte[] messageBytes = "retry_serialized".getBytes(StandardCharsets.UTF_8);

        when(marshaller.deserialize(nextMessage)).thenReturn(delta);
        when(marshaller.serialize(retry)).thenReturn(messageBytes);
        doThrow(new RetryableErrorException("process() failed", null)).when(processor).process(delta);

        testConsumer.consumeMessage();

        final InOrder inOrder = inOrder(consumerGroup, marshaller, processor, logger);

        inOrder.verify(consumerGroup).connect();
        inOrder.verify(marshaller).deserialize(nextMessage);
        inOrder.verify(processor).process(delta);
        inOrder.verify(logger).errorContext(eq(CONTEXT_ID), anyString(), any(Exception.class), anyMap());
        inOrder.verify(marshaller).serialize(retry);
        inOrder.verify(consumerGroup).retry(eq(nextAttempt), captor.capture());
        inOrder.verify(consumerGroup).commit();
        inOrder.verifyNoMoreInteractions();
        assertThat(captor.getValue().getValue(), is(messageBytes));
    }

    private static Stream<Arguments> provideRetryExceptions() {
        return Stream.of(Arguments.of(new ExecutionException("execution failed", null)),
                Arguments.of(new InterruptedException("thread interrupted")));
    }

    @ParameterizedTest(name="queueRetry failure: {0}")
    @MethodSource("provideRetryExceptions")
    void consumeMessageWhenProcessingNonTransientErrorThenRetryFailure(final Exception exception)
            throws ExecutionException, InterruptedException {
        when(consumerGroup.getConfig().getMaxRetries()).thenReturn(EXPECTED.intValue());
        when(consumerGroup.consume()).thenReturn(new ArrayList<>(messageList));
        doThrow(exception).when(consumerGroup).retry(eq(1), any(Message.class));

        final Message nextMessage = messageList.get(0);
        final ChsDelta delta = createDelta(0);
        final ChsDelta retry = createDelta(1);
        final byte[] messageBytes = "retry_serialized".getBytes(StandardCharsets.UTF_8);

        when(marshaller.deserialize(nextMessage)).thenReturn(delta);
        when(marshaller.serialize(retry)).thenReturn(messageBytes);
        doThrow(new RetryableErrorException("process() failed", null)).when(processor).process(delta);

        testConsumer.consumeMessage();

        final InOrder inOrder = inOrder(consumerGroup, marshaller, processor, logger);

        inOrder.verify(consumerGroup).connect();
        inOrder.verify(marshaller).deserialize(nextMessage);
        inOrder.verify(processor).process(delta);
        inOrder.verify(logger).errorContext(eq(CONTEXT_ID), anyString(), any(Exception.class), anyMap()); // log process failure
        inOrder.verify(marshaller).serialize(retry);
        inOrder.verify(consumerGroup).retry(eq(1), captor.capture());
        inOrder.verify(logger).errorContext(eq(CONTEXT_ID), anyString(), any(Exception.class), anyMap()); // log retry failure
        inOrder.verify(consumerGroup).commit();
        inOrder.verifyNoMoreInteractions();
        assertThat(captor.getValue().getValue(), is(messageBytes));
    }

    private ChsDelta createDelta(final int attempt) {
        return new ChsDelta("data", attempt, "context_id");
    }

    @Test
    void destroy() {
        testConsumer.destroy();

        verify(consumerGroup).close();
    }

    private List<Message> generateMessageList(final String topic, final int firstOffset, final int lastOffset) {
        return LongStream.range(firstOffset, lastOffset).mapToObj(i -> {
            Message m = new Message();

            m.setTopic(topic);
            m.setOffset(i);

            return m;
        }).collect(Collectors.toList());
    }
}
