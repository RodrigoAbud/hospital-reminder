package com.hospital.reminder.service;

import com.hospital.reminder.dto.AtualizarDadosRequest;
import com.hospital.reminder.dto.CriarPrescricaoCompletaRequest;
import com.hospital.reminder.dto.FinalizarPrescricaoRequest;
import com.hospital.reminder.embedded.ConfiguracaoEscalonamento;
import com.hospital.reminder.embedded.MedicoEmbeddable;
import com.hospital.reminder.embedded.PacienteEmbeddable;
import com.hospital.reminder.entity.PrescricaoEscalonada;
import com.hospital.reminder.enums.CanalComunicacao;
import com.hospital.reminder.enums.FaseRetorno;
import com.hospital.reminder.repository.PrescricaoEscalonadaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PrescricaoEscalonadaService {

    private final PrescricaoEscalonadaRepository repository;
    private final NotificationService notificationService;

    public PrescricaoEscalonada criarPrescricao(CriarPrescricaoCompletaRequest request) {
        PrescricaoEscalonada prescricao = new PrescricaoEscalonada();

        prescricao.setPaciente(new PacienteEmbeddable());
        prescricao.setMedico(new MedicoEmbeddable());
        prescricao.setConfiguracao(new ConfiguracaoEscalonamento());

        prescricao.getPaciente().setIdExterno(request.paciente().id());
        prescricao.getPaciente().setNome(request.paciente().nome());
        prescricao.getPaciente().setTelefone(request.paciente().telefone());
        prescricao.getPaciente().setEmail(request.paciente().email());

        prescricao.getMedico().setIdExterno(request.medico().id());
        prescricao.getMedico().setNome(request.medico().nome());
        prescricao.getMedico().setCrm(request.medico().crm());
        prescricao.getMedico().setEmail(request.medico().email());

        prescricao.getConfiguracao().setPrazoFase1Dias(request.configuracao().prazoFase1Dias());
        prescricao.getConfiguracao().setPrazoFase2Dias(request.configuracao().prazoFase2Dias());
        prescricao.getConfiguracao().setMaxDiasFase3(request.configuracao().maxDiasFase3());
        prescricao.getConfiguracao().setCanais(
                request.configuracao().canais().stream().map(Enum::name).toArray(String[]::new)
        );
        prescricao.getConfiguracao().setTelefoneHospital(request.configuracao().telefoneHospital());

        prescricao.setSistemaOrigem(request.metadata().sistemaOrigem());
        prescricao.setUsuarioIdExterno(request.metadata().usuarioId());
        prescricao.setConsultaIdExterno(request.metadata().consultaId());

        prescricao.setFaseAtual(FaseRetorno.FASE1);
        prescricao.setFinalizada(false);
        prescricao.setDiasEmFase3(0);

        LocalDate hoje = LocalDate.now();
        prescricao.setDataLimiteFase1(hoje.plusDays(request.configuracao().prazoFase1Dias()));
        prescricao.setDataLimiteFase2(prescricao.getDataLimiteFase1().plusDays(request.configuracao().prazoFase2Dias()));

        return repository.save(prescricao);
    }

    public PrescricaoEscalonada buscarPorId(UUID id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Prescrição não encontrada: " + id));
    }

    public List<PrescricaoEscalonada> buscarPorPacienteExterno(String pacienteIdExterno) {
        return repository.findByPacienteIdExternoOrderByCreatedAtDesc(pacienteIdExterno);
    }

    public PrescricaoEscalonada atualizarDados(UUID id, AtualizarDadosRequest request) {
        PrescricaoEscalonada prescricao = buscarPorId(id);
        prescricao.getPaciente().setTelefone(request.novoTelefone());
        prescricao.getPaciente().setEmail(request.novoEmail());
        return repository.save(prescricao);
    }

    public PrescricaoEscalonada finalizar(UUID id, FinalizarPrescricaoRequest request) {
        PrescricaoEscalonada prescricao = buscarPorId(id);
        prescricao.setFinalizada(true);
        prescricao.setMotivoFinalizacao(request.motivoFinalizacao());
        prescricao.setFaseAtual(FaseRetorno.FINALIZADA);
        return repository.save(prescricao);
    }

    public void apagarLGPD(UUID id) {
        repository.deleteById(id);
    }

    /*
    O scheduler executa a cada 5 minutos apenas para facilitar os testes, ou seja 
    ao chegar na fase 3 ele enviará uma mensagem a cada 5 minutos, na implementação 
    real esse scheduler irá rodar uma vez por dia, sendo assim será enviada uma mensagem por dia
    */
    @Scheduled(cron = "0 */5 * * * *")
    public void processarEscalonamento() {
        LocalDate hoje = LocalDate.now();

        // FASE1 vencida -> enviar lembrete FASE1 e mover para FASE2
        for (PrescricaoEscalonada p : repository.findVencidasFase1(hoje)) {
            enviarMensagem(p, FaseRetorno.FASE1);
            p.setFaseAtual(FaseRetorno.FASE2);
            repository.save(p);
        }

        // FASE2 vencida -> enviar lembrete FASE2 e mover para FASE3
        for (PrescricaoEscalonada p : repository.findVencidasFase2(hoje)) {
            enviarMensagem(p, FaseRetorno.FASE2);
            p.setFaseAtual(FaseRetorno.FASE3);
            if (p.getDataInicioFase3() == null) {
                p.setDataInicioFase3(hoje);
            }
            repository.save(p);
        }

        // FASE3: reenvio diário até maxDiasFase3
        for (PrescricaoEscalonada p : repository.findEmFase3()) {
            if (Boolean.TRUE.equals(p.getFinalizada())) {
                continue;
            }
            int maxDias = p.getConfiguracao() != null && p.getConfiguracao().getMaxDiasFase3() != null
                    ? p.getConfiguracao().getMaxDiasFase3()
                    : 30;

            LocalDate inicio = p.getDataInicioFase3();
            if (inicio == null) {
                p.setDataInicioFase3(hoje);
                inicio = hoje;
            }

            int dias = (int) java.time.temporal.ChronoUnit.DAYS.between(inicio, hoje);
            p.setDiasEmFase3(dias);

            if (dias <= maxDias) {
                enviarMensagem(p, FaseRetorno.FASE3);
                repository.save(p);
            } else {
                p.setFinalizada(true);
                p.setMotivoFinalizacao("MAX_DIAS_FASE3_EXCEDIDO");
                p.setFaseAtual(FaseRetorno.FINALIZADA);
                repository.save(p);
            }
        }
    }

    private void enviarMensagem(PrescricaoEscalonada prescricao, FaseRetorno fase) {
        if (prescricao.getConfiguracao() == null || prescricao.getConfiguracao().getCanais() == null) {
            return;
        }

        String mensagem = gerarMensagem(prescricao, fase);

        for (String canalStr : prescricao.getConfiguracao().getCanais()) {
            CanalComunicacao canal;
            try {
                canal = CanalComunicacao.valueOf(canalStr);
            } catch (Exception e) {
                continue;
            }
            notificationService.enviarNotificacaoRetorno(prescricao, canal, mensagem);
        }
    }

    private String gerarMensagem(PrescricaoEscalonada prescricao, FaseRetorno fase) {
        String nomePaciente = prescricao.getPaciente() != null ? prescricao.getPaciente().getNome() : "";
        String nomeMedico = prescricao.getMedico() != null ? prescricao.getMedico().getNome() : "";
        String telefoneHospital = prescricao.getConfiguracao() != null ? prescricao.getConfiguracao().getTelefoneHospital() : "";

        int prazo = switch (fase) {
            case FASE1 -> prescricao.getConfiguracao() != null ? prescricao.getConfiguracao().getPrazoFase1Dias() : 0;
            case FASE2 -> prescricao.getConfiguracao() != null ? prescricao.getConfiguracao().getPrazoFase2Dias() : 0;
            case FASE3 -> prescricao.getConfiguracao() != null ? prescricao.getConfiguracao().getMaxDiasFase3() : 0;
            default -> 0;
        };

        return switch (fase) {
            case FASE1 -> "Sr(a) %s,\nLembrete: Retorno com Dr(a). %s\nPrazo: %d dias\nLigue para agendar: %s".formatted(
                    nomePaciente,
                    nomeMedico,
                    prazo,
                    telefoneHospital
            );
            case FASE2 -> "Sr(a) %s,\nATENÇÃO: Você PRECISA marcar seu retorno com Dr(a). %s\nPrazo: %d dias\nAgende o quanto antes: %s".formatted(
                    nomePaciente,
                    nomeMedico,
                    prazo,
                    telefoneHospital
            );
            case FASE3 -> "Sr(a) %s,\nURGENTE: Você PRECISA marcar seu retorno com Dr(a). %s.\nSe você não agendar, isso poderá prejudicar a sua saúde.\nPrazo máximo: %d dias\nLigue agora para agendar: %s".formatted(
                    nomePaciente,
                    nomeMedico,
                    prazo,
                    telefoneHospital
            );
            default -> "Sr(a) %s,\nLembrete: Retorno com Dr(a). %s\nLigue para agendar: %s".formatted(
                    nomePaciente,
                    nomeMedico,
                    telefoneHospital
            );
        };
    }
}
