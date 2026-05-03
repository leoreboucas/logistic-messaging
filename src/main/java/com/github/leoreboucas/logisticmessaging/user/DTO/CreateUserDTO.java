package com.github.leoreboucas.logisticmessaging.user.DTO;


import com.github.leoreboucas.logisticmessaging.user.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateUserDTO(
        @NotBlank(message = "O nome é obrigatório") String firstName,
        @NotBlank(message = "Sobrenome é obrigatório.") String secondName,
        @Pattern(regexp = "(^\\d{11}$)|(^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$)|(^\\d{14}$)|(^\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}$)",
                message = "Documento inválido. Informe um CPF ou CNPJ válido.")

        String document,
        @Email(message = "Email inválido.")  String email,
        @Pattern(
                regexp ="^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{6,}$",
                message = "Senha deve conter pelo menos 6 caracteres, 1 letra maiúscula, 1 letra minúscula e 1 caractere especial, por exemplo = @#$%^&+=")
        String password,
        @NotNull(message = "A role é obrigatória.")
        UserRole role
) {
}
