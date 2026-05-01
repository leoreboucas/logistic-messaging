package com.github.leoreboucas.logisticmessaging.infra.client.DTO;

import java.util.List;

public record DeliveryManOrdersDTO(
        List<PartialDeliveryDTO> partialDelivery,
        List<FinalDeliveryDTO> finalDelivery
) {
}
