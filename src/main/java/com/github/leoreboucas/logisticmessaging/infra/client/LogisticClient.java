package com.github.leoreboucas.logisticmessaging.infra.client;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class LogisticClient {

    @Value("${logistic.base_url}")
    private String logisticBaseUrl;

    @Value("${internal.api.key}")
    private String internalApiKey;

    private RestClient restClient;

    @PostConstruct
    void init() {
        this.restClient = RestClient.builder()
                .baseUrl(logisticBaseUrl)
                .defaultHeader("X-Internal-Api-Key", internalApiKey)
                .build();
    }

    public ResponseEntity<?> confirmUserValidity(String document, String role) {
        return restClient.get().uri("/internal/usuarios/verify?document={document}&role={role}", document, role)
                .retrieve().toBodilessEntity();
    }
}
