package uk.gov.companieshouse.officer.delta.processor.config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.companieshouse.officer.delta.processor.Util.withKafkaEnvironment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.kafka.consumer.ConsumerConfig;

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
