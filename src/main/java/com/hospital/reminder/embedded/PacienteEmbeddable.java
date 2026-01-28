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
public class PacienteEmbeddable {

    @Column(name = "paciente_id_externo", length = 100)
    private String idExterno;

    @Column(name = "paciente_nome", nullable = false, length = 200)
    private String nome;

    @Column(name = "paciente_telefone", nullable = false, length = 20)
    private String telefone;

    @Column(name = "paciente_email", length = 200)
    private String email;
}
