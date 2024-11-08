package uk.gov.companieshouse.officer.delta.processor.apiclient;

import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;

@Component
public class InternalApiClientFactory implements Supplier<InternalApiClient> {

    private final String apiKey;
    private final String apiUrl;
    private final String paymentsApiUrl;
    private final String internalApiUrl;

    public InternalApiClientFactory(@Value("${chs.internal.api.key}") String apiKey,
            @Value("${api.url}") String apiUrl,
            @Value("${payments.api.url}") String paymentsApiUrl,
            @Value("${internal.api.url}") String internalApiUrl) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.paymentsApiUrl = paymentsApiUrl;
        this.internalApiUrl = internalApiUrl;
    }

    @Override
    public InternalApiClient get() {
        InternalApiClient internalApiClient = new InternalApiClient(new ApiKeyHttpClient(apiKey));
        internalApiClient.setBasePath(apiUrl);
        internalApiClient.setBasePaymentsPath(paymentsApiUrl);
        internalApiClient.setInternalBasePath(internalApiUrl);
        return internalApiClient;
    }
}
