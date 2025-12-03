package uk.gov.companieshouse.officer.delta.processor.exception;


import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.officer.delta.processor.Util;

import static org.assertj.core.api.Assertions.assertThat;

class RetryableTopicErrorInterceptorTest {

    private RetryableTopicErrorInterceptor interceptor;

    @BeforeEach
    void setUp(){
        interceptor = new RetryableTopicErrorInterceptor();
    }

    @Test
    void when_correct_topic_is_sent_record_is_unchanged() {
        ProducerRecord<String, Object> producerRecord = Util.createRecord("topic", "header");
        ProducerRecord<String, Object> newRecord = interceptor.onSend(producerRecord);

        assertThat(newRecord).isEqualTo(producerRecord);
    }

    @Test
    void when_error_is_nonretryable_topic_is_set_to_invalid() {
        ProducerRecord<String, Object> producerRecord = Util.createRecord("topic-error", NonRetryableErrorException.class.getName());
        ProducerRecord<String, Object> newRecord = interceptor.onSend(producerRecord);

        assertThat(newRecord.topic()).isEqualTo("topic-invalid");
    }

    @Test
    void when_error_is_retryable_topic_is_unchanged() {
        ProducerRecord<String, Object> producerRecord = Util.createRecord("topic-error", RetryableErrorException.class.getName());
        ProducerRecord<String, Object> newRecord = interceptor.onSend(producerRecord);

        assertThat(newRecord.topic()).isEqualTo("topic-error");
    }
}
