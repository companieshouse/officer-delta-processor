package uk.gov.companieshouse.officer.delta.processor.config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.companieshouse.officer.delta.processor.Util.withKafkaEnvironment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.kafka.consumer.ConsumerConfig;
import uk.gov.companieshouse.kafka.deserialization.AvroDeserializer;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officer.delta.processor.processor.Processor;

@ExtendWith(MockitoExtension.class)
class ErrorConsumerConfigTest {
    private ErrorConsumerConfig testConfig;

    @BeforeEach
    void setUp() {
        testConfig = new ErrorConsumerConfig();
    }

    @Test
    void errorConsumerConfig() throws Exception {
        ConsumerConfig consumerConfig = withKafkaEnvironment().execute(() -> testConfig.errorTopicConsumerConfig());

        assertThat(consumerConfig, isA(ConsumerConfig.class));
        assertThat(consumerConfig.getGroupName(), is("group-error"));
        assertThat(consumerConfig.isAutoCommit(), is(false));
    }
}
