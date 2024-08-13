package uk.gov.companieshouse.officer.delta.processor.logging;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.officer.delta.processor.OfficerDeltaProcessorApplication;
import uk.gov.companieshouse.officer.delta.processor.exception.RetryableErrorException;

@Component
@Aspect
class StructuredLoggingKafkaListenerAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            OfficerDeltaProcessorApplication.NAMESPACE);

    private static final String LOG_MESSAGE_RECEIVED = "Processing delta";
    private static final String LOG_MESSAGE_PROCESSED = "Processed delta";
    private static final String EXCEPTION_MESSAGE = "%s exception thrown: %s";

    @Around("@annotation(org.springframework.kafka.annotation.KafkaListener)")
    public Object manageStructuredLogging(ProceedingJoinPoint joinPoint)
        throws Throwable {

        try {
            Message<?> message = (Message<?>) joinPoint.getArgs()[0];
            DataMapHolder.initialise(extractContextId(message.getPayload())
                    .orElse(UUID.randomUUID().toString()));

            DataMapHolder.get()
                    .topic((String) message.getHeaders().get("kafka_receivedTopic"))
                    .partition((Integer) message.getHeaders().get("kafka_receivedPartitionId"))
                    .offset((Long)message.getHeaders().get("kafka_offset"));

            LOGGER.info(LOG_MESSAGE_RECEIVED, DataMapHolder.getLogMap());

            Object result = joinPoint.proceed();

            LOGGER.info(LOG_MESSAGE_PROCESSED, DataMapHolder.getLogMap());

            return result;
        } catch (RetryableErrorException ex) {
            LOGGER.info(String.format(EXCEPTION_MESSAGE,
                            ex.getClass().getSimpleName(), Arrays.toString(ex.getStackTrace())),
                    DataMapHolder.getLogMap());
            throw ex;
        } catch (Exception ex) {
            LOGGER.errorContext(ex.getMessage(), ex, DataMapHolder.getLogMap());
            throw ex;
        } finally {
            DataMapHolder.clear();
        }
    }

    private Optional<String> extractContextId(Object payload) {
        if (payload instanceof ChsDelta) {
            return Optional.of(((ChsDelta)payload).getContextId());
        }
        return Optional.empty();
    }
}