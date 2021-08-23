package uk.gov.companieshouse.officer.delta.processor;

import com.github.stefanbirkner.systemlambda.SystemLambda;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;

/**
 * Extension for setting up a kafka container before all of the tests and
 * destroying it afterward. Since test containers maps the ports to an arbitrary port this extension
 * exports the kafka port to the kafka.broker.url property and
 * sets the KAFKA_BROKER_ADDR environment variable.
 */
public class KafkaExtension implements BeforeAllCallback, AfterAllCallback {
    private KafkaContainer kafkaContainer;
    private SystemLambda.WithEnvironmentVariables env;
    Map<String, String> originalVariables;

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        kafkaContainer = new KafkaContainer(
                DockerImageName.parse("confluentinc/cp-kafka"));
        kafkaContainer.start();

        String kafkaUrl = String.format("localhost:%d", kafkaContainer.getFirstMappedPort());
        System.setProperty("kafka.broker.url", kafkaUrl);
        setKafkaEnvironmentVariable(kafkaUrl);
    }

    /**
     * System lambda only allows for setting environment variables within a lambda
     * this doesn't work well with the junit extension life cycle, which doesn't have an
     * "around" callback. So instead, this method uses Reflection magic to access system
     * lambdas internal methods for setting and restoring environment variables.
     *
     * @param kafkaBrokerAddr the address for the kafka broker
     */
    private void setKafkaEnvironmentVariable(final String kafkaBrokerAddr) {
        originalVariables = new HashMap<>(System.getenv());
        env = withEnvironmentVariable("KAFKA_BROKER_ADDR", kafkaBrokerAddr);
        ReflectionTestUtils.invokeMethod(env, "setEnvironmentVariables");
    }

    /**
     * System lambda only allows for setting environment variables within a lambda
     * this doesn't work well with the junit extension life cycle, which doesn't have an
     * "around" callback. So instead, this method uses Reflection magic to access system
     * lambdas internal methods for setting and restoring environment variables.
     */
    private void restoreOriginalEnvironment() {
        ReflectionTestUtils.invokeMethod(env, "restoreOriginalVariables", originalVariables);
    }


    @Override
    public void afterAll(ExtensionContext extensionContext) {
        restoreOriginalEnvironment();
        kafkaContainer.stop();
    }
}
