package uk.gov.companieshouse.officer.delta.processor.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.officer.delta.processor.serialization.ChsDeltaDeserializer;
import uk.gov.companieshouse.officer.delta.processor.serialization.ChsDeltaSerializer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.core.IsNot.not;

class KafkaSerializerConfigTest {

    @Mock
    Logger logger;
    ChsDeltaDeserializer chsDeltaDeserializer = new ChsDeltaDeserializer(logger);
    ChsDeltaSerializer chsDeltaSerializer = new ChsDeltaSerializer(logger);
    KafkaSerializerConfig kafkaConfig;

    @BeforeEach
    void setUp() {
        kafkaConfig = new KafkaSerializerConfig(chsDeltaDeserializer,
                chsDeltaSerializer,"server",1);
    }
    @Test
    void listenerContainerFactory() {
        assertThat(kafkaConfig.listenerContainerFactory(), is(not(nullValue())));
        assertThat(kafkaConfig.listenerContainerFactory(), isA(
                ConcurrentKafkaListenerContainerFactory.class));
    }

    @Test
    void producerFactory() {
        assertThat(kafkaConfig.producerFactory(), is(not(nullValue())));
        assertThat(kafkaConfig.producerFactory(), isA(ProducerFactory.class));
        assertThat(kafkaConfig.producerFactory().getConfigurationProperties(),
                is(not(nullValue())));
    }

    @Test
    void kafkaConsumerFactory() {
        assertThat(kafkaConfig.kafkaConsumerFactory(), is(not(nullValue())));
        assertThat(kafkaConfig.kafkaConsumerFactory(), isA(ConsumerFactory.class));
        assertThat(kafkaConfig.kafkaConsumerFactory().getConfigurationProperties(),
                is(not(nullValue())));
        assertThat(kafkaConfig.kafkaConsumerFactory().getConfigurationProperties()
                        .get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG),is("server"));
        assertThat(kafkaConfig.kafkaConsumerFactory().getConfigurationProperties()
                .get(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS),
                is(ChsDeltaDeserializer.class));
        assertThat(kafkaConfig.kafkaConsumerFactory().getConfigurationProperties()
                .get(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS),
                is(StringDeserializer.class));
    }
}
