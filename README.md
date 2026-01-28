# Hospital Reminder API

API de lembretes escalonados de **retorno** para sistema hospitalar.

## 🚀 Tecnologias

- **Java 21** com Virtual Threads
- **Spring Boot 3.2.1**
- **PostgreSQL 16**
- **Docker & Docker Compose**
- **OpenAPI 3 com Swagger UI**
- **Resilience4j Circuit Breaker**
- **Maven**

## 📋 Funcionalidades

- ✅ Lembretes escalonados por **fases em dias** (FASE1, FASE2, FASE3)
- ✅ Múltiplos canais: Email, SMS, WhatsApp
- ✅ API Key authentication
- ✅ Virtual Threads para alta performance
- ✅ Auditoria completa com versionamento
- ✅ Dashboard Swagger UI

## 🏗️ Arquitetura

- **Uma única tabela** `prescricoes_escalonadas` com dados embutidos
- **@Embedded** para `PacienteEmbeddable`, `MedicoEmbeddable`, `ConfiguracaoEscalonamento`
- **Agendamento automático** com Spring `@Scheduled`

## 🚀 Como rodar (passo a passo)

### 1. Pré-requisitos

- Java 21
- Maven
- Docker e Docker Compose

### 2. Subir Banco de Dados (PostgreSQL)
```bash
docker-compose up -d postgres
```

### 3. Executar Migrações (automático)

As migrações do Flyway rodam automaticamente na inicialização da aplicação.

### 4. Executar Aplicação
```bash
mvn spring-boot:run
```

### 5. Acessar Swagger UI
```
http://localhost:8080/swagger-ui.html
```

### 6. Healthcheck (Actuator)
```
http://localhost:8080/actuator/health
```

## 🐳 Docker

### Build e Execução
```bash
# Build imagem
docker build -t hospital-reminder .

# Apenas PostgreSQL
docker-compose up -d postgres
```

## 🔐 Autenticação

A API utiliza **API Key** no header `X-API-KEY`.

**Default Key:**
```
hospital-api-key-2024-secret
```

**Exemplo:**
```bash
curl -X GET http://localhost:8080/api/prescricoes \
  -H "X-API-KEY: hospital-api-key-2024-secret"
```

## 📊 Endpoints Principais

### Prescrições
- `POST /api/prescricoes` - Criar prescrição (dados completos)
- `GET /api/prescricoes/{id}` - Buscar por ID (UUID)
- `GET /api/prescricoes/paciente/{idExterno}` - Buscar prescrições por ID externo do paciente
- `PUT /api/prescricoes/{id}/dados` - Atualizar telefone/email do paciente
- `PUT /api/prescricoes/{id}/finalizar` - Finalizar (paciente retornou)
- `DELETE /api/prescricoes/{id}` - LGPD (remove registro)

## 📝 Exemplos de requisições (curl)

Em todos os exemplos abaixo, use o header `X-API-KEY`.

### 1) Criar prescrição (POST /api/prescricoes)

```bash
curl -X POST "http://localhost:8080/api/prescricoes" \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: hospital-api-key-2024-secret" \
  -d '{
    "paciente": {
      "id": "P123",
      "nome": "João Silva",
      "telefone": "11999999999",
      "email": "joao@email.com"
    },
    "medico": {
      "id": "M456",
      "nome": "Dra. Maria Santos",
      "crm": "12345/SP",
      "email": "maria.santos@hospital.com"
    },
    "configuracao": {
      "prazoFase1Dias": 30,
      "prazoFase2Dias": 15,
      "maxDiasFase3": 30,
      "canais": ["SMS", "WHATSAPP", "EMAIL"],
      "telefoneHospital": "1133339999"
    },
    "metadata": {
      "sistemaOrigem": "SistemaHospitalarX",
      "usuarioId": "U789",
      "consultaId": "C101"
    }
  }'
```

### 2) Buscar por ID (GET /api/prescricoes/{id})

```bash
curl -X GET "http://localhost:8080/api/prescricoes/<UUID>" \
  -H "X-API-KEY: hospital-api-key-2024-secret"
```

