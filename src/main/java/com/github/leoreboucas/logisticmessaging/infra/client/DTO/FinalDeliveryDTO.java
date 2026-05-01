package com.github.leoreboucas.logisticmessaging.infra.client.DTO;

import java.time.LocalDateTime;

public record FinalDeliveryDTO(
        String trackingCode,
        String originCenter,
        String customerCompleteName,
        String cep,
        String street,
        String houseNumber,
        String complement,
        String neighborhood,
        String city,
        String state,
        LocalDateTime departureDate,
        String observation
) {
}
