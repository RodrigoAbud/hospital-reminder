package com.hospital.reminder.mapper;

import com.hospital.reminder.dto.CriarPrescricaoCompletaRequest;
import com.hospital.reminder.embedded.ConfiguracaoEscalonamento;
import com.hospital.reminder.embedded.MedicoEmbeddable;
import com.hospital.reminder.embedded.PacienteEmbeddable;
import com.hospital.reminder.entity.PrescricaoEscalonada;
import com.hospital.reminder.enums.CanalComunicacao;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PrescricaoEscalonadaMapper {

    @Mapping(source = "paciente", target = "paciente")
    @Mapping(source = "medico", target = "medico")
    @Mapping(source = "configuracao", target = "configuracao")
    @Mapping(source = "metadata.sistemaOrigem", target = "sistemaOrigem")
    @Mapping(source = "metadata.usuarioId", target = "usuarioIdExterno")
    @Mapping(source = "metadata.consultaId", target = "consultaIdExterno")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "faseAtual", ignore = true)
    @Mapping(target = "dataPrescricao", ignore = true)
    @Mapping(target = "dataLimiteFase1", ignore = true)
    @Mapping(target = "dataLimiteFase2", ignore = true)
    @Mapping(target = "dataInicioFase3", ignore = true)
    @Mapping(target = "diasEmFase3", ignore = true)
    @Mapping(target = "finalizada", ignore = true)
    @Mapping(target = "motivoFinalizacao", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "versao", ignore = true)
    PrescricaoEscalonada toEntity(CriarPrescricaoCompletaRequest request);

    @Mapping(source = "id", target = "idExterno")
    PacienteEmbeddable toPaciente(CriarPrescricaoCompletaRequest.PacienteRequest request);

    @Mapping(source = "id", target = "idExterno")
    MedicoEmbeddable toMedico(CriarPrescricaoCompletaRequest.MedicoRequest request);

    ConfiguracaoEscalonamento toConfiguracao(CriarPrescricaoCompletaRequest.ConfiguracaoRequest request);

    default String[] map(List<CanalComunicacao> canais) {
        if (canais == null || canais.isEmpty()) {
            return null;
        }
        return canais.stream().map(Enum::name).toArray(String[]::new);
    }
}
