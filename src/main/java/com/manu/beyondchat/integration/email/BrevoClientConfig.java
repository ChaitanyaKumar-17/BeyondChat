package com.manu.beyondchat.integration.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class BrevoClientConfig {

    @Bean
    public BrevoHttpClient brevoHttpClient(@Value("${brevo.api.key}") String apiKey) {

        // 1. Build the modern, synchronous RestClient
        // We securely attach the base URL and authentication header here, universally.
        RestClient restClient = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .defaultHeader("api-key", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();

        // 2. Create the adapter linking the client to the proxy factory
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        // 3. Spring dynamically generates the interface implementation at runtime
        return factory.createClient(BrevoHttpClient.class);
    }
}
