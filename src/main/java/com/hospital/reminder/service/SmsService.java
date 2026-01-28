package com.hospital.reminder.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class SmsService {

    private final RestTemplate restTemplate;

    @Value("${sms.api.url:https://api.sms-provider.com/send}")
    private String smsApiUrl;

    @Value("${sms.api.key:}")
    private String smsApiKey;

    @Value("${sms.api.from:HOSPITAL}")
    private String smsFrom;

    public SmsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean enviarSms(String telefone, String mensagem) {
        try {
            // Simulação de envio de SMS - implementar integração real com provedor
            log.info("Enviando SMS para {}: {}", telefone, mensagem);
            
            // Exemplo de implementação com API REST
            /*
            SmsRequest request = SmsRequest.builder()
                    .to(telefone)
                    .from(smsFrom)
                    .message(mensagem)
                    .apiKey(smsApiKey)
                    .build();
            
            ResponseEntity<SmsResponse> response = restTemplate.postForEntity(
                    smsApiUrl, request, SmsResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && 
                response.getBody() != null && 
                response.getBody().isSuccess()) {
                log.info("SMS enviado com sucesso para {}", telefone);
                return true;
            } else {
                log.error("Falha ao enviar SMS para {}: {}", telefone, response.getBody());
                return false;
            }
            */
            
            // Simulação bem-sucedida
            log.info("SMS simulado com sucesso para {}", telefone);
            return true;
            
        } catch (Exception e) {
            log.error("Erro ao enviar SMS para {}: {}", telefone, e.getMessage());
            return false;
        }
    }

    public boolean validarTelefone(String telefone) {
        // Validação básica de telefone brasileiro
        if (telefone == null || telefone.isBlank()) {
            return false;
        }
        
        // Remove caracteres não numéricos
        String telefoneLimpo = telefone.replaceAll("[^0-9]", "");
        
        // Telefone brasileiro deve ter 10 ou 11 dígitos
        return telefoneLimpo.length() == 10 || telefoneLimpo.length() == 11;
    }
}
