package uk.gov.companieshouse.officer.delta.processor.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class OfficersTest {
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Example JSON can be serialized into model")
    void jsonDecodeExample() throws JsonProcessingException {
        // Given
        String rawJSON = loadExampleJSON();

        // When
        Officers officers = objectMapper.readValue(rawJSON, Officers.class);

        assertThat(officers.getCreatedTime(), is("07-JUN-21 15.26.17.000000"));
        assertThat(officers.getOfficers().getFirst().getServiceAddress().getPremises(), is("SA"));
        assertThat(officers.getDeltaAt(), is("20140925171003950844"));
    }


    private String loadExampleJSON() {
        try {
            File file = ResourceUtils.getFile("classpath:officer_delta_example.json");
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            fail("Unable to load example JSON file.", e);
            throw new RuntimeException(e);
        }
    }

}