FLUXO CORRETO:
SISTEMA HOSPITALAR (chamador)
    ↓ (envia TODOS os dados)
SUA API (serviço de lembretes)
    ↓ (armazena apenas prescrições + cópia dos dados)
BANCO (apenas tabela prescricoes)
    ↓ (usa dados recebidos para enviar mensagens)
PROVEDORES (SMS, Email, WhatsApp)


ESTRUTURA DA REQUISIÇÃO:
POST /api/prescricoes
{
  "paciente": {
    "id": "P123",                    // ID no sistema hospitalar
    "nome": "João Silva",
    "telefone": "11999999999",
    "email": "joao@email.com"
  },
  "medico": {
    "id": "M456",                    // ID no sistema hospitalar  
    "nome": "Dra. Maria Santos",
    "crm": "12345/SP",
    "email": "maria.santos@hospital.com"
  },
  "configuracao": {
    "prazoFase1Dias": 30,
    "prazoFase2Dias": 15,
    "maxDiasFase3": 30,
    "canais": ["SMS", "WHATSAPP", "EMAIL"],  // Onde enviar
    "telefoneHospital": "1133339999"         // Para paciente ligar
  },
  "metadata": {
    "sistemaOrigem": "SistemaHospitalarX",
    "usuarioId": "U789",            // Quem criou no sistema hospitalar
    "consultaId": "C101"            // ID da consulta original (opcional)
  }
}


ESTRUTURA DA TABELA ÚNICA:
-- APENAS ESTA TABELA NA SUA API
CREATE TABLE prescricoes_escalonadas (
    -- ID interno da sua API
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Dados do paciente (recebidos)
    paciente_id_externo VARCHAR(100),      -- ID no sistema hospitalar
    paciente_nome VARCHAR(200) NOT NULL,
    paciente_telefone VARCHAR(20) NOT NULL,
    paciente_email VARCHAR(200),
    
    -- Dados do médico (recebidos)
    medico_id_externo VARCHAR(100),        -- ID no sistema hospitalar
    medico_nome VARCHAR(200) NOT NULL,
    medico_crm VARCHAR(50),
    medico_email VARCHAR(200),
    
    -- Configuração (recebida)
    prazo_fase1_dias INTEGER NOT NULL,
    prazo_fase2_dias INTEGER NOT NULL,
    max_dias_fase3 INTEGER DEFAULT 30,
    canais TEXT[] DEFAULT '{"SMS"}',       -- Array PostgreSQL
    telefone_hospital VARCHAR(20) NOT NULL,
    
    -- Estado atual (gerado pela API)
    fase_atual VARCHAR(20) DEFAULT 'FASE1',
    data_prescricao TIMESTAMP DEFAULT NOW(),
    data_limite_fase1 DATE,
    data_limite_fase2 DATE,
    data_inicio_fase3 DATE,
    dias_em_fase3 INTEGER DEFAULT 0,
    finalizada BOOLEAN DEFAULT FALSE,
    motivo_finalizacao VARCHAR(100),
    
    -- Metadata (recebido)
    sistema_origem VARCHAR(100),
    usuario_id_externo VARCHAR(100),
    consulta_id_externo VARCHAR(100),
    
    -- Controle
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    
    -- Índices
    INDEX idx_paciente_externo (paciente_id_externo),
    INDEX idx_medico_externo (medico_id_externo),
    INDEX idx_fase_atual (fase_atual),
    INDEX idx_data_limite_fase1 (data_limite_fase1)
);


ENTIDADE JPA (apenas uma tabela):
@Entity
@Table(name = "prescricoes_escalonadas")
public class PrescricaoEscalonada {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    // Dados do paciente (embutidos)
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "idExterno", column = @Column(name = "paciente_id_externo")),
        @AttributeOverride(name = "nome", column = @Column(name = "paciente_nome")),
        @AttributeOverride(name = "telefone", column = @Column(name = "paciente_telefone")),
        @AttributeOverride(name = "email", column = @Column(name = "paciente_email"))
    private PacienteEmbeddable paciente;
    
    // Dados do médico (embutidos)
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "idExterno", column = @Column(name = "medico_id_externo")),
        @AttributeOverride(name = "nome", column = @Column(name = "medico_nome")),
        @AttributeOverride(name = "crm", column = @Column(name = "medico_crm")),
        @AttributeOverride(name = "email", column = @Column(name = "medico_email"))
    })
    private MedicoEmbeddable medico;
    
    // Configuração
    @Embedded
    private ConfiguracaoEscalonamento configuracao;
    
    // Estado (gerado pela API)
    @Enumerated(EnumType.STRING)
    private FaseRetorno faseAtual = FaseRetorno.FASE1;
    
    private LocalDate dataLimiteFase1;
    private LocalDate dataLimiteFase2;
    private LocalDate dataInicioFase3;
    private Integer diasEmFase3 = 0;
    private Boolean finalizada = false;
    private String motivoFinalizacao;
    
    // Metadata
    private String sistemaOrigem;
    private String usuarioIdExterno;
    private String consultaIdExterno;
    
    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Construtor que calcula datas automaticamente
    public PrescricaoEscalonada(
            PacienteEmbeddable paciente,
            MedicoEmbeddable medico,
            ConfiguracaoEscalonamento configuracao,
            String sistemaOrigem,
            String usuarioIdExterno) {
        
        this.paciente = paciente;
        this.medico = medico;
        this.configuracao = configuracao;
        this.sistemaOrigem = sistemaOrigem;
        this.usuarioIdExterno = usuarioIdExterno;
        
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // Calcula datas automaticamente
        this.dataLimiteFase1 = LocalDate.now()
            .plusDays(configuracao.getPrazoFase1Dias());
        this.dataLimiteFase2 = this.dataLimiteFase1
            .plusDays(configuracao.getPrazoFase2Dias());
    }
}

