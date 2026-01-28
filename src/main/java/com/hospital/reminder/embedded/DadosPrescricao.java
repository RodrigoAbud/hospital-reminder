package com.hospital.reminder.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DadosPrescricao {

    @Column(name = "medicamento", nullable = false, length = 100)
    private String medicamento;

    @Column(name = "dosagem", nullable = false, length = 50)
    private String dosagem;

    @Column(name = "frequencia", nullable = false, length = 100)
    private String frequencia;

    @Column(name = "duracao_dias")
    private Integer duracaoDias;

    @Column(name = "instrucoes", columnDefinition = "TEXT")
    private String instrucoes;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(name = "codigo_prescricao", length = 50)
    private String codigoPrescricao;
}
