package com.github.leoreboucas.logisticmessaging.infra.client.DTO;

public record OrdersDTO(
        String trackingCode,
        String status,
        String cep,
        String street,
        String houseNumber,
        String complement,
        String neighborhood,
        String city,
        String state,
        String observation
) {
}
