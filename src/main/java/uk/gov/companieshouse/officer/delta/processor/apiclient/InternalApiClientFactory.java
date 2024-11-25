package uk.gov.companieshouse.officer.delta.processor.apiclient;

import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.officer.delta.processor.logging.DataMapHolder;

@Component
public class InternalApiClientFactory implements Supplier<InternalApiClient> {

    private final String apiKey;
    private final String apiUrl;

    public InternalApiClientFactory(@Value("${chs.internal.api.key}") String apiKey,
            @Value("${api.url}") String apiUrl) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    @Override
    public InternalApiClient get() {
        InternalApiClient internalApiClient = new InternalApiClient(new ApiKeyHttpClient(apiKey));
        internalApiClient.setBasePath(apiUrl);
        internalApiClient.getHttpClient().setRequestId(DataMapHolder.getRequestId());
        return internalApiClient;
    }
}
