-- Migration: Create table prescricoes_escalonadas
-- Description: Tabela única para armazenar prescrições escalonadas (retorno) com dados embutidos

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS prescricoes_escalonadas (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Dados do paciente (recebidos)
    paciente_id_externo VARCHAR(100),
    paciente_nome VARCHAR(200) NOT NULL,
    paciente_telefone VARCHAR(20) NOT NULL,
    paciente_email VARCHAR(200),

    -- Dados do médico (recebidos)
    medico_id_externo VARCHAR(100),
    medico_nome VARCHAR(200) NOT NULL,
    medico_crm VARCHAR(50),
    medico_email VARCHAR(200),

    -- Configuração (recebida)
    prazo_fase1_dias INTEGER NOT NULL,
    prazo_fase2_dias INTEGER NOT NULL,
    max_dias_fase3 INTEGER NOT NULL DEFAULT 30,
    canais TEXT[] NOT NULL DEFAULT '{SMS}',
    telefone_hospital VARCHAR(20) NOT NULL,

    -- Estado atual (gerado pela API)
    fase_atual VARCHAR(20) NOT NULL DEFAULT 'FASE1',
    data_prescricao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_limite_fase1 DATE,
    data_limite_fase2 DATE,
    data_inicio_fase3 DATE,
    dias_em_fase3 INTEGER NOT NULL DEFAULT 0,
    finalizada BOOLEAN NOT NULL DEFAULT FALSE,
    motivo_finalizacao VARCHAR(100),

    -- Metadata (recebido)
    sistema_origem VARCHAR(100),
    usuario_id_externo VARCHAR(100),
    consulta_id_externo VARCHAR(100),

    -- Controle
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    versao BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT chk_fase_atual CHECK (fase_atual IN ('FASE1', 'FASE2', 'FASE3', 'FINALIZADA')),
    CONSTRAINT chk_prazos CHECK (prazo_fase1_dias >= 1 AND prazo_fase2_dias >= 1 AND max_dias_fase3 >= 1),
    CONSTRAINT chk_dias_em_fase3 CHECK (dias_em_fase3 >= 0)
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_paciente_externo ON prescricoes_escalonadas(paciente_id_externo);
CREATE INDEX IF NOT EXISTS idx_medico_externo ON prescricoes_escalonadas(medico_id_externo);
CREATE INDEX IF NOT EXISTS idx_fase_atual ON prescricoes_escalonadas(fase_atual);
CREATE INDEX IF NOT EXISTS idx_data_limite_fase1 ON prescricoes_escalonadas(data_limite_fase1);

-- Trigger para atualizar updated_at automaticamente
CREATE OR REPLACE FUNCTION atualizar_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_atualizar_updated_at
    BEFORE UPDATE ON prescricoes_escalonadas
    FOR EACH ROW
    EXECUTE FUNCTION atualizar_updated_at();

-- Comentários na tabela
COMMENT ON TABLE prescricoes_escalonadas IS 'Tabela única para prescrições escalonadas de retorno (dados do hospital + estado do ciclo)';
COMMENT ON COLUMN prescricoes_escalonadas.paciente_id_externo IS 'ID do paciente no sistema hospitalar';
COMMENT ON COLUMN prescricoes_escalonadas.medico_id_externo IS 'ID do médico no sistema hospitalar';
COMMENT ON COLUMN prescricoes_escalonadas.fase_atual IS 'Fase atual do retorno (FASE1, FASE2, FASE3, FINALIZADA)';
COMMENT ON COLUMN prescricoes_escalonadas.canais IS 'Canais configurados para envio de lembretes (text[])';

