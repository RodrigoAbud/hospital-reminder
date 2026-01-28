package com.hospital.reminder.dto;

import com.hospital.reminder.enums.CanalComunicacao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CriarPrescricaoCompletaRequest(
        @Valid @NotNull PacienteRequest paciente,
        @Valid @NotNull MedicoRequest medico,
        @Valid @NotNull ConfiguracaoRequest configuracao,
        @Valid @NotNull MetadataRequest metadata
) {

    public record PacienteRequest(
            @NotBlank String id,
            @NotBlank @Size(min = 3, max = 200) String nome,
            @NotBlank @Pattern(regexp = "\\d{10,11}") String telefone,
            @Email String email
    ) {}

    public record MedicoRequest(
            @NotBlank String id,
            @NotBlank @Size(min = 3, max = 200) String nome,
            String crm,
            @Email String email
    ) {}

    public record ConfiguracaoRequest(
            @NotNull @Min(1) @Max(365) Integer prazoFase1Dias,
            @NotNull @Min(1) @Max(180) Integer prazoFase2Dias,
            @NotNull @Min(1) @Max(90) Integer maxDiasFase3,
            @NotNull List<CanalComunicacao> canais,
            @NotBlank String telefoneHospital
    ) {}

    public record MetadataRequest(
            String sistemaOrigem,
            String usuarioId,
            String consultaId
    ) {}
}
