package com.hospital.reminder.service;

import com.hospital.reminder.entity.PrescricaoEscalonada;
import com.hospital.reminder.enums.CanalComunicacao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;
    private final SmsService smsService;
    private final WhatsAppService whatsappService;

    public boolean enviarNotificacaoRetorno(PrescricaoEscalonada prescricao, CanalComunicacao canal, String mensagem) {
        try {
            return switch (canal) {
                case EMAIL -> enviarEmail(prescricao, mensagem);
                case SMS -> enviarSms(prescricao, mensagem);
                case WHATSAPP -> enviarWhatsApp(prescricao, mensagem);
            };
        } catch (Exception e) {
            log.error("Erro ao enviar notificação por {} para prescrição {}: {}", 
                    canal, prescricao.getId(), e.getMessage());
            return false;
        }
    }

    private boolean enviarEmail(PrescricaoEscalonada prescricao, String mensagem) {
        try {
            if (prescricao.getPaciente().getEmail() == null || 
                prescricao.getPaciente().getEmail().isBlank()) {
                log.warn("Email do paciente não disponível para prescrição: {}", prescricao.getId());
                return false;
            }

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(prescricao.getPaciente().getEmail());
            mailMessage.setSubject("Lembrete de Retorno - Hospital System");
            mailMessage.setText(mensagem);
            mailMessage.setFrom("noreply@hospital.com");

            mailSender.send(mailMessage);
            log.info("Email enviado com sucesso para prescrição: {}", prescricao.getId());
            return true;

        } catch (Exception e) {
            log.error("Erro ao enviar email para prescrição {}: {}", prescricao.getId(), e.getMessage());
            return false;
        }
    }

    private boolean enviarSms(PrescricaoEscalonada prescricao, String mensagem) {
        try {
            if (prescricao.getPaciente().getTelefone() == null || 
                prescricao.getPaciente().getTelefone().isBlank()) {
                log.warn("Telefone do paciente não disponível para prescrição: {}", prescricao.getId());
                return false;
            }

            boolean resultado = smsService.enviarSms(
                    prescricao.getPaciente().getTelefone(), 
                    mensagem
            );

            if (resultado) {
                log.info("SMS enviado com sucesso para prescrição: {}", prescricao.getId());
            }

            return resultado;

        } catch (Exception e) {
            log.error("Erro ao enviar SMS para prescrição {}: {}", prescricao.getId(), e.getMessage());
            return false;
        }
    }

    private boolean enviarWhatsApp(PrescricaoEscalonada prescricao, String mensagem) {
        try {
            if (prescricao.getPaciente().getTelefone() == null || 
                prescricao.getPaciente().getTelefone().isBlank()) {
                log.warn("Telefone do paciente não disponível para WhatsApp na prescrição: {}", prescricao.getId());
                return false;
            }

            boolean resultado = whatsappService.enviarWhatsApp(
                    prescricao.getPaciente().getTelefone(), 
                    mensagem
            );

            if (resultado) {
                log.info("WhatsApp enviado com sucesso para prescrição: {}", prescricao.getId());
            }

            return resultado;

        } catch (Exception e) {
            log.error("Erro ao enviar WhatsApp para prescrição {}: {}", prescricao.getId(), e.getMessage());
            return false;
        }
    }
}
