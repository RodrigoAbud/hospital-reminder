package com.hospital.reminder.entity;

import com.hospital.reminder.embedded.ConfiguracaoEscalonamento;
import com.hospital.reminder.embedded.MedicoEmbeddable;
import com.hospital.reminder.embedded.PacienteEmbeddable;
import com.hospital.reminder.enums.FaseRetorno;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "prescricoes_escalonadas")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescricaoEscalonada {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "idExterno", column = @Column(name = "paciente_id_externo")),
        @AttributeOverride(name = "nome", column = @Column(name = "paciente_nome")),
        @AttributeOverride(name = "telefone", column = @Column(name = "paciente_telefone")),
        @AttributeOverride(name = "email", column = @Column(name = "paciente_email"))
    })
    private PacienteEmbeddable paciente;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "idExterno", column = @Column(name = "medico_id_externo")),
        @AttributeOverride(name = "nome", column = @Column(name = "medico_nome")),
        @AttributeOverride(name = "crm", column = @Column(name = "medico_crm")),
        @AttributeOverride(name = "email", column = @Column(name = "medico_email"))
    })
    private MedicoEmbeddable medico;

    @Embedded
    private ConfiguracaoEscalonamento configuracao;

    @Enumerated(EnumType.STRING)
    @Column(name = "fase_atual", nullable = false)
    private FaseRetorno faseAtual = FaseRetorno.FASE1;

    @Column(name = "data_prescricao", nullable = false)
    private LocalDateTime dataPrescricao;

    @Column(name = "data_limite_fase1")
    private LocalDate dataLimiteFase1;

    @Column(name = "data_limite_fase2")
    private LocalDate dataLimiteFase2;

    @Column(name = "data_inicio_fase3")
    private LocalDate dataInicioFase3;

    @Column(name = "dias_em_fase3")
    private Integer diasEmFase3 = 0;

    @Column(name = "finalizada")
    private Boolean finalizada = false;

    @Column(name = "motivo_finalizacao")
    private String motivoFinalizacao;

    @Column(name = "sistema_origem")
    private String sistemaOrigem;

    @Column(name = "usuario_id_externo")
    private String usuarioIdExterno;

    @Column(name = "consulta_id_externo")
    private String consultaIdExterno;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "versao")
    private Long versao;

    @PrePersist
    protected void onCreate() {
        LocalDateTime agora = LocalDateTime.now();
        if (createdAt == null) createdAt = agora;
        if (updatedAt == null) updatedAt = agora;
        if (dataPrescricao == null) dataPrescricao = agora;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
