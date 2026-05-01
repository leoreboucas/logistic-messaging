package com.github.leoreboucas.logisticmessaging.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginUserDTO(
        @NotBlank String document,
        @NotBlank String password
) {
}
