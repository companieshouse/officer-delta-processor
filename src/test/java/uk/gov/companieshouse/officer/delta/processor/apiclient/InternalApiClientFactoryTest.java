package uk.gov.companieshouse.officer.delta.processor.apiclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.InternalApiClient;

class InternalApiClientFactoryTest {

    private static final String API_KEY = "api-key";
    private static final String API_URL = "url";

    private final InternalApiClientFactory internalApiClientFactory = new InternalApiClientFactory(API_KEY, API_URL);

    @Test
    void shouldGetNewInternalApiClient() {
        // given

        // when
        InternalApiClient actual = internalApiClientFactory.get();

        // then
        assertNotNull(actual.getHttpClient());
        assertEquals(API_URL, actual.getBasePath());
    }
}