package uk.gov.companieshouse.officer.delta.processor.steps;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.officer.delta.processor.data.TestData;
import uk.gov.companieshouse.officer.delta.processor.matcher.OfficerRequestMatcher;
import uk.gov.companieshouse.logging.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.requestMadeFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

public class CommonSteps {

    private static final String DELETE_DELTA_URI =
            "/company/09876543/appointments/N-YqKNwdT_HvetusfTJ0H0jAQbA/full_record/delete";
    private static final String DELETE_DELTA_AT = "20230724093435661593";
    private static final String X_DELTA_AT = "X-DELTA-AT";

    @Value("${officer.delta.processor.topic}")
    private String mainTopic;

    @Value("${wiremock.server.port:8888}")
    private String port;

    private static WireMockServer wireMockServer;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    public KafkaConsumer<String, Object> kafkaConsumer;
    @Autowired
    private Logger logger;

    private String type;
    private String id;
    private String output;

    @Given("the application is running")
    public void theApplicationRunning() {
        assertThat(kafkaTemplate).isNotNull();
    }

    @When("^the consumer receives a (.*) officer delta with id (.*)$")
    public void theConsumerReceivesOfficerDelta(String type, String id) throws Exception {
        configureWiremock();
        stubPutAppointment("01777777", id);
        this.output = TestData.getOutputData(type);
        this.type = type;
        this.id = id;

        ChsDelta delta = new ChsDelta(TestData.getInputData(type), 1, "1", false);
        kafkaTemplate.send(mainTopic, delta);

        countDown();
    }

    @When("an invalid avro message is sent")
    public void invalidAvroMessageIsSent() throws Exception {
        kafkaTemplate.send(mainTopic, "InvalidData");

        countDown();
    }

    @When("a message with invalid data is sent")
    public void messageWithInvalidDataIsSent() throws Exception {
        ChsDelta delta = new ChsDelta("InvalidData", 1, "1", false);
        kafkaTemplate.send(mainTopic, delta);

        countDown();
    }

    @When("^the consumer receives a message but the data api returns a (\\d*)$")
    public void theConsumerReceivesMessageButDataApiReturns(int responseCode) throws Exception{
        configureWiremock();
        stubPutAppointment("01777777", "EcEKO1YhIKexb0cSDZsn_OHsFw4", responseCode);

        ChsDelta delta = new ChsDelta(
                TestData.getInputData("natural"), 1, "1", false);
        kafkaTemplate.send(mainTopic, delta);

        countDown();
    }

    @When("the consumer receives a message that causes an error")
    public void theConsumerReceivesMessageThatCausesAnError() throws Exception {
        ChsDelta delta = new ChsDelta(
                TestData.getInputData("error"), 1, "1", false);
        kafkaTemplate.send(mainTopic, delta);

        countDown();
    }

    @Then("a PUT request is sent to the appointments api with the transformed data")
    public void putRequestIsSentToTheAppointmentsApi() {
        verify(1, requestMadeFor(new OfficerRequestMatcher(logger, "01777777", output, id)));
    }

    @Then("^the message should be moved to topic (.*)$")
    public void theMessageShouldBeMovedToTopic(String topic) {
        ConsumerRecord<String, Object> singleRecord = KafkaTestUtils.getSingleRecord(kafkaConsumer, topic);

        assertThat(singleRecord.value()).isNotNull();
    }

    @Then("^the message should retry (\\d*) times and then error$")
    public void theMessageShouldRetryAndError(int retries) {
        ConsumerRecords<String, Object> records = KafkaTestUtils.getRecords(kafkaConsumer);
        Iterable<ConsumerRecord<String, Object>> retryRecords =  records.records("officer-delta-retry");
        Iterable<ConsumerRecord<String, Object>> errorRecords =  records.records("officer-delta-error");

        int actualRetries = (int) StreamSupport.stream(retryRecords.spliterator(), false).count();
        int errors = (int) StreamSupport.stream(errorRecords.spliterator(), false).count();

        assertThat(actualRetries).isEqualTo(retries);
        assertThat(errors).isEqualTo(1);
    }

    @When("the consumer receives a delete payload")
    public void theConsumerReceivesDelete() throws Exception {
        configureWiremock();
        stubDeleteOfficer(200);
        ChsDelta delta = new ChsDelta(TestData.getDeleteData(), 1, "1", true);
        kafkaTemplate.send(mainTopic, delta);

        countDown();
    }

    @Then("a DELETE request is sent to the appointments api with the encoded Id and company number")
    public void deleteRequestIsSent() {
        verify(1, deleteRequestedFor(urlMatching(DELETE_DELTA_URI))
                .withHeader(X_DELTA_AT, equalTo(DELETE_DELTA_AT)));
    }

    @After
    public void shutdownWiremock(){
        if (wireMockServer != null)
            wireMockServer.stop();
    }

    private void configureWiremock() {
        wireMockServer = new WireMockServer(Integer.parseInt(port));
        wireMockServer.start();
        configureFor("localhost", Integer.parseInt(port));
    }

    private void stubPutAppointment(String conumb, String id) {
        stubPutAppointment(conumb, id, 200);
    }

    private void stubPutAppointment(String conumb, String id, int responseCode) {
        stubFor(put(urlEqualTo("/company/" + conumb + "/appointments/"+ id +"/full_record"))
                .willReturn(aResponse().withStatus(responseCode)));
    }

    private void stubDeleteOfficer(int responseCode) {
        stubFor(delete(urlEqualTo(DELETE_DELTA_URI))
                .withHeader(X_DELTA_AT, equalTo(DELETE_DELTA_AT))
                .willReturn(aResponse().withStatus(responseCode)));
    }

    private void countDown() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(5, TimeUnit.SECONDS);
    }
}

