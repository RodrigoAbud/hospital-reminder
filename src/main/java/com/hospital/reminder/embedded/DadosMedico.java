package com.hospital.reminder.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DadosMedico {

    @Column(name = "medico_nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "medico_crm", nullable = false, length = 20)
    private String crm;

    @Column(name = "medico_especialidade", length = 50)
    private String especialidade;

    @Column(name = "medico_telefone", length = 20)
    private String telefone;

    @Column(name = "medico_email", length = 100)
    private String email;

    @Column(name = "medico_codigo", length = 50)
    private String codigoMedico;
}
