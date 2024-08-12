package uk.gov.companieshouse.officer.delta.processor.exception;

import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.officer.delta.processor.OfficerDeltaProcessorApplication;

import java.util.Map;
import uk.gov.companieshouse.officer.delta.processor.logging.DataMapHolder;

import static java.lang.String.format;
import static org.springframework.kafka.support.KafkaHeaders.EXCEPTION_CAUSE_FQCN;
import static org.springframework.kafka.support.KafkaHeaders.EXCEPTION_STACKTRACE;

/**
 * Retryable Topic Error Interceptor.
 */
public class RetryableTopicErrorInterceptor implements ProducerInterceptor<String, Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OfficerDeltaProcessorApplication.NAMESPACE);

    @Override
    public ProducerRecord<String, Object> onSend(ProducerRecord<String, Object> kafkaRecord) {
        String nextTopic = kafkaRecord.topic().contains("-error") ? getNextErrorTopic(kafkaRecord)
                : kafkaRecord.topic();
        LOGGER.info(format("Moving record into new topic: %s", nextTopic), DataMapHolder.getLogMap());
        if (nextTopic.contains("-invalid")) {
            return new ProducerRecord<>(nextTopic, kafkaRecord.key(), kafkaRecord.value());
        }

        return kafkaRecord;
    }

    @Override
    public void onAcknowledgement(RecordMetadata recordMetadata, Exception ex) {
        // Disable default onAcknowledgement behaviour
    }

    @Override
    public void close() {
        // Disable default close behaviour
    }

    @Override
    public void configure(Map<String, ?> map) {
        // Disable default configure behaviour
    }

    private String getNextErrorTopic(ProducerRecord<String, Object> kafkaRecord) {
        var header1 = kafkaRecord.headers().lastHeader(EXCEPTION_CAUSE_FQCN);
        var header2 = kafkaRecord.headers().lastHeader(EXCEPTION_STACKTRACE);
        return ((header1 != null
                && new String(header1.value()).contains(NonRetryableErrorException.class.getName()))
                || (header2 != null
                && new String(header2.value()).contains(
                NonRetryableErrorException.class.getName())))
                ? kafkaRecord.topic().replace("-error", "-invalid") : kafkaRecord.topic();
    }
}