// Objetos embutidos (não são entidades separadas)
@Embeddable
public class PacienteEmbeddable {
    private String idExterno;  // ID no sistema hospitalar
    private String nome;
    private String telefone;
    private String email;
}

@Embeddable  
public class MedicoEmbeddable {
    private String idExterno;  // ID no sistema hospitalar
    private String nome;
    private String crm;
    private String email;
}

@Embeddable
public class ConfiguracaoEscalonamento {
    private Integer prazoFase1Dias;
    private Integer prazoFase2Dias;
    private Integer maxDiasFase3 = 30;
    
    @Column(name = "canais")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<CanalComunicacao> canais = List.of(CanalComunicacao.SMS);
    
    private String telefoneHospital;
}


ENDPOINT QUE RECEBE DADOS COMPLETOS:
@RestController
@RequestMapping("/api/prescricoes")
public class PrescricaoController {
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PrescricaoResponse criarPrescricao(
            @Valid @RequestBody CriarPrescricaoCompletaRequest request) {
        
        // 1. Validação básica
        validarDados(request);
        
        // 2. Cria objeto com dados embutidos
        PrescricaoEscalonada prescricao = new PrescricaoEscalonada(
            new PacienteEmbeddable(
                request.getPaciente().getId(),
                request.getPaciente().getNome(),
                request.getPaciente().getTelefone(),
                request.getPaciente().getEmail()
            ),
            new MedicoEmbeddable(
                request.getMedico().getId(),
                request.getMedico().getNome(),
                request.getMedico().getCrm(),
                request.getMedico().getEmail()
            ),
            new ConfiguracaoEscalonamento(
                request.getConfiguracao().getPrazoFase1Dias(),
                request.getConfiguracao().getPrazoFase2Dias(),
                request.getConfiguracao().getMaxDiasFase3(),
                request.getConfiguracao().getCanais(),
                request.getConfiguracao().getTelefoneHospital()
            ),
            request.getMetadata().getSistemaOrigem(),
            request.getMetadata().getUsuarioId()
        );
        
        // 3. Salva (apenas esta tabela)
        PrescricaoEscalonada salva = repository.save(prescricao);
        
        // 4. Agenda primeiro lembrete (FASE 1)
        agendadorService.agendarLembretesFase1(salva);
        
        // 5. Retorna resposta com ID da SUA API
        return PrescricaoResponse.fromEntity(salva);
    }
    
    // Outros endpoints...
    @PutMapping("/{id}/finalizar")
    public void finalizarPrescricao(
            @PathVariable UUID id,
            @RequestBody FinalizarPrescricaoRequest request) {
        // Quando paciente retorna, sistema hospitalar notifica
    }
    
    @GetMapping("/externo/{idExterno}")
    public List<PrescricaoResponse> buscarPorPacienteExterno(
            @PathVariable String idExterno) {
        // Sistema hospitalar busca prescrições do paciente
    }
}


DTOs DE REQUISIÇÃO:
// Recebe TUDO do sistema hospitalar
public record CriarPrescricaoCompletaRequest(
    
    @Valid
    PacienteRequest paciente,
    
    @Valid
    MedicoRequest medico,
    
    @Valid
    ConfiguracaoRequest configuracao,
    
    @Valid
    MetadataRequest metadata
) {}

public record PacienteRequest(
    @NotBlank
    String id,  // ID no sistema hospitalar
    
    @NotBlank
    @Size(min = 3, max = 200)
    String nome,
    
    @NotBlank
    @Pattern(regexp = "\\d{10,11}")
    String telefone,
    
    @Email
    String email,
) {}

public record MedicoRequest(
    @NotBlank
    String id,  // ID no sistema hospitalar
    
    @NotBlank
    String nome,
    
    String crm,
    
    @Email
    String email
) {}

