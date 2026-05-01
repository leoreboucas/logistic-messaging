package com.github.leoreboucas.logisticmessaging.infra.client;

import com.github.leoreboucas.logisticmessaging.infra.client.DTO.DeliveryManOrdersDTO;
import com.github.leoreboucas.logisticmessaging.infra.client.DTO.OrdersDTO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

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

    public void confirmUserValidity(String document, String role) {
        restClient.get().uri("/internal/usuarios/verify?document={document}&role={role}", document, role)
                .retrieve().toBodilessEntity();
    }

    public List<OrdersDTO> getOrdersForCostumer(String customerCpf) {
        return restClient.get().uri("/internal/pedidos?customerCpf={customerCpf}", customerCpf).retrieve().body(new ParameterizedTypeReference<>() {
        });
    }

    public DeliveryManOrdersDTO getOrdersForDeliveryMan(String deliveryManCpf) {
        return restClient.get().uri("/internal/pedidos?deliveryManCpf={deliveryManCpf}", deliveryManCpf).retrieve().body(DeliveryManOrdersDTO.class);
    }

}
