package com.hospital.reminder.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.reminder.enums.CanalNotificacao;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Collections;
import java.util.List;

@Converter
public class CanalNotificacaoListConverter implements AttributeConverter<List<CanalNotificacao>, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<CanalNotificacao> attribute) {
        try {
            if (attribute == null) {
                return null;
            }
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Falha ao serializar canais de notificação", e);
        }
    }

    @Override
    public List<CanalNotificacao> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank()) {
                return Collections.emptyList();
            }
            return OBJECT_MAPPER.readValue(dbData, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Falha ao desserializar canais de notificação", e);
        }
    }
}