### 3) Buscar por ID externo do paciente (GET /api/prescricoes/paciente/{idExterno})

```bash
curl -X GET "http://localhost:8080/api/prescricoes/paciente/P123" \
  -H "X-API-KEY: hospital-api-key-2024-secret"
```

### 4) Atualizar dados do paciente (PUT /api/prescricoes/{id}/dados)

```bash
curl -X PUT "http://localhost:8080/api/prescricoes/<UUID>/dados" \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: hospital-api-key-2024-secret" \
  -d '{
    "novoTelefone": "11988887777",
    "novoEmail": "novo.email@exemplo.com"
  }'
```

### 5) Finalizar (PUT /api/prescricoes/{id}/finalizar)

```bash
curl -X PUT "http://localhost:8080/api/prescricoes/<UUID>/finalizar" \
  -H "Content-Type: application/json" \
  -H "X-API-KEY: hospital-api-key-2024-secret" \
  -d '{
    "motivoFinalizacao": "PACIENTE_RETORNOU"
  }'
```

### 6) LGPD (DELETE /api/prescricoes/{id})

```bash
curl -X DELETE "http://localhost:8080/api/prescricoes/<UUID>" \
  -H "X-API-KEY: hospital-api-key-2024-secret"
```

## ✅ Como testar e simular os envios

SMS e WhatsApp são **simulados** e aparecem nos logs. Email depende de configuração de SMTP (se não estiver configurado, o envio pode falhar e será logado).

### 1) Suba o Postgres e rode a aplicação

```bash
docker-compose up -d postgres
mvn spring-boot:run
```

### 2) Crie uma prescrição e guarde o `id` retornado

Use o exemplo de `POST /api/prescricoes` acima e copie o campo `id` da resposta.

### 3) Confirme no banco que a prescrição foi criada e está em `FASE1`

```bash
docker-compose exec -T postgres psql -U hospital_user -d hospital_reminder -c "SELECT id, fase_atual, data_limite_fase1, data_limite_fase2, finalizada FROM prescricoes_escalonadas ORDER BY created_at DESC LIMIT 5;"
```

### 4) Forçar vencimento para simular disparo imediato do scheduler

Por padrão, as datas de limite são calculadas para o futuro. Para testar sem esperar dias, você pode forçar uma fase como vencida.

#### Simular vencimento da FASE1

Isso fará o scheduler enviar a mensagem e mover para `FASE2` no próximo ciclo.

```bash
docker-compose exec -T postgres psql -U hospital_user -d hospital_reminder -c "UPDATE prescricoes_escalonadas SET fase_atual='FASE1', finalizada=FALSE, data_limite_fase1 = (CURRENT_DATE - INTERVAL '1 day') WHERE id = '<UUID>';"
```

#### Simular vencimento da FASE2

```bash
docker-compose exec -T postgres psql -U hospital_user -d hospital_reminder -c "UPDATE prescricoes_escalonadas SET fase_atual='FASE2', finalizada=FALSE, data_limite_fase2 = (CURRENT_DATE - INTERVAL '1 day') WHERE id = '<UUID>';"
```

#### Simular paciente já estar em FASE3

```bash
docker-compose exec -T postgres psql -U hospital_user -d hospital_reminder -c "UPDATE prescricoes_escalonadas SET fase_atual='FASE3', finalizada=FALSE, data_inicio_fase3 = (CURRENT_DATE - INTERVAL '1 day') WHERE id = '<UUID>';"
```

### 5) Aguarde o scheduler e verifique os logs

O job roda a cada **5 minutos**. Você deve ver logs como:

- `Enviando SMS para ...`
- `SMS simulado com sucesso ...`
- `Enviando WhatsApp para ...`
- `WhatsApp simulado com sucesso ...`

Se o canal `EMAIL` estiver habilitado e o SMTP não estiver configurado, você poderá ver erro de envio de email nos logs (isso é esperado em ambiente local).

### 6) Verifique que a fase evoluiu

Após simular a FASE1 vencida, a prescrição deve ir para `FASE2`; após FASE2 vencida, deve ir para `FASE3`.

