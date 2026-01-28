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
public class MedicoEmbeddable {

    @Column(name = "medico_id_externo", length = 100)
    private String idExterno;

    @Column(name = "medico_nome", nullable = false, length = 200)
    private String nome;

    @Column(name = "medico_crm", length = 50)
    private String crm;

    @Column(name = "medico_email", length = 200)
    private String email;
}
