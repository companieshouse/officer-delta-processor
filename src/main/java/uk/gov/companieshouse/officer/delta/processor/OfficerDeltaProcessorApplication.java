package uk.gov.companieshouse.officer.delta.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OfficerDeltaProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(OfficerDeltaProcessorApplication.class, args);
    }

}