```bash
docker-compose exec -T postgres psql -U postgres -d hospital_reminder -c "SELECT id, fase_atual, data_inicio_fase3, dias_em_fase3, finalizada, motivo_finalizacao FROM prescricoes_escalonadas WHERE id = '<UUID>';"
```

## ⏰ Fases de lembrete (por dias)

- **FASE1**: até `data_limite_fase1`
- **FASE2**: após `data_limite_fase1` até `data_limite_fase2`
- **FASE3**: após `data_limite_fase2`, com reenvio diário até `max_dias_fase3`

## 🔧 Configuração

### application.yml
```yaml
# API Key
security:
  api-key:
    secret-key: sua-chave-secreta-aqui
```

### Variáveis de Ambiente
```bash
# Email
EMAIL_USERNAME=seu-email@gmail.com
EMAIL_PASSWORD=sua-app-password

# API Key
API_KEY_SECRET=sua-chave-secreta

# SMS/WhatsApp (opcional)
SMS_API_KEY=sua-api-key-sms
WHATSAPP_API_TOKEN=seu-token-whatsapp
```

## 🗄️ Estrutura do Banco

### Tabela Única: prescricoes_escalonadas
```sql
CREATE TABLE prescricoes_escalonadas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    paciente_id_externo VARCHAR(100),
    paciente_nome VARCHAR(200) NOT NULL,
    paciente_telefone VARCHAR(20) NOT NULL,
    paciente_email VARCHAR(200),
    medico_id_externo VARCHAR(100),
    medico_nome VARCHAR(200) NOT NULL,
    medico_crm VARCHAR(50),
    medico_email VARCHAR(200),
    prazo_fase1_dias INTEGER NOT NULL,
    prazo_fase2_dias INTEGER NOT NULL,
    max_dias_fase3 INTEGER DEFAULT 30,
    canais TEXT[] DEFAULT '{SMS}',
    telefone_hospital VARCHAR(20) NOT NULL,
    fase_atual VARCHAR(20) DEFAULT 'FASE1',
    data_prescricao TIMESTAMP DEFAULT NOW(),
    data_limite_fase1 DATE,
    data_limite_fase2 DATE,
    data_inicio_fase3 DATE,
    dias_em_fase3 INTEGER DEFAULT 0,
    finalizada BOOLEAN DEFAULT FALSE,
    motivo_finalizacao VARCHAR(100),
    sistema_origem VARCHAR(100),
    usuario_id_externo VARCHAR(100),
    consulta_id_externo VARCHAR(100),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

## 📈 Monitoramento

### Actuator Endpoints
- `GET /actuator/health` - Health check
- `GET /actuator/info` - Informações da aplicação

### Logs
```bash
# Ver logs em tempo real
tail -f logs/hospital-reminder.log
```

## 🧪 Testes

### Executar Testes
```bash
mvn test
```

### Testes com Docker
```bash
# Subir PostgreSQL para testes
docker-compose -f docker-compose.test.yml up -d

# Executar testes
mvn test -Dspring.profiles.active=test
```

## 🔄 CI/CD

### GitHub Actions
```yaml
name: Build and Test
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
      - run: mvn test
```

## 🚨 Troubleshooting

### Problemas Comuns

1. **Conexão PostgreSQL**
   ```bash
   # Verificar se PostgreSQL está rodando
   docker-compose ps postgres
   
   # Verificar logs
   docker-compose logs postgres
   ```

2. **API Key Inválida**
   ```bash
   # Verificar header
   curl -H "X-API-KEY: chave-errada" http://localhost:8080/api/prescricoes
   # Response: 401 Unauthorized
   ```

3. **Virtual Threads**
   ```bash
   # Verificar se Java 21 está sendo usado
   java -version
   # Deve mostrar: openjdk version "21"
   ```

## 📝 Licença

MIT License - Copyright (c) 2024 Hospital System

## 🤝 Contribuição

1. Fork o projeto
2. Create feature branch
3. Commit suas mudanças
4. Push para branch
5. Abra Pull Request

## 📞 Suporte

- **Email:** support@hospital.com
- **Issues:** GitHub Issues
- **Documentação:** Swagger UI

---

**Desenvolvido com ❤️ para sistemas hospitalares**
