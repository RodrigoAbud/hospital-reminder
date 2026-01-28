package com.hospital.reminder.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AtualizarDadosRequest(
        @NotBlank @Pattern(regexp = "\\d{10,11}") String novoTelefone,
        @Email String novoEmail
) {
}
