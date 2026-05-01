package com.github.leoreboucas.logisticmessaging.infra.client.DTO;

import java.time.LocalDateTime;

public record PartialDeliveryDTO(
        String trackingCode,
        String observation,
        LocalDateTime departureDate,
        String originCenter,
        String destinationCenter
) {
}
