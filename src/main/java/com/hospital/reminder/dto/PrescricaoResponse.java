package com.hospital.reminder.dto;

import com.hospital.reminder.entity.PrescricaoEscalonada;
import com.hospital.reminder.enums.FaseRetorno;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PrescricaoResponse(
        UUID id,
        String pacienteIdExterno,
        String medicoIdExterno,
        FaseRetorno faseAtual,
        boolean finalizada,
        String motivoFinalizacao,
        LocalDate dataLimiteFase1,
        LocalDate dataLimiteFase2,
        LocalDate dataInicioFase3,
        Integer diasEmFase3,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<String> canais
) {
    public static PrescricaoResponse fromEntity(PrescricaoEscalonada p) {
        List<String> canais = p.getConfiguracao() != null && p.getConfiguracao().getCanais() != null
                ? List.of(p.getConfiguracao().getCanais())
                : List.of();

        return new PrescricaoResponse(
                p.getId(),
                p.getPaciente() != null ? p.getPaciente().getIdExterno() : null,
                p.getMedico() != null ? p.getMedico().getIdExterno() : null,
                p.getFaseAtual(),
                Boolean.TRUE.equals(p.getFinalizada()),
                p.getMotivoFinalizacao(),
                p.getDataLimiteFase1(),
                p.getDataLimiteFase2(),
                p.getDataInicioFase3(),
                p.getDiasEmFase3(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                canais
        );
    }
}
