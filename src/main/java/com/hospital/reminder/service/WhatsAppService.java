package com.hospital.reminder.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class WhatsAppService {

    private final RestTemplate restTemplate;

    @Value("${whatsapp.api.url:https://api.whatsapp-provider.com/send}")
    private String whatsappApiUrl;

    @Value("${whatsapp.api.token:}")
    private String whatsappApiToken;

    @Value("${whatsapp.api.phone-number:5511999998888}")
    private String whatsappPhoneNumber;

    public WhatsAppService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean enviarWhatsApp(String telefone, String mensagem) {
        try {
            // Simulação de envio de WhatsApp - implementar integração real com provedor
            log.info("Enviando WhatsApp para {}: {}", telefone, mensagem);
            
            // Exemplo de implementação com API REST (WhatsApp Business API)
            /*
            WhatsAppRequest request = WhatsAppRequest.builder()
                    .messagingProduct("whatsapp")
                    .to(formatarTelefoneWhatsApp(telefone))
                    .from(whatsappPhoneNumber)
                    .type("text")
                    .text(WhatsAppText.builder()
                            .body(mensagem)
                            .build())
                    .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(whatsappApiToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<WhatsAppRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<WhatsAppResponse> response = restTemplate.postForEntity(
                    whatsappApiUrl, entity, WhatsAppResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && 
                response.getBody() != null && 
                response.getBody().isSuccess()) {
                log.info("WhatsApp enviado com sucesso para {}", telefone);
                return true;
            } else {
                log.error("Falha ao enviar WhatsApp para {}: {}", telefone, response.getBody());
                return false;
            }
            */
            
            // Simulação bem-sucedida
            log.info("WhatsApp simulado com sucesso para {}", telefone);
            return true;
            
        } catch (Exception e) {
            log.error("Erro ao enviar WhatsApp para {}: {}", telefone, e.getMessage());
            return false;
        }
    }

    private String formatarTelefoneWhatsApp(String telefone) {
        // Remove caracteres não numéricos e adiciona código do país
        String telefoneLimpo = telefone.replaceAll("[^0-9]", "");
        
        // Se não tiver código do país, adiciona 55 (Brasil)
        if (!telefoneLimpo.startsWith("55")) {
            telefoneLimpo = "55" + telefoneLimpo;
        }
        
        return telefoneLimpo + "@c.us"; // Formato para WhatsApp API
    }

    public boolean validarTelefoneWhatsApp(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            return false;
        }
        
        String telefoneLimpo = telefone.replaceAll("[^0-9]", "");
        
        // WhatsApp Brasil: 11 dígitos com DDD
        return telefoneLimpo.length() == 11;
    }
}
