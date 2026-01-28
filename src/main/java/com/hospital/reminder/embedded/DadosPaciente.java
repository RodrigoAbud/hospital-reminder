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
public class DadosPaciente {

    @Column(name = "paciente_nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "paciente_cpf", nullable = false, length = 11, unique = true)
    private String cpf;

    @Column(name = "paciente_data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "paciente_telefone", nullable = false, length = 20)
    private String telefone;

    @Column(name = "paciente_email", length = 100)
    private String email;

    @Column(name = "paciente_convenio", length = 50)
    private String convenio;

    @Column(name = "paciente_codigo", length = 50)
    private String codigoPaciente;
}