public record ConfiguracaoRequest(
    @Min(1) @Max(365)
    Integer prazoFase1Dias,
    
    @Min(1) @Max(180)
    Integer prazoFase2Dias,
    
    @Min(1) @Max(90)
    Integer maxDiasFase3,
    
    List<CanalComunicacao> canais,
    
    @NotBlank
    String telefoneHospital
) {}

public record MetadataRequest(
    String sistemaOrigem,
    String usuarioId,
    String consultaId
) {}


SERVIÇO QUE USA DADOS RECEBIDOS:
@Service
public class MensageriaService {
    
    public void enviarLembreteFase1(PrescricaoEscalonada prescricao) {
        // Usa dados EMBUTIDOS na prescrição
        String telefonePaciente = prescricao.getPaciente().getTelefone();
        String nomePaciente = prescricao.getPaciente().getNome();
        String nomeMedico = prescricao.getMedico().getNome();
        String telefoneHospital = prescricao.getConfiguracao().getTelefoneHospital();
        
        String mensagem = String.format("""
            Sr(a) %s,
            Lembrete: Retorno com Dr(a). %s
            Prazo: %d dias
            Ligue para agendar: %s
            """,
            nomePaciente,
            nomeMedico,
            prescricao.getConfiguracao().getPrazoFase1Dias(),
            telefoneHospital
        );
        
        // Envia pelos canais configurados
        prescricao.getConfiguracao().getCanais().forEach(canal -> {
            switch (canal) {
                case SMS -> smsService.enviar(telefonePaciente, mensagem);
                case WHATSAPP -> whatsappService.enviar(telefonePaciente, mensagem);
                case EMAIL -> {
                    String email = prescricao.getPaciente().getEmail();
                    if (email != null) {
                        emailService.enviar(email, "Lembrete de Retorno", mensagem);
                    }
                }
            }
        });
        
        // Também envia notificação para o médico
        notificarMedico(prescricao, "Lembrete enviado ao paciente");
    }
}


SINCRONIZAÇÃO DE ATUALIZAÇÕES:
@PutMapping("/{id}/atualizar-dados")
public void atualizarDadosPrescricao(
        @PathVariable UUID id,
        @Valid @RequestBody AtualizarDadosRequest request) {
    
    // Sistema hospitalar pode atualizar dados se mudaram
    PrescricaoEscalonada prescricao = repository.findById(id)
        .orElseThrow(() -> new PrescricaoNotFoundException());
    
    // Atualiza dados embutidos
    prescricao.getPaciente().setTelefone(request.novoTelefone());
    prescricao.getPaciente().setEmail(request.novoEmail());
    
    repository.save(prescricao);
    
    logger.info("Dados atualizados para prescrição {}: telefone={}",
        id, request.novoTelefone());
}


PROMPT REVISADO PARA SUA API:
CRIE UMA API DE LEMBRETES ESCALONADOS QUE RECEBE DADOS COMPLETOS

ARQUITETURA:
- Sistema hospitalar ENVIA dados completos
- API armazena TUDO em UMA tabela
- API gerencia ciclo de vida das prescrições
- Dados de paciente/médico ficam EMBUTIDOS

BANCO DE DADOS (APENAS):
* prescricoes_escalonadas (com colunas para todos os dados)

FLUXO:
1. Sistema hospitalar chama API com:
   - Dados COMPLETOS do paciente (nome, telefone, email)
   - Dados COMPLETOS do médico (nome, telefone consultório)
   - Configuração (prazos, canais, telefone hospital)

2. API:
   a) Valida dados
   b) Cria registro ÚNICO com tudo embutido
   c) Calcula datas automaticamente
   d) Agenda lembretes
   e) Retorna ID da prescrição (UUID)

3. Mensagens usam dados EMBUTIDOS:
   "Sr. [nome da coluna paciente_nome],
    Ligue para [telefone_hospital]"

4. Atualizações (se necessário):
   - Sistema hospitalar chama endpoint para atualizar telefone/email
   - Ou cria nova prescrição com dados atualizados

ENDPOINTS PRINCIPAIS:
1. POST /prescricoes (recebe dados completos)
2. PUT /prescricoes/{id}/finalizar (paciente retornou)
3. PUT /prescricoes/{id}/dados (atualiza telefone/email)
4. GET /prescricoes/paciente/{idExterno} (busca por ID do sistema hospitalar)
5. DELETE /prescricoes/{id} (LGPD - remove tudo)

SEGURANÇA:
- Autenticação por API Key (sistema hospitalar)
- Logs anonimizados
- Criptografia de dados sensíveis
- Rate limiting por cliente

GERAR:
1. Entidade JPA única com @Embedded
2. DTOs de request/response completos
3. Serviço que usa dados embutidos
4. Migração SQL da tabela única
5. Endpoints com validação