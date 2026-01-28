package com.hospital.reminder.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracaoEscalonamento {

    @Column(name = "prazo_fase1_dias", nullable = false)
    private Integer prazoFase1Dias;

    @Column(name = "prazo_fase2_dias", nullable = false)
    private Integer prazoFase2Dias;

    @Column(name = "max_dias_fase3")
    private Integer maxDiasFase3 = 30;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "canais", columnDefinition = "text[]")
    private String[] canais;

    @Column(name = "telefone_hospital", nullable = false, length = 20)
    private String telefoneHospital;
}
