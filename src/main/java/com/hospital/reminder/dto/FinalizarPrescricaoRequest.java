package com.hospital.reminder.dto;

import jakarta.validation.constraints.NotBlank;

public record FinalizarPrescricaoRequest(
        @NotBlank String motivoFinalizacao
) {
}